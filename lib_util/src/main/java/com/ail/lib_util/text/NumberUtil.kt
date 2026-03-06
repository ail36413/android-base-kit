package com.ail.lib_util.text

import java.math.BigDecimal
import java.math.RoundingMode

/** 数值解析与格式化工具。 */
object NumberUtil {

    /** 字符串转 Int，失败返回 [defaultValue]。 */
    fun parseInt(text: String?, defaultValue: Int = 0): Int = text?.trim()?.toIntOrNull() ?: defaultValue

    /** 字符串转 Long，失败返回 [defaultValue]。 */
    fun parseLong(text: String?, defaultValue: Long = 0L): Long = text?.trim()?.toLongOrNull() ?: defaultValue

    /** 字符串转 Double，失败返回 [defaultValue]。 */
    fun parseDouble(text: String?, defaultValue: Double = 0.0): Double = text?.trim()?.toDoubleOrNull() ?: defaultValue

    /** 限制值在 [min, max] 内。 */
    fun clamp(value: Int, min: Int, max: Int): Int {
        if (min > max) return value
        return value.coerceIn(min, max)
    }

    /** 四舍五入到指定小数位。 */
    fun round(value: Double, scale: Int): Double {
        if (scale < 0) return value
        return BigDecimal.valueOf(value).setScale(scale, RoundingMode.HALF_UP).toDouble()
    }

    /** 格式化小数位并可选移除尾随 0。 */
    fun formatDecimal(value: Double, scale: Int, stripTrailingZeros: Boolean = true): String {
        if (scale < 0) return value.toString()
        val decimal = BigDecimal.valueOf(value).setScale(scale, RoundingMode.HALF_UP)
        return if (stripTrailingZeros) decimal.stripTrailingZeros().toPlainString() else decimal.toPlainString()
    }
}
