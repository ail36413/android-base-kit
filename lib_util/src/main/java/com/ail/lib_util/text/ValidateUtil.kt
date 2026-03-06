package com.ail.lib_util.text

import android.util.Patterns

/** 常见文本合法性校验工具。 */
object ValidateUtil {

    private val mobileCnRegex = Regex("^1[3-9]\\d{9}$")

    /** 是否是合法邮箱。 */
    fun isEmail(text: CharSequence?): Boolean {
        return !text.isNullOrBlank() && Patterns.EMAIL_ADDRESS.matcher(text).matches()
    }

    /** 是否是合法 URL。 */
    fun isUrl(text: CharSequence?): Boolean {
        return !text.isNullOrBlank() && Patterns.WEB_URL.matcher(text).matches()
    }

    /** 是否是中国大陆手机号。 */
    fun isMobileCN(text: CharSequence?): Boolean {
        return !text.isNullOrBlank() && mobileCnRegex.matches(text)
    }

    /** 是否是 IPv4 地址。 */
    fun isIpV4(text: CharSequence?): Boolean {
        if (text.isNullOrBlank()) return false
        val parts = text.split('.')
        if (parts.size != 4) return false
        return parts.all { part ->
            if (part.isBlank()) return@all false
            val value = part.toIntOrNull() ?: return@all false
            value in 0..255 && (part == "0" || !part.startsWith('0'))
        }
    }

    /**
     * 是否是强密码（至少包含大小写字母和数字）。
     *
     * @param minLen 最小长度。
     */
    fun isStrongPassword(text: CharSequence?, minLen: Int = 8): Boolean {
        if (text.isNullOrBlank() || text.length < minLen) return false
        val hasUpper = text.any { it.isUpperCase() }
        val hasLower = text.any { it.isLowerCase() }
        val hasDigit = text.any { it.isDigit() }
        return hasUpper && hasLower && hasDigit
    }
}
