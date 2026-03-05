package com.ail.android_base_kit.network.websocket

import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
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
    private val wsUrl = "wss://ws.postman-echo.com/raw" // 公共可用测试地址

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
            appendLogRes(if (started) R.string.ws_log_manual_reconnect_triggered else R.string.ws_log_manual_reconnect_failed)
        }
        findViewById<Button>(R.id.btn_ws_disconnect).setOnClickListener {
            wsManager.disconnect(connectionId, permanent = true)
            appendLogRes(R.string.ws_log_disconnected_permanent)
        }
        findViewById<Button>(R.id.btn_ws_send_text).setOnClickListener {
            val content = etMessage.text?.toString()?.trim().orEmpty()
            if (content.isBlank()) {
                toast(getString(R.string.ws_toast_input_required))
                return@setOnClickListener
            }
            val queued = wsManager.sendMessage(connectionId, content)
            appendLogRes(if (queued) R.string.ws_log_send_text_ok else R.string.ws_log_send_text_fail, content)
        }
        findViewById<Button>(R.id.btn_ws_send_binary).setOnClickListener {
            val bytes = "binary-demo".toByteArray()
            val queued = wsManager.sendMessage(connectionId, bytes)
            appendLogRes(
                if (queued) R.string.ws_log_send_binary_ok else R.string.ws_log_send_binary_fail,
                bytes.size
            )
        }
        
        // 单连接快捷API演示
        findViewById<Button>(R.id.btn_ws_connect_default).setOnClickListener {
            connectDefault()
        }
        findViewById<Button>(R.id.btn_ws_send_default).setOnClickListener {
            val content = etMessage.text?.toString()?.trim().orEmpty()
            if (content.isBlank()) {
                toast(getString(R.string.ws_toast_input_required))
                return@setOnClickListener
            }
            val sent = wsManager.sendText(content)
            appendLogRes(if (sent) R.string.ws_log_default_send_ok else R.string.ws_log_default_send_fail, content)
        }

        appendLog(getString(R.string.ws_intro))
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
                    appendLogRes(R.string.ws_log_connected, connectionId)
                }

                override fun onStateChanged(
                    connectionId: String,
                    oldState: WebSocketManager.State,
                    newState: WebSocketManager.State
                ) {
                    appendLogRes(R.string.ws_log_state_changed, oldState.name, newState.name)
                }

                override fun onMessage(connectionId: String, text: String) {
                    appendLogRes(R.string.ws_log_receive_text, text)
                }

                override fun onMessage(connectionId: String, bytes: ByteArray) {
                    appendLogRes(R.string.ws_log_receive_binary, bytes.size)
                }

                override fun onClosing(connectionId: String, code: Int, reason: String) {
                    appendLogRes(R.string.ws_log_closing, code, reason)
                }

                override fun onClosed(connectionId: String, code: Int, reason: String) {
                    appendLogRes(R.string.ws_log_closed, code, reason)
                }

                override fun onFailure(connectionId: String, throwable: Throwable) {
                    appendLogRes(R.string.ws_log_failure, throwable.message ?: "")
                }

                override fun onReconnecting(connectionId: String, attempt: Int) {
                    appendLogRes(R.string.ws_log_reconnecting, attempt)
                }

                override fun onHeartbeatTimeout(connectionId: String) {
                    appendLogRes(R.string.ws_log_heartbeat_timeout)
                }
            }
        )
        appendLogRes(R.string.ws_log_connect_start, wsUrl)
    }

    private fun appendLog(message: String) {
        val current = tvLog.text?.toString().orEmpty()
        val next = if (current.isBlank()) message else "$current\n$message"
        tvLog.text = if (next.length > 4000) next.takeLast(4000) else next
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun appendLogRes(@StringRes resId: Int, vararg args: Any) {
        appendLog(getString(resId, *args))
    }

    // ==================== 单连接快捷API演示 ====================
    private val defaultUrl = "wss://ws.postman-echo.com/raw"

    private fun connectDefault() {
        wsManager.connectDefault(
            url = defaultUrl,
            config = WebSocketManager.Config(
                enableHeartbeat = true,
                heartbeatIntervalMs = 30_000,
                enableMessageReplay = true,
                callbackOnMainThread = true,
                enableDebugLog = true
            ),
            listener = object : WebSocketManager.WebSocketListener {
                override fun onOpen(connectionId: String) {
                    appendLogRes(R.string.ws_log_default_connected)
                }

                override fun onStateChanged(
                    connectionId: String,
                    oldState: WebSocketManager.State,
                    newState: WebSocketManager.State
                ) {
                    appendLogRes(R.string.ws_log_default_state_changed, oldState.name, newState.name)
                }

                override fun onMessage(connectionId: String, text: String) {
                    appendLogRes(R.string.ws_log_default_receive_text, text)
                }

                override fun onMessage(connectionId: String, bytes: ByteArray) {
                    appendLogRes(R.string.ws_log_default_receive_binary, bytes.size)
                }

                override fun onClosed(connectionId: String, code: Int, reason: String) {
                    appendLogRes(R.string.ws_log_default_closed, code, reason)
                }

                override fun onFailure(connectionId: String, throwable: Throwable) {
                    appendLogRes(R.string.ws_log_default_failure, throwable.message ?: "")
                }

                override fun onReconnecting(connectionId: String, attempt: Int) {
                    appendLogRes(R.string.ws_log_default_reconnecting, attempt)
                }
            }
        )
        appendLogRes(R.string.ws_log_default_connect_start, defaultUrl)
    }
}
