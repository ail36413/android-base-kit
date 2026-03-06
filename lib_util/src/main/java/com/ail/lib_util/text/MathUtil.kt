package com.ail.lib_util.text

import kotlin.math.abs

/** 常见数学计算工具。 */
object MathUtil {

    /** 计算最大公约数。 */
    fun gcd(a: Long, b: Long): Long {
        var x = abs(a)
        var y = abs(b)
        while (y != 0L) {
            val t = x % y
            x = y
            y = t
        }
        return x
    }

    /** 计算最小公倍数。 */
    fun lcm(a: Long, b: Long): Long {
        if (a == 0L || b == 0L) return 0L
        return abs(a / gcd(a, b) * b)
    }

    /** 是否为偶数。 */
    fun isEven(value: Long): Boolean = (value and 1L) == 0L

    /**
     * 计算百分比并按小数位四舍五入。
     */
    fun percentage(part: Double, total: Double, scale: Int = 2): Double {
        if (total == 0.0) return 0.0
        return NumberUtil.round(part / total * 100.0, scale)
    }
}
