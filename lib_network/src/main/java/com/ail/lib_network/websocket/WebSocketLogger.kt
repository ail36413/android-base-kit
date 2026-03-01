package com.ail.lib_network.websocket

import com.ail.lib_network.http.annotations.INetLogger

object WebSocketLogger {
    private const val BASE_TAG = "WSClient"
    private var enableLog: Boolean = false
    private var customLogger: INetLogger? = null

    fun setLogger(l: INetLogger?) {
        customLogger = l
    }

    fun setLogEnabled(enabled: Boolean) {
        enableLog = enabled
    }

    fun d(connectionId: String, message: String) {
        if (enableLog) {
            customLogger?.d(composeTag(connectionId), message)
        }
    }

    fun i(connectionId: String, message: String) {
        if (enableLog) {
            customLogger?.d(composeTag(connectionId), message)
        }
    }

    fun w(connectionId: String, message: String, throwable: Throwable? = null) {
        if (enableLog) {
            customLogger?.e(composeTag(connectionId), message, throwable)
        }
    }

    fun e(connectionId: String, message: String, throwable: Throwable? = null) {
        customLogger?.e(composeTag(connectionId), message, throwable)
    }

    private fun composeTag(connectionId: String): String {
        val fullTag = "$BASE_TAG[$connectionId]"
        return if (fullTag.length > 23) {
            "$BASE_TAG[..${connectionId.takeLast(10)}]"
        } else {
            fullTag
        }
    }
}