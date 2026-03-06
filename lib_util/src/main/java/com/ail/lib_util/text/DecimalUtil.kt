package com.ail.lib_util.text

import java.math.BigDecimal
import java.math.RoundingMode

/** 基于 BigDecimal 的精确运算工具。 */
object DecimalUtil {

    /** 精确加法。 */
    fun add(a: String?, b: String?, scale: Int = 2): String {
        val result = toBigDecimal(a).add(toBigDecimal(b))
        return format(result, scale)
    }

    /** 精确减法。 */
    fun subtract(a: String?, b: String?, scale: Int = 2): String {
        val result = toBigDecimal(a).subtract(toBigDecimal(b))
        return format(result, scale)
    }

    /** 精确乘法。 */
    fun multiply(a: String?, b: String?, scale: Int = 2): String {
        val result = toBigDecimal(a).multiply(toBigDecimal(b))
        return format(result, scale)
    }

    /**
     * 精确除法，除数为 0 时返回 "0"。
     */
    fun divide(a: String?, b: String?, scale: Int = 2): String {
        val divisor = toBigDecimal(b)
        if (divisor.compareTo(BigDecimal.ZERO) == 0) return "0"
        val result = toBigDecimal(a).divide(divisor, scale.coerceAtLeast(0), RoundingMode.HALF_UP)
        return result.stripTrailingZeros().toPlainString()
    }

    /** 比较两个数值大小。 */
    fun compare(a: String?, b: String?): Int {
        return toBigDecimal(a).compareTo(toBigDecimal(b))
    }

    private fun toBigDecimal(text: String?): BigDecimal {
        return text?.trim()?.toBigDecimalOrNull() ?: BigDecimal.ZERO
    }

    private fun format(value: BigDecimal, scale: Int): String {
        val safeScale = scale.coerceAtLeast(0)
        return value.setScale(safeScale, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()
    }
}
