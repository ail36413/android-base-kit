package com.ail.lib_util.time

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** 时间格式化与解析工具。 */
object DateTimeUtil {

    /** 当前时间戳（毫秒）。 */
    fun nowMillis(): Long = System.currentTimeMillis()

    /**
     * 格式化时间戳。
     *
     * @param millis 时间戳（毫秒）。
     * @param pattern 时间格式模板。
     */
    fun format(
        millis: Long = nowMillis(),
        pattern: String = "yyyy-MM-dd HH:mm:ss",
        locale: Locale = Locale.getDefault(),
    ): String {
        return runCatching {
            val sdf = SimpleDateFormat(pattern, locale)
            sdf.format(Date(millis))
        }.getOrDefault("")
    }

    /**
     * 解析时间文本为时间戳。
     *
     * @return 解析失败返回 null。
     */
    fun parse(
        text: String,
        pattern: String = "yyyy-MM-dd HH:mm:ss",
        locale: Locale = Locale.getDefault(),
    ): Long? {
        return parseInternal(text, pattern, locale, lenient = true)
    }

    /**
     * 严格解析时间文本为时间戳。
     *
     * 与 [parse] 的区别是会关闭宽松解析，非法日期会直接返回 null。
     */
    fun parseStrict(
        text: String,
        pattern: String = "yyyy-MM-dd HH:mm:ss",
        locale: Locale = Locale.getDefault(),
    ): Long? {
        return parseInternal(text, pattern, locale, lenient = false)
    }

    private fun parseInternal(
        text: String,
        pattern: String,
        locale: Locale,
        lenient: Boolean,
    ): Long? {
        if (text.isBlank()) return null
        return runCatching {
            val sdf = SimpleDateFormat(pattern, locale)
            sdf.isLenient = lenient
            sdf.parse(text)?.time
        }.getOrNull()
    }

    /** 判断目标时间是否属于今天。 */
    fun isToday(millis: Long): Boolean {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply { timeInMillis = millis }
        return now.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
            now.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)
    }
}
