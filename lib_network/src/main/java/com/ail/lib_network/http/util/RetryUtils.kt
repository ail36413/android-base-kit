package com.ail.lib_network.http.util

import kotlinx.coroutines.delay
import kotlin.math.min
import kotlin.math.pow
import kotlin.random.Random

/**
 * Suspend helper to retry a suspend block with exponential backoff and jitter.
 * Caller provides shouldRetry to determine whether to retry based on Throwable.
 */
suspend fun <T> retryWithBackoff(
    maxAttempts: Int = 3,
    initialDelayMillis: Long = 300,
    maxDelayMillis: Long = 5_000,
    factor: Double = 2.0,
    shouldRetry: (Throwable) -> Boolean = { true },
    block: suspend () -> T
): T {
    var attempt = 0
    var lastError: Throwable? = null
    while (attempt < maxAttempts) {
        try {
            return block()
        } catch (t: Throwable) {
            lastError = t
            if (!shouldRetry(t) || attempt + 1 >= maxAttempts) break
            val backoff = computeDelay(initialDelayMillis, maxDelayMillis, factor, attempt)
            val jitter = Random.nextLong(0, (initialDelayMillis / 2).coerceAtLeast(1))
            delay(backoff + jitter)
        }
        attempt++
    }
    throw lastError ?: IllegalStateException("retryWithBackoff failed without exception")
}

private fun computeDelay(initial: Long, max: Long, factor: Double, attempt: Int): Long {
    val raw = initial * factor.pow(attempt.toDouble())
    return min(max, raw.toLong())
}
