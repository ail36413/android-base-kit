package com.bohai.util

import com.ail.lib_network.http.model.NetworkResult
import com.ail.lib_network.http.exception.RequestException
import com.ail.lib_network.http.auth.TokenProvider
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Helper to handle business-level token-expired responses (200 + body { code: expiredCode }).
 * Wrap network calls that return NetworkResult<T> and, upon business failure with expired code,
 * perform a single global refresh and retry once.
 */
object TokenRefreshHelper {
    private val refreshMutex = Mutex()

    suspend fun <T> withAppLevelRefresh(
        expiredBusinessCode: Int = 401,
        tokenProvider: TokenProvider,
        call: suspend () -> NetworkResult<T>
    ): NetworkResult<T> {
        val first = call()
        if (first is NetworkResult.Success) return first
        if (first is NetworkResult.BusinessFailure && first.code == expiredBusinessCode) {
            return refreshMutex.withLock {
                // maybe another coroutine already refreshed; try once more
                val afterOther = call()
                if (afterOther is NetworkResult.Success) return@withLock afterOther

                val refreshed = try {
                    tokenProvider.refreshTokenSuspend()
                } catch (_: Throwable) {
                    false
                }
                if (!refreshed) return@withLock NetworkResult.TechnicalFailure(RequestException(-1, "refresh failed"))

                // retry once
                call()
            }
        }
        return first
    }
}
