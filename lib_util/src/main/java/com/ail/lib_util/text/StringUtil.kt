package com.ail.lib_util.text

/** 字符串常用处理工具。 */
object StringUtil {

    /** 是否为 null 或空白文本。 */
    fun isBlank(text: String?): Boolean = text.isNullOrBlank()

    /** 去除首尾空白；null 返回空串。 */
    fun orEmptyTrim(text: String?): String = text?.trim().orEmpty()

    /** 空白文本转 null，非空返回 trim 后结果。 */
    fun nullIfBlank(text: String?): String? = text?.trim()?.takeIf { it.isNotEmpty() }

    /** 忽略大小写比较。 */
    fun equalsIgnoreCase(a: String?, b: String?): Boolean {
        return a?.equals(b, ignoreCase = true) ?: (b == null)
    }

    /**
     * 文本截断并追加后缀。
     *
     * @param maxLength 结果最大长度。
     * @param suffix 截断后追加的后缀。
     */
    fun ellipsize(text: String?, maxLength: Int, suffix: String = "..."): String {
        val raw = text.orEmpty()
        if (maxLength <= 0 || raw.length <= maxLength) return raw
        if (suffix.length >= maxLength) return raw.take(maxLength)
        return raw.take(maxLength - suffix.length) + suffix
    }
}
