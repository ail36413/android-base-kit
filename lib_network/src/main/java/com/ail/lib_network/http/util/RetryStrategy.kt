package com.ail.lib_network.http.util

import okhttp3.Request
import okhttp3.Response
import java.io.IOException

/**
 * Strategy interface to decide whether to retry and how long to wait.
 */
interface RetryStrategy {
    fun shouldRetry(request: Request, response: Response?, error: IOException?, attempt: Int): Boolean
    fun nextDelayMillis(attempt: Int): Long
}

/**
 * Default retry strategy: retry idempotent methods on IO errors or 5xx responses.
 */
class DefaultRetryStrategy(
    private val maxRetries: Int = 2,
    private val initialBackoffMillis: Long = 300,
    private val maxBackoffMillis: Long = 5_000,
    private val factor: Double = 2.0
) : RetryStrategy {

    private val idempotentMethods = setOf("GET", "HEAD", "PUT", "DELETE", "OPTIONS")

    override fun shouldRetry(request: Request, response: Response?, error: IOException?, attempt: Int): Boolean {
        if (attempt >= maxRetries) return false
        if (!idempotentMethods.contains(request.method.uppercase())) return false
        if (error != null) return true
        val code = response?.code ?: return false
        return code in 500..599
    }

    override fun nextDelayMillis(attempt: Int): Long {
        val raw = initialBackoffMillis * Math.pow(factor, attempt.toDouble())
        val delay = raw.toLong().coerceAtMost(maxBackoffMillis)
        return delay
    }
}
