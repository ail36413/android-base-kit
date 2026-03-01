package com.ail.android_base_kit.network.websocket

import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ail.lib_network.websocket.IWebSocketManager
import com.ail.lib_network.websocket.WebSocketManager
import com.ail.android_base_kit.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class WebSocketDemoActivity : AppCompatActivity() {

    @Inject
    lateinit var wsManager: IWebSocketManager

    private lateinit var tvLog: TextView
    private lateinit var etMessage: EditText

    private val connectionId = "demo_ws"
    private val wsUrl = "wss://echo.websocket.org" // 替换为你的地址

    private val config = WebSocketManager.Config(
        enableHeartbeat = true,
        heartbeatIntervalMs = 30_000,
        enableMessageReplay = true,
        messageQueueCapacity = 50,
        dropOldestWhenQueueFull = true,
        callbackOnMainThread = true,
        enableDebugLog = true
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_websocket_demo)

        tvLog = findViewById(R.id.tv_ws_log)
        etMessage = findViewById(R.id.et_ws_message)
        etMessage.inputType = InputType.TYPE_CLASS_TEXT

        findViewById<Button>(R.id.btn_ws_connect).setOnClickListener {
            connect()
        }
        findViewById<Button>(R.id.btn_ws_reconnect).setOnClickListener {
            val started = wsManager.reconnect(connectionId)
            appendLog(if (started) "触发手动重连" else "手动重连失败（当前非断开状态）")
        }
        findViewById<Button>(R.id.btn_ws_disconnect).setOnClickListener {
            wsManager.disconnect(connectionId, permanent = true)
            appendLog("主动断开连接（永久）")
        }
        findViewById<Button>(R.id.btn_ws_send_text).setOnClickListener {
            val content = etMessage.text?.toString()?.trim().orEmpty()
            if (content.isBlank()) {
                toast("请输入要发送的文本")
                return@setOnClickListener
            }
            val queued = wsManager.sendMessage(connectionId, content)
            appendLog(if (queued) "发送文本：$content" else "发送失败：$content")
        }
        findViewById<Button>(R.id.btn_ws_send_binary).setOnClickListener {
            val bytes = "binary-demo".toByteArray()
            val queued = wsManager.sendMessage(connectionId, bytes)
            appendLog(if (queued) "发送二进制：${bytes.size} bytes" else "发送失败：二进制")
        }

        appendLog("演示功能：状态回调、心跳、重连、离线补发、回调主线程")
    }

    override fun onDestroy() {
        super.onDestroy()
        wsManager.disconnect(connectionId, permanent = true)
    }

    private fun connect() {
        wsManager.connect(
            connectionId = connectionId,
            url = wsUrl,
            config = config,
            listener = object : WebSocketManager.WebSocketListener {
                override fun onOpen(connectionId: String) {
                    appendLog("连接成功：$connectionId")
                }

                override fun onStateChanged(
                    connectionId: String,
                    oldState: WebSocketManager.State,
                    newState: WebSocketManager.State
                ) {
                    appendLog("状态变化：$oldState -> $newState")
                }

                override fun onMessage(connectionId: String, text: String) {
                    appendLog("收到文本：$text")
                }

                override fun onMessage(connectionId: String, bytes: ByteArray) {
                    appendLog("收到二进制：${bytes.size} bytes")
                }

                override fun onClosing(connectionId: String, code: Int, reason: String) {
                    appendLog("连接关闭中：$code $reason")
                }

                override fun onClosed(connectionId: String, code: Int, reason: String) {
                    appendLog("连接已关闭：$code $reason")
                }

                override fun onFailure(connectionId: String, throwable: Throwable) {
                    appendLog("连接失败：${throwable.message}")
                }

                override fun onReconnecting(connectionId: String, attempt: Int) {
                    appendLog("触发重连：第$attempt 次")
                }

                override fun onHeartbeatTimeout(connectionId: String) {
                    appendLog("心跳超时：触发断开")
                }
            }
        )
        appendLog("开始连接：$wsUrl")
    }

    private fun appendLog(message: String) {
        val current = tvLog.text?.toString().orEmpty()
        val next = if (current.isBlank()) message else "$current\n$message"
        tvLog.text = if (next.length > 4000) next.takeLast(4000) else next
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
