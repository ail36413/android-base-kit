package com.ail.lib_network.websocket

interface IWebSocketManager {
    fun connect(
        connectionId: String,
        url: String,
        config: WebSocketManager.Config = WebSocketManager.Config(),
        listener: WebSocketManager.WebSocketListener
    )

    fun disconnect(connectionId: String, permanent: Boolean = true)
    fun disconnectAll()
    fun reconnect(connectionId: String): Boolean
    fun sendMessage(connectionId: String, text: String): Boolean
    fun sendMessage(connectionId: String, bytes: ByteArray): Boolean
    fun isConnected(connectionId: String): Boolean

    // === 默认单连接快捷 API ===
    fun connectDefault(
        url: String,
        config: WebSocketManager.Config = WebSocketManager.Config(),
        listener: WebSocketManager.WebSocketListener
    )

    fun disconnectDefault(permanent: Boolean = true)
    fun reconnectDefault(): Boolean
    fun sendText(text: String): Boolean
    fun sendBinary(bytes: ByteArray): Boolean
    fun isConnected(): Boolean
}