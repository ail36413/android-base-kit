package com.ail.lib_util.thread

/** 通用同步重试工具（支持间隔与退避）。 */
object RetryUtil {

    /** 重试配置。 */
    data class Config(
        /** 最大尝试次数，最小为 1。 */
        val maxAttempts: Int = 3,
        /** 首次重试前等待时长（毫秒）。 */
        val initialDelayMs: Long = 0L,
        /** 指数退避系数，>=1。 */
        val backoffFactor: Double = 1.0,
        /** 单次等待上限（毫秒）。 */
        val maxDelayMs: Long = 5_000L,
    ) {
        init {
            require(maxAttempts >= 1) { "maxAttempts must be >= 1" }
            require(initialDelayMs >= 0L) { "initialDelayMs must be >= 0" }
            require(backoffFactor >= 1.0) { "backoffFactor must be >= 1.0" }
            require(maxDelayMs >= 0L) { "maxDelayMs must be >= 0" }
        }
    }

    /**
     * 执行带重试的任务。
     *
     * @param block attempt 从 1 开始递增。
     */
    fun <T> retry(config: Config = Config(), block: (attempt: Int) -> T): T {
        var currentDelay = config.initialDelayMs
        var lastError: Throwable? = null

        for (attempt in 1..config.maxAttempts) {
            try {
                return block(attempt)
            } catch (t: Throwable) {
                lastError = t
                if (attempt == config.maxAttempts) break
                sleepSafely(currentDelay.coerceAtMost(config.maxDelayMs))
                currentDelay = nextDelay(currentDelay, config)
            }
        }

        throw lastError ?: IllegalStateException("retry failed without throwable")
    }

    /** 失败时返回 null，不抛异常。 */
    fun <T> retryOrNull(config: Config = Config(), block: (attempt: Int) -> T): T? {
        return runCatching { retry(config, block) }.getOrNull()
    }

    private fun nextDelay(currentDelay: Long, config: Config): Long {
        if (currentDelay <= 0L) return config.initialDelayMs.coerceAtMost(config.maxDelayMs)
        val next = (currentDelay * config.backoffFactor).toLong()
        return next.coerceAtMost(config.maxDelayMs)
    }

    private fun sleepSafely(delayMs: Long) {
        if (delayMs <= 0L) return
        runCatching { Thread.sleep(delayMs) }
    }
}
