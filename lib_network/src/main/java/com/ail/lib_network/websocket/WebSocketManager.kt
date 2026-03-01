package com.ail.lib_network.websocket

import okhttp3.OkHttpClient
import java.util.concurrent.ConcurrentHashMap

class WebSocketManager(
    private val okHttpClient: OkHttpClient
) : IWebSocketManager {

    companion object {
        private const val DEFAULT_CONNECTION_ID = "default_ws"
    }

    enum class State {
        DISCONNECTED,
        CONNECTING,
        CONNECTED
    }

    private val connections = ConcurrentHashMap<String, WebSocketClientImpl>()

    override fun connect(
        connectionId: String,
        url: String,
        config: Config,
        listener: WebSocketListener
    ) {
        connections[connectionId]?.disconnect(permanent = true)
        val client = WebSocketClientImpl(okHttpClient, url, config, connectionId, listener)
        connections[connectionId] = client
        client.connect()
    }

    override fun disconnect(connectionId: String, permanent: Boolean) {
        connections[connectionId]?.disconnect(permanent)
        if (permanent) {
            connections.remove(connectionId)
        }
    }

    override fun disconnectAll() {
        connections.values.forEach { it.disconnect(true) }
        connections.clear()
    }

    override fun reconnect(connectionId: String): Boolean {
        return connections[connectionId]?.reconnect() ?: false
    }

    override fun sendMessage(connectionId: String, text: String): Boolean {
        return connections[connectionId]?.sendMessage(text) ?: false
    }

    override fun sendMessage(connectionId: String, bytes: ByteArray): Boolean {
        return connections[connectionId]?.sendMessage(bytes) ?: false
    }

    override fun isConnected(connectionId: String): Boolean {
        return connections[connectionId]?.isConnected() ?: false
    }

    override fun connectDefault(
        url: String,
        config: Config,
        listener: WebSocketListener
    ) {
        connect(DEFAULT_CONNECTION_ID, url, config, listener)
    }

    override fun disconnectDefault(permanent: Boolean) {
        disconnect(DEFAULT_CONNECTION_ID, permanent)
    }

    override fun reconnectDefault(): Boolean {
        return reconnect(DEFAULT_CONNECTION_ID)
    }

    override fun sendText(text: String): Boolean {
        return sendMessage(DEFAULT_CONNECTION_ID, text)
    }

    override fun sendBinary(bytes: ByteArray): Boolean {
        return sendMessage(DEFAULT_CONNECTION_ID, bytes)
    }

    override fun isConnected(): Boolean {
        return isConnected(DEFAULT_CONNECTION_ID)
    }

    // --- 配置类 ---
    data class Config(
        val connectTimeout: Long = 10,
        val readTimeout: Long = 60,
        val writeTimeout: Long = 60,
        val enableHeartbeat: Boolean = true,
        val heartbeatIntervalMs: Long = 30_000,
        val heartbeatMessage: String = "{\"type\":\"ping\"}", // ✅ 新增：可配置心跳消息

        // 重连策略
        val reconnectBaseDelayMs: Long = 2_000,
        val reconnectMaxDelayMs: Long = 30_000,

        // ✅ 新增：是否启用离线消息补发
        val enableMessageReplay: Boolean = false,

        // 消息队列
        val messageQueueCapacity: Int = 100,
        val dropOldestWhenQueueFull: Boolean = true,

        // 回调线程
        val callbackOnMainThread: Boolean = true,

        // 日志
        val enableDebugLog: Boolean = true

    )

    // --- 回调接口 ---
    interface WebSocketListener {
        fun onStateChanged(connectionId: String, oldState: State, newState: State) {}
        fun onOpen(connectionId: String) {}
        fun onMessage(connectionId: String, text: String) {}
        fun onMessage(connectionId: String, bytes: ByteArray) {}
        fun onClosing(connectionId: String, code: Int, reason: String) {}
        fun onClosed(connectionId: String, code: Int, reason: String) {}
        fun onFailure(connectionId: String, throwable: Throwable) {}
        fun onReconnecting(connectionId: String, attempt: Int) {}
        fun onHeartbeatTimeout(connectionId: String) {}
    }
}