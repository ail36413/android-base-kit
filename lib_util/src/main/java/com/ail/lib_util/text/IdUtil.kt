package com.ail.lib_util.text

import java.util.UUID
import java.util.concurrent.atomic.AtomicLong

/** 唯一标识生成工具。 */
object IdUtil {

    private const val DEFAULT_SHORT_LENGTH = 12
    private val localCounter = AtomicLong(0L)

    /**
     * 生成 UUID。
     *
     * @param withDash true 返回标准 UUID；false 返回去掉 `-` 的紧凑形式。
     */
    fun uuid(withDash: Boolean = false): String {
        val raw = UUID.randomUUID().toString()
        return if (withDash) raw else raw.replace("-", "")
    }

    /** 生成短 ID（默认 12 位）。 */
    fun shortId(length: Int = DEFAULT_SHORT_LENGTH): String {
        val safeLength = length.coerceIn(4, 64)
        return uuid(withDash = false).take(safeLength)
    }

    /**
     * 生成时间序列 ID。
     *
     * @param prefix 前缀。
     * @param suffixCounter 是否追加自增尾巴提升同毫秒内唯一性。
     */
    fun timeBasedId(prefix: String = "", suffixCounter: Boolean = true): String {
        val millis = System.currentTimeMillis()
        if (!suffixCounter) return "$prefix$millis"
        val tail = localCounter.incrementAndGet().toString(36)
        return "$prefix${millis}_$tail"
    }
}
