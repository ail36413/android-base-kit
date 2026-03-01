package com.ail.lib_network.http.util

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

/**
 * OkHttp interceptor that retries requests using a configurable [RetryStrategy].
 */
class RetryInterceptor(
    private val strategy: RetryStrategy = DefaultRetryStrategy()
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()
        var attempt = 0
        var lastException: IOException? = null

        while (true) {
            try {
                val response = chain.proceed(request)
                if (!strategy.shouldRetry(request, response, null, attempt)) {
                    return response
                }
                response.close()
            } catch (ioe: IOException) {
                lastException = ioe
                if (!strategy.shouldRetry(request, null, ioe, attempt)) break
            }

            val backoff = strategy.nextDelayMillis(attempt)
            try {
                Thread.sleep(backoff)
            } catch (_: InterruptedException) {
                Thread.currentThread().interrupt()
                break
            }
            attempt++
        }

        lastException?.let { throw it }
        return chain.proceed(request)
    }
}
