package com.ail.lib_network.websocket

/**
 * WebSocket 内部日志工具
 * 支持开关控制和自定义日志实现
 */
object WebSocketLogger {
    private const val BASE_TAG = "WSClient"
    private var enableLog: Boolean = false
    private var customLogger: IWebSocketLogger? = null

    /**
     * 设置自定义日志实现
     */
    fun setLogger(l: IWebSocketLogger?) {
        customLogger = l
    }

    /**
     * 设置日志开关
     */
    fun setLogEnabled(enabled: Boolean) {
        enableLog = enabled
    }

    /**
     * 输出调试日志（受日志开关控制）
     */
    fun d(connectionId: String, message: String) {
        if (enableLog) {
            customLogger?.d(composeTag(connectionId), message)
        }
    }

    /**
     * 输出信息日志（受日志开关控制）
     */
    fun i(connectionId: String, message: String) {
        if (enableLog) {
            customLogger?.d(composeTag(connectionId), message)
        }
    }

    /**
     * 输出警告日志（受日志开关控制）
     */
    fun w(connectionId: String, message: String, throwable: Throwable? = null) {
        if (enableLog) {
            customLogger?.e(composeTag(connectionId), message, throwable)
        }
    }

    /**
     * 输出错误日志（不受日志开关控制，始终输出）
     */
    fun e(connectionId: String, message: String, throwable: Throwable? = null) {
        customLogger?.e(composeTag(connectionId), message, throwable)
    }

    /**
     * 组合日志标签，处理超长标签
     */
    private fun composeTag(connectionId: String): String {
        val fullTag = "$BASE_TAG[$connectionId]"
        return if (fullTag.length > 23) {
            "$BASE_TAG[..${connectionId.takeLast(10)}]"
        } else {
            fullTag
        }
    }
}