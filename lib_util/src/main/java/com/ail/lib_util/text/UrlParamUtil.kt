package com.ail.lib_util.text

import java.net.URLDecoder
import java.net.URLEncoder

/** URL Query 参数构建与解析工具。 */
object UrlParamUtil {

    private const val UTF8 = "UTF-8"

    /**
     * 将 [params] 追加到 [baseUrl] 后。
     *
     * @param baseUrl 可为纯路径或已带 query 的 URL。
     * @param params 待追加参数，value 会转为字符串后编码。
     */
    fun build(baseUrl: String, params: Map<String, Any?>): String {
        if (params.isEmpty()) return baseUrl
        val query = params.entries.joinToString("&") { (key, value) ->
            "${encode(key)}=${encode(value?.toString().orEmpty())}"
        }
        val separator = if (baseUrl.contains("?")) "&" else "?"
        return baseUrl + separator + query
    }

    /**
     * 解析 URL 或 query 字符串为键值对。
     */
    fun parse(urlOrQuery: String): Map<String, String> {
        if (urlOrQuery.isBlank()) return emptyMap()
        val query = urlOrQuery.substringAfter('?', urlOrQuery).substringBefore('#')
        if (query.isBlank()) return emptyMap()

        val result = linkedMapOf<String, String>()
        query.split('&').forEach { pair ->
            if (pair.isBlank()) return@forEach
            val key = pair.substringBefore('=')
            val value = pair.substringAfter('=', "")
            result[decode(key)] = decode(value)
        }
        return result
    }

    /** 便捷追加单个参数。 */
    fun append(url: String, key: String, value: Any?): String {
        return build(url, mapOf(key to value))
    }

    private fun encode(text: String): String {
        return runCatching { URLEncoder.encode(text, UTF8) }.getOrDefault(text)
    }

    private fun decode(text: String): String {
        return runCatching { URLDecoder.decode(text, UTF8) }.getOrDefault(text)
    }
}
