package com.ail.lib_network.http.util

import com.ail.lib_network.http.annotations.NetworkConfigProvider
import com.ail.lib_network.http.auth.TokenProvider
import com.ail.lib_network.http.auth.UnauthorizedHandler
import com.ail.lib_network.http.exception.ExceptionHandle
import com.ail.lib_network.http.model.IBaseResponse
import com.ail.lib_network.http.model.NetEvent
import com.ail.lib_network.http.model.NetEventStage
import com.ail.lib_network.http.model.NetworkResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Optional
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 负责普通请求的执行（请求/原始请求）
 */
@Singleton
class RequestExecutor @Inject constructor(
    private val configProvider: NetworkConfigProvider,
    private val tokenProviderOptional: Optional<TokenProvider>,
    private val unauthorizedHandlerOptional: Optional<UnauthorizedHandler>
) {

    // Mutex to ensure only one coroutine performs token refresh at a time
    private val refreshMutex = Mutex()

    suspend fun <T> executeRequest(
        successCode: Int? = null,
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
        tag: String? = null,
        call: suspend () -> IBaseResponse<T>
    ): NetworkResult<T> {
        val start = System.currentTimeMillis()
        NetTracker.track(
            NetEvent(
                name = "executeRequest",
                stage = NetEventStage.START,
                timestampMs = start,
                tag = tag
            )
        )
        val result = withContext(dispatcher) {
            try {
                val response = call()
                val effectiveSuccessCode = successCode ?: configProvider.current.defaultSuccessCode
                if (response.code == effectiveSuccessCode) {
                    NetworkResult.Success(response.data)
                } else {
                    NetworkResult.BusinessFailure(response.code, response.msg)
                }
            } catch (e: Exception) {
                NetworkResult.TechnicalFailure(ExceptionHandle.handleException(e))
            }
        }

        // If business failure indicates expired token (code==401), handle refresh/retry or notify unauthorized
        val finalResult = if (result is NetworkResult.BusinessFailure && result.code == 401) {
            if (!tokenProviderOptional.isPresent) {
                // no TokenProvider configured: notify unauthorized (if provided) and return original business failure
                try {
                    if (unauthorizedHandlerOptional.isPresent) {
                        // fire-and-forget
                        unauthorizedHandlerOptional.get().onUnauthorized()
                    }
                } catch (_: Exception) {}
                result
            } else {
                // token provider is present: attempt serialized refresh + retry
                refreshMutex.withLock {
                    // maybe another coroutine already refreshed; try the call once more
                    var secondAttemptException: Exception? = null
                    val secondAttempt = try {
                        withContext(dispatcher) { call() }
                    } catch (e: Exception) {
                        secondAttemptException = e
                        null
                    }

                    if (secondAttempt != null) {
                        val effectiveSuccessCode = successCode ?: configProvider.current.defaultSuccessCode
                        if (secondAttempt.code == effectiveSuccessCode) {
                            NetworkResult.Success(secondAttempt.data)
                        } else if (secondAttempt.code != 401) {
                            // business failure but not token-expired anymore
                            NetworkResult.BusinessFailure(secondAttempt.code, secondAttempt.msg)
                        } else {
                            // still 401 -> try refresh
                            val refreshed = try {
                                tokenProviderOptional.get().refreshTokenSuspend()
                            } catch (_: Throwable) {
                                false
                            }
                            if (!refreshed) {
                                try {
                                    if (unauthorizedHandlerOptional.isPresent) {
                                        unauthorizedHandlerOptional.get().onUnauthorized()
                                    }
                                } catch (_: Exception) {}
                                NetworkResult.BusinessFailure(result.code, result.msg)
                            } else {
                                // retry once after refresh
                                try {
                                    val afterRefresh = withContext(dispatcher) { call() }
                                    val effective = successCode ?: configProvider.current.defaultSuccessCode
                                    if (afterRefresh.code == effective) {
                                        NetworkResult.Success(afterRefresh.data)
                                    } else {
                                        NetworkResult.BusinessFailure(afterRefresh.code, afterRefresh.msg)
                                    }
                                } catch (e: Exception) {
                                    NetworkResult.TechnicalFailure(ExceptionHandle.handleException(e))
                                }
                            }
                        }
                    } else {
                        // secondAttempt failed with exception, preserve original context
                        NetworkResult.TechnicalFailure(
                            ExceptionHandle.handleException(secondAttemptException ?: Throwable("call failed during retry"))
                        )
                    }
                }
            }
        } else {
            result
        }

        val end = System.currentTimeMillis()
        val duration = end - start
        val (type, errorCode) = when (finalResult) {
            is NetworkResult.Success -> "SUCCESS" to null
            is NetworkResult.TechnicalFailure -> "TECHNICAL_FAILURE" to finalResult.exception.code
            is NetworkResult.BusinessFailure -> "BUSINESS_FAILURE" to finalResult.code
        }
        NetTracker.track(
            NetEvent(
                name = "executeRequest",
                stage = NetEventStage.END,
                timestampMs = end,
                durationMs = duration,
                resultType = type,
                errorCode = errorCode,
                tag = tag
            )
        )
        return finalResult
    }

    suspend fun <T> executeRawRequest(
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
        tag: String? = null,
        call: suspend () -> T
    ): NetworkResult<T> {
        val start = System.currentTimeMillis()
        NetTracker.track(
            NetEvent(
                name = "executeRawRequest",
                stage = NetEventStage.START,
                timestampMs = start,
                tag = tag
            )
        )
        val result = withContext(dispatcher) {
            try {
                val response = call()
                NetworkResult.Success(response)
            } catch (e: Exception) {
                NetworkResult.TechnicalFailure(ExceptionHandle.handleException(e))
            }
        }
        val end = System.currentTimeMillis()
        val duration = end - start
        val (type, errorCode) = when (result) {
            is NetworkResult.Success -> "SUCCESS" to null
            is NetworkResult.TechnicalFailure -> "TECHNICAL_FAILURE" to result.exception.code
            is NetworkResult.BusinessFailure -> "BUSINESS_FAILURE" to result.code
        }
        NetTracker.track(
            NetEvent(
                name = "executeRawRequest",
                stage = NetEventStage.END,
                timestampMs = end,
                durationMs = duration,
                resultType = type,
                errorCode = errorCode,
                tag = tag
            )
        )
        return result
    }
}
