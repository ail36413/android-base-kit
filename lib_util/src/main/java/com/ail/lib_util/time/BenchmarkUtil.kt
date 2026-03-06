package com.ail.lib_util.time

/** 轻量耗时测量工具。 */
object BenchmarkUtil {

    /** 单次测量结果。 */
    data class Result<T>(
        /** 执行结果值。 */
        val value: T,
        /** 执行耗时（毫秒）。 */
        val costMs: Long,
    )

    /** 测量 block 执行耗时。 */
    inline fun <T> measure(block: () -> T): Result<T> {
        val start = System.nanoTime()
        val value = block()
        val costMs = (System.nanoTime() - start) / 1_000_000
        return Result(value, costMs)
    }

    /**
     * 重复执行并返回总耗时。
     *
     * @param times 执行次数，<=0 返回 0。
     */
    inline fun repeatMeasure(times: Int, block: (index: Int) -> Unit): Long {
        if (times <= 0) return 0L
        val start = System.nanoTime()
        repeat(times) { index -> block(index) }
        return (System.nanoTime() - start) / 1_000_000
    }
}
