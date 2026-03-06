package com.ail.lib_util.text

/** 脱敏工具（手机号、邮箱、证件号）。 */
object MaskUtil {

    /** 手机号脱敏：保留前 3 位与后 4 位。 */
    fun maskPhone(phone: String?): String {
        val raw = phone.orEmpty()
        if (raw.length < 7) return raw
        return raw.replaceRange(3, raw.length - 4, "****")
    }

    /** 邮箱脱敏：保留用户名首字符。 */
    fun maskEmail(email: String?): String {
        val raw = email.orEmpty()
        val at = raw.indexOf('@')
        if (at <= 1) return raw
        val prefix = raw.substring(0, at)
        val domain = raw.substring(at)
        val keep = prefix.take(1)
        return keep + "***" + domain
    }

    /**
     * 证件号脱敏。
     *
     * @param keepPrefix 前缀保留位数。
     * @param keepSuffix 后缀保留位数。
     */
    fun maskIdCard(id: String?, keepPrefix: Int = 3, keepSuffix: Int = 4): String {
        val raw = id.orEmpty()
        if (raw.length <= keepPrefix + keepSuffix) return raw
        val stars = "*".repeat(raw.length - keepPrefix - keepSuffix)
        return raw.take(keepPrefix) + stars + raw.takeLast(keepSuffix)
    }
}
