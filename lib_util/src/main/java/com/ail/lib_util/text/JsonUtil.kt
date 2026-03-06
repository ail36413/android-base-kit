package com.ail.lib_util.text

import org.json.JSONArray
import org.json.JSONObject

/** JSON 构建、解析与读取工具。 */
object JsonUtil {

    /** Map 转 JSON 字符串。 */
    fun toJson(map: Map<String, Any?>): String {
        return JSONObject(map).toString()
    }

    /** 美化输出 JSON，失败返回原文。 */
    fun pretty(json: String): String {
        if (json.isBlank()) return ""
        return runCatching {
            val source = json.trim()
            if (source.startsWith("[")) {
                JSONArray(source).toString(2)
            } else {
                JSONObject(source).toString(2)
            }
        }.getOrDefault(json)
    }

    /** 压缩输出 JSON，失败返回原文。 */
    fun compact(json: String): String {
        if (json.isBlank()) return ""
        return runCatching {
            val source = json.trim()
            if (source.startsWith("[")) {
                JSONArray(source).toString()
            } else {
                JSONObject(source).toString()
            }
        }.getOrDefault(json)
    }

    /** 解析 JSONObject，失败返回 null。 */
    fun parseObject(json: String?): JSONObject? {
        if (json.isNullOrBlank()) return null
        return runCatching { JSONObject(json) }.getOrNull()
    }

    /** 解析 JSONArray，失败返回 null。 */
    fun parseArray(json: String?): JSONArray? {
        if (json.isNullOrBlank()) return null
        return runCatching { JSONArray(json) }.getOrNull()
    }

    /** 安全读取字符串字段。 */
    fun optString(json: String?, key: String, default: String = ""): String {
        return parseObject(json)?.optString(key, default) ?: default
    }

    /** 安全读取整数字段。 */
    fun optInt(json: String?, key: String, default: Int = 0): Int {
        return parseObject(json)?.optInt(key, default) ?: default
    }

    /** 安全读取长整数字段。 */
    fun optLong(json: String?, key: String, default: Long = 0L): Long {
        return parseObject(json)?.optLong(key, default) ?: default
    }

    /** 安全读取布尔字段。 */
    fun optBoolean(json: String?, key: String, default: Boolean = false): Boolean {
        return parseObject(json)?.optBoolean(key, default) ?: default
    }

    /** 安全读取浮点数字段。 */
    fun optDouble(json: String?, key: String, default: Double = 0.0): Double {
        return parseObject(json)?.optDouble(key, default) ?: default
    }

    /** 是否是有效 JSONObject 字符串。 */
    fun isObject(json: String?): Boolean = parseObject(json) != null

    /** 是否是有效 JSONArray 字符串。 */
    fun isArray(json: String?): Boolean = parseArray(json) != null

    /** 安全读取嵌套 JSONObject。 */
    fun optObject(json: String?, key: String): JSONObject? {
        return parseObject(json)?.optJSONObject(key)
    }

    /** 安全读取嵌套 JSONArray。 */
    fun optArray(json: String?, key: String): JSONArray? {
        return parseObject(json)?.optJSONArray(key)
    }
}
