package com.ail.lib_util.text

import kotlin.random.Random

/** 随机工具（整数、布尔、字符串）。 */
object RandomUtil {

    private val alphaNumeric = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"

    /** 获取 [minInclusive, maxExclusive) 区间随机 Int。 */
    fun nextInt(minInclusive: Int = 0, maxExclusive: Int = Int.MAX_VALUE): Int {
        if (maxExclusive <= minInclusive) return minInclusive
        return Random.nextInt(minInclusive, maxExclusive)
    }

    /** 获取 [minInclusive, maxExclusive) 区间随机 Long。 */
    fun nextLong(minInclusive: Long = 0L, maxExclusive: Long = Long.MAX_VALUE): Long {
        if (maxExclusive <= minInclusive) return minInclusive
        return Random.nextLong(minInclusive, maxExclusive)
    }

    /** 获取随机布尔值。 */
    fun nextBoolean(): Boolean = Random.nextBoolean()

    /**
     * 生成随机字符串。
     *
     * @param length 目标长度。
     * @param chars 候选字符集。
     */
    fun randomString(length: Int, chars: String = alphaNumeric): String {
        if (length <= 0 || chars.isEmpty()) return ""
        return buildString(length) {
            repeat(length) {
                append(chars[Random.nextInt(chars.length)])
            }
        }
    }
}
