package com.ail.lib_util.text

/** 正则匹配工具。 */
object RegexUtil {

    /** 是否匹配成功。 */
    fun isMatch(text: CharSequence?, pattern: String): Boolean {
        if (text.isNullOrEmpty() || pattern.isBlank()) return false
        return runCatching { Regex(pattern).containsMatchIn(text) }.getOrDefault(false)
    }

    /** 提取首个匹配项，失败返回 [defaultValue]。 */
    fun findFirst(text: CharSequence?, pattern: String, defaultValue: String = ""): String {
        if (text.isNullOrEmpty() || pattern.isBlank()) return defaultValue
        return runCatching { Regex(pattern).find(text)?.value.orEmpty() }.getOrDefault(defaultValue)
    }

    /** 提取全部匹配项。 */
    fun findAll(text: CharSequence?, pattern: String): List<String> {
        if (text.isNullOrEmpty() || pattern.isBlank()) return emptyList()
        return runCatching { Regex(pattern).findAll(text).map { it.value }.toList() }.getOrDefault(emptyList())
    }

    /** 替换匹配文本。 */
    fun replace(text: CharSequence?, pattern: String, replacement: String): String {
        if (text.isNullOrEmpty() || pattern.isBlank()) return text?.toString().orEmpty()
        return runCatching { Regex(pattern).replace(text, replacement) }.getOrDefault(text.toString())
    }

    /** 按正则分割文本，自动过滤空项。 */
    fun split(text: CharSequence?, pattern: String, limit: Int = 0): List<String> {
        if (text.isNullOrEmpty()) return emptyList()
        if (pattern.isBlank()) return listOf(text.toString())
        return runCatching { Regex(pattern).split(text, limit).filter { it.isNotEmpty() } }.getOrDefault(emptyList())
    }
}
