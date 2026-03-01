package com.ail.lib_network.http.interceptor

import com.ail.lib_network.http.annotations.INetLogger
import org.json.JSONArray
import org.json.JSONObject

/**
 * 漂亮的日志打印器
 * 职责：将 OkHttp 的原始日志美化为易读的格式，并交给 [INetLogger] 输出
 */
class PrettyNetLogger(private val netLogger: INetLogger) : okhttp3.logging.HttpLoggingInterceptor.Logger {

    companion object {
        private const val TAG = "NetworkLog"
        private const val JSON_INDENT = 4
        private const val MAX_LOG_LENGTH = 4000
    }

    override fun log(message: String) {
        val trimmedMessage = message.trim()
        if (trimmedMessage.startsWith("{") || trimmedMessage.startsWith("[")) {
            try {
                val prettyJson = if (trimmedMessage.startsWith("{")) {
                    JSONObject(trimmedMessage).toString(JSON_INDENT)
                } else {
                    JSONArray(trimmedMessage).toString(JSON_INDENT)
                }
                // JSON 日志一般为业务数据，这里只做格式化，不做脱敏，但仍然限制单行长度
                prettyJson.lines().forEach { line ->
                    netLogger.d(TAG, truncateIfTooLong(line))
                }
            } catch (e: Exception) {
                netLogger.d(TAG, maskAndTruncate(message))
            }
        } else {
            netLogger.d(TAG, maskAndTruncate(message))
        }
    }

    /**
     * 对日志做两件事：
     * 1. 脱敏：对常见敏感 Header（Authorization/Cookie/token 等）进行掩码处理；
     * 2. 截断：对超长日志做截断，避免占用过多日志缓冲区。
     */
    private fun maskAndTruncate(raw: String): String {
        val masked = maskSensitiveHeader(raw)
        return truncateIfTooLong(masked)
    }

    private fun maskSensitiveHeader(message: String): String {
        val lower = message.lowercase()
        val sensitivePrefixes = listOf(
            "authorization:",
            "cookie:",
            "set-cookie:",
            "x-auth-token:",
            "token:"
        )
        val matched = sensitivePrefixes.firstOrNull { lower.startsWith(it) } ?: return message
        val index = message.indexOf(':')
        if (index == -1) return "****(masked)"
        val name = message.substring(0, index)
        return "$name: ****(masked)"
    }

    private fun truncateIfTooLong(message: String): String {
        return if (message.length > MAX_LOG_LENGTH) {
            message.substring(0, MAX_LOG_LENGTH) + " ... (truncated)"
        } else {
            message
        }
    }
}
