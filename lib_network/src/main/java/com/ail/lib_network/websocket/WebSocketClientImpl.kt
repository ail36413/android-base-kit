package com.ail.lib_network.websocket

import android.os.Handler
import android.os.Looper
import okhttp3.*
import okio.ByteString
import okio.ProtocolException
import java.net.UnknownHostException
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import kotlin.collections.isNotEmpty
import kotlin.math.min
import kotlin.random.Random

internal class WebSocketClientImpl(
    private val okHttpClient: OkHttpClient,
    private val url: String,
    private val config: WebSocketManager.Config,
    private val connectionId: String,
    private val listener: WebSocketManager.WebSocketListener
) {

    private var webSocket: WebSocket? = null
    private var isManualClose = false
    private var isPermanentClose = false
    private var reconnectAttempt = 0
    private val mainHandler = Handler(Looper.getMainLooper())
    private var reconnectRunnable: Runnable? = null

    @Volatile
    private var currentState = WebSocketManager.State.DISCONNECTED

    private val messageQueue = LinkedBlockingQueue<QueuedMessage>(config.messageQueueCapacity)

    private var lastPongTime = System.currentTimeMillis()
    private val pongTimeoutMs = config.heartbeatIntervalMs + 15_000

    // === 应用层心跳：主动发送 Ping ===
    private val pingSenderRunnable = object : Runnable {
        override fun run() {
            if (currentState == WebSocketManager.State.CONNECTED) {
                WebSocketLogger.d(connectionId, "发送应用层心跳，内容：${config.heartbeatMessage}")
                sendMessage(config.heartbeatMessage)
            }
            if (currentState == WebSocketManager.State.CONNECTED) {
                mainHandler.postDelayed(this, config.heartbeatIntervalMs)
            }
        }
    }

    // === Pong 超时检测器 ===
    private val pongCheckerRunnable = object : Runnable {
        override fun run() {
            val timeoutDuration = System.currentTimeMillis() - lastPongTime
            if (timeoutDuration > pongTimeoutMs) {
                WebSocketLogger.w(
                    connectionId,
                    "心跳超时，已${timeoutDuration}ms未收到服务端消息/心跳回复，即将关闭连接"
                )
                dispatchCallback { listener.onHeartbeatTimeout(connectionId) }
                webSocket?.close(1001, "Heartbeat timeout")
            } else {
                // 继续检查（只要连接还活跃）
                if (currentState == WebSocketManager.State.CONNECTED) {
                    mainHandler.postDelayed(this, 5_000)
                }
            }
        }
    }

    sealed class QueuedMessage {
        data class Text(val content: String) : QueuedMessage()
        data class Binary(val data: ByteArray) : QueuedMessage() {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (other !is Binary) return false
                return data.contentEquals(other.data)
            }

            override fun hashCode(): Int = data.contentHashCode()
        }
    }

    // 🔥 核心：初始化日志开关，绑定配置的debug开关
    init {
        WebSocketLogger.setLogEnabled(config.enableDebugLog)
    }

    private fun createClient(): OkHttpClient {
        return okHttpClient.newBuilder()
            .connectTimeout(config.connectTimeout, TimeUnit.SECONDS)
            .readTimeout(config.readTimeout, TimeUnit.SECONDS)
            .writeTimeout(config.writeTimeout, TimeUnit.SECONDS)
            .build()
    }

    private inline fun dispatchCallback(crossinline action: () -> Unit) {
        if (!config.callbackOnMainThread) {
            action()
            return
        }
        if (Looper.myLooper() == Looper.getMainLooper()) {
            action()
        } else {
            mainHandler.post { action() }
        }
    }

    private fun enqueueMessage(message: QueuedMessage): Boolean {
        if (messageQueue.offer(message)) return true
        if (config.dropOldestWhenQueueFull) {
            messageQueue.poll()
            val enqueued = messageQueue.offer(message)
            if (!enqueued) {
                WebSocketLogger.w(connectionId, "消息队列已满，丢弃消息（已尝试丢弃最旧消息）")
            }
            return enqueued
        }
        WebSocketLogger.w(connectionId, "消息队列已满，丢弃消息")
        return false
    }

    private fun changeState(newState: WebSocketManager.State) {
        if (currentState == newState) return
        val oldState = currentState
        currentState = newState
        dispatchCallback { listener.onStateChanged(connectionId, oldState, newState) }
    }

    fun connect() {
        connectInternal(fromReconnect = false)
    }

    fun reconnect(): Boolean {
        if (currentState != WebSocketManager.State.DISCONNECTED) return false
        isManualClose = false
        isPermanentClose = false
        reconnectAttempt = 0
        reconnectRunnable?.let { mainHandler.removeCallbacks(it) }
        reconnectRunnable = null
        connectInternal(fromReconnect = true)
        return true
    }

    private fun connectInternal(fromReconnect: Boolean) {
        if (isPermanentClose || currentState != WebSocketManager.State.DISCONNECTED) return
        if (fromReconnect && isManualClose) return
        if (url.isBlank()) {
            val error = IllegalArgumentException("WebSocket url 不能为空")
            WebSocketLogger.e(connectionId, "WebSocket连接失败：url为空", error)
            dispatchCallback { listener.onFailure(connectionId, error) }
            changeState(WebSocketManager.State.DISCONNECTED)
            return
        }

        changeState(WebSocketManager.State.CONNECTING)
        WebSocketLogger.d(connectionId, "开始建立WebSocket连接，目标URL：$url")
        val request = Request.Builder().url(url).build()
        webSocket = createClient().newWebSocket(request, createListener())
        lastPongTime = System.currentTimeMillis()
    }

    fun disconnect(permanent: Boolean) {
        WebSocketLogger.d(
            connectionId,
            "执行断开连接操作，是否永久断开：$permanent，当前连接状态：$currentState"
        )
        isManualClose = true
        isPermanentClose = permanent

        reconnectRunnable?.let { mainHandler.removeCallbacks(it) }
        reconnectRunnable = null

        if (!permanent) {
            reconnectAttempt = 0
        }

        stopPingSender()
        stopPongChecker()

        if (currentState == WebSocketManager.State.CONNECTING || currentState == WebSocketManager.State.CONNECTED) {
            webSocket?.close(1000, "Normal close")
            webSocket = null
        }

        changeState(WebSocketManager.State.DISCONNECTED)

        if (permanent) {
            messageQueue.clear()
        }
    }

    fun sendMessage(text: String): Boolean {
        return when (currentState) {
            WebSocketManager.State.CONNECTED -> {
                val result = webSocket?.send(text) ?: false
                if (result) {
                    WebSocketLogger.d(connectionId, "发送文本消息：$text")
                } else {
                    WebSocketLogger.w(connectionId, "发送文本消息失败，WebSocket 已断开")
                }
                result
            }
            else -> {
                if (config.enableMessageReplay) {
                    val enqueued = enqueueMessage(QueuedMessage.Text(text))
                    WebSocketLogger.d(connectionId, "当前未连接，文本消息已加入离线队列，入队${if (enqueued) "成功" else "失败"}，队列大小：${messageQueue.size}")
                    enqueued
                } else {
                    WebSocketLogger.w(connectionId, "当前未连接，文本消息已丢弃（未开启离线补发）")
                    false
                }
            }
        }
    }

    fun sendMessage(bytes: ByteArray): Boolean {
        return when (currentState) {
            WebSocketManager.State.CONNECTED -> {
                try {
                    val result = webSocket?.send(ByteString.of(*bytes)) ?: false
                    if (result) {
                        WebSocketLogger.d(connectionId, "发送二进制消息，大小：${bytes.size} bytes")
                    } else {
                        WebSocketLogger.w(connectionId, "发送二进制消息失败，WebSocket 已断开")
                    }
                    result
                } catch (e: Exception) {
                    WebSocketLogger.w(connectionId, "发送二进制消息异常：${e.message}", e)
                    false
                }
            }

            else -> {
                if (config.enableMessageReplay) {
                    val enqueued = enqueueMessage(QueuedMessage.Binary(bytes))
                    WebSocketLogger.d(connectionId, "当前未连接，二进制消息已加入离线队列，入队${if (enqueued) "成功" else "失败"}，队列大小：${messageQueue.size}")
                    enqueued
                } else {
                    WebSocketLogger.w(connectionId, "当前未连接，二进制消息已丢弃（未开启离线补发）")
                    false
                }
            }
        }
    }

    fun isConnected(): Boolean = currentState == WebSocketManager.State.CONNECTED

    private fun createListener(): WebSocketListener {
        return object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                reconnectAttempt = 0
                isManualClose = false
                changeState(WebSocketManager.State.CONNECTED)
                lastPongTime = System.currentTimeMillis()
                if (config.enableHeartbeat) {
                    startPingSender()
                    startPongChecker()
                }
                if (config.enableMessageReplay) flushMessageQueue()
                WebSocketLogger.d(
                    connectionId,
                    "WebSocket连接成功，HTTP响应码：${response.code}，是否开启心跳：${config.enableHeartbeat}，待补发消息数：${messageQueue.size}"
                )
                dispatchCallback { listener.onOpen(connectionId) }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                lastPongTime = System.currentTimeMillis()
                WebSocketLogger.d(connectionId, "收到文本消息：$text")
                dispatchCallback { listener.onMessage(connectionId, text) }
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                lastPongTime = System.currentTimeMillis()
                WebSocketLogger.d(connectionId, "收到二进制消息，大小：${bytes.size} bytes，内容(hex)：${bytes.hex()}")
                dispatchCallback { listener.onMessage(connectionId, bytes.toByteArray()) }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                WebSocketLogger.d(
                    connectionId,
                    "WebSocket连接正在关闭，关闭码：$code，关闭原因：$reason"
                )
                dispatchCallback { listener.onClosing(connectionId, code, reason) }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                this@WebSocketClientImpl.webSocket = null
                changeState(WebSocketManager.State.DISCONNECTED)
                WebSocketLogger.d(
                    connectionId,
                    "WebSocket连接已完全关闭，关闭码：$code，关闭原因：$reason"
                )
                dispatchCallback { listener.onClosed(connectionId, code, reason) }
                attemptReconnect()
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                this@WebSocketClientImpl.webSocket = null
                changeState(WebSocketManager.State.DISCONNECTED)

                val isUnrecoverable = (response?.code in setOf(401, 403, 404)) ||
                        t is UnknownHostException ||
                        t is ProtocolException

                if (isUnrecoverable) {
                    isPermanentClose = true
                    messageQueue.clear()
                }
                WebSocketLogger.e(
                    connectionId,
                    "WebSocket连接失败，HTTP响应码：${response?.code ?: -1}，是否为不可恢复异常：$isUnrecoverable，异常原因：${t.message}",
                    t
                )
                dispatchCallback { listener.onFailure(connectionId, t) }

                if (!isUnrecoverable) {
                    attemptReconnect()
                }
            }
        }
    }

    private fun attemptReconnect() {
        if (isManualClose || isPermanentClose || currentState != WebSocketManager.State.DISCONNECTED) return

        reconnectAttempt++

        val baseDelay = config.reconnectBaseDelayMs * (1L shl min(reconnectAttempt - 1, 10))
        val delayWithJitter = (baseDelay * (0.9 + Random.nextDouble() * 0.2)).toLong()
        val finalDelay = delayWithJitter.coerceIn(1_000L, config.reconnectMaxDelayMs)
        WebSocketLogger.d(
            connectionId,
            "触发WebSocket重连，第$reconnectAttempt 次重连，重连延迟：${finalDelay}ms"
        )
        dispatchCallback { listener.onReconnecting(connectionId, reconnectAttempt) }

        val runnable = Runnable {
            if (!isPermanentClose && currentState == WebSocketManager.State.DISCONNECTED && !isManualClose) {
                connectInternal(fromReconnect = true)
            }
        }
        reconnectRunnable = runnable
        mainHandler.postDelayed(runnable, finalDelay)
    }

    // —————— 心跳：启动/停止 ——————
    private fun startPingSender() {
        mainHandler.removeCallbacks(pingSenderRunnable)
        mainHandler.postDelayed(pingSenderRunnable, config.heartbeatIntervalMs)
    }

    private fun stopPingSender() {
        mainHandler.removeCallbacks(pingSenderRunnable)
    }

    private fun startPongChecker() {
        mainHandler.removeCallbacks(pongCheckerRunnable)
        mainHandler.postDelayed(pongCheckerRunnable, 5_000)
    }

    private fun stopPongChecker() {
        mainHandler.removeCallbacks(pongCheckerRunnable)
    }

    private fun flushMessageQueue() {
        if (messageQueue.isEmpty()) return
        val pending = mutableListOf<QueuedMessage>()
        messageQueue.drainTo(pending)
        pending.forEach { message ->
            when (message) {
                is QueuedMessage.Text -> sendMessage(message.content)
                is QueuedMessage.Binary -> sendMessage(message.data)
            }
        }
    }
}