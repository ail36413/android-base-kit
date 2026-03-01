package com.ail.lib_network.http.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * Emit the result of [block] periodically. Stop when [stopWhen] returns true or after [maxAttempts].
 */
fun <T> pollingFlow(
    periodMillis: Long,
    maxAttempts: Long = Long.MAX_VALUE,
    stopWhen: suspend (T) -> Boolean = { false },
    block: suspend () -> T
): Flow<T> = flow {
    var count = 0L
    while (count < maxAttempts) {
        val value = block()
        emit(value)
        if (stopWhen(value)) break
        count++
        delay(periodMillis)
    }
}.flowOn(Dispatchers.IO)
