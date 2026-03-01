package com.ail.lib_network.http.model

import com.ail.lib_network.http.exception.BaseNetException

/**
 * 快捷处理扩展：成功回调
 */
inline fun <T> NetworkResult<T>.onSuccess(action: (T?) -> Unit): NetworkResult<T> {
    if (this is NetworkResult.Success) action(data)
    return this
}

/**
 * 快捷处理扩展：技术失败回调
 */
inline fun <T> NetworkResult<T>.onTechnicalFailure(action: (BaseNetException) -> Unit): NetworkResult<T> {
    if (this is NetworkResult.TechnicalFailure) action(exception)
    return this
}

/**
 * 快捷处理扩展：业务失败回调
 */
inline fun <T> NetworkResult<T>.onBusinessFailure(action: (code: Int, msg: String) -> Unit): NetworkResult<T> {
    if (this is NetworkResult.BusinessFailure) action(code, msg)
    return this
}

/**
 * 快捷处理扩展：任意失败（技术或业务）回调
 */
inline fun <T> NetworkResult<T>.onFailure(action: (failure: Any) -> Unit): NetworkResult<T> {
    when (this) {
        is NetworkResult.TechnicalFailure -> action(exception)
        is NetworkResult.BusinessFailure -> action(this)
        is NetworkResult.Success -> {}
    }
    return this
}

/**
 * 转换器：将 NetworkResult<T> 转换为另一种类型 NetworkResult<R>
 */
inline fun <T, R> NetworkResult<T>.map(transform: (T?) -> R): NetworkResult<R> {
    return when (this) {
        is NetworkResult.Success -> NetworkResult.Success(transform(data))
        is NetworkResult.TechnicalFailure -> NetworkResult.TechnicalFailure(exception)
        is NetworkResult.BusinessFailure -> NetworkResult.BusinessFailure(code, msg)
    }
}

/**
 * 折叠：统一处理三种结果
 */
inline fun <T, R> NetworkResult<T>.fold(
    onSuccess: (T?) -> R,
    onTechnicalFailure: (BaseNetException) -> R,
    onBusinessFailure: (code: Int, msg: String) -> R
): R {
    return when (this) {
        is NetworkResult.Success -> onSuccess(data)
        is NetworkResult.TechnicalFailure -> onTechnicalFailure(exception)
        is NetworkResult.BusinessFailure -> onBusinessFailure(code, msg)
    }
}

/**
 * 辅助方法：仅在成功时返回数据，否则返回 null。
 */
inline fun <T> NetworkResult<T>.getOrNull(): T? {
    return when (this) {
        is NetworkResult.Success -> data
        is NetworkResult.TechnicalFailure,
        is NetworkResult.BusinessFailure -> null
    }
}

/**
 * 辅助方法：成功时返回数据（可能为 null），失败时抛出异常。
 *
 * - TechnicalFailure：直接抛出内部的 [BaseNetException]。
 * - BusinessFailure：抛出 IllegalStateException，携带业务 code/msg。
 */
inline fun <T> NetworkResult<T>.getOrThrow(): T? {
    return when (this) {
        is NetworkResult.Success -> data
        is NetworkResult.TechnicalFailure -> throw exception
        is NetworkResult.BusinessFailure -> throw IllegalStateException(
            "Business failure, code=$code, msg=$msg"
        )
    }
}

/**
 * 辅助方法：成功返回数据，若数据为 null 或失败则返回给定默认值。
 */
inline fun <T> NetworkResult<T>.getOrDefault(defaultValue: T): T {
    return when (this) {
        is NetworkResult.Success -> data ?: defaultValue
        is NetworkResult.TechnicalFailure,
        is NetworkResult.BusinessFailure -> defaultValue
    }
}

/**
 * 是否为成功结果。
 */
inline fun <T> NetworkResult<T>.isSuccess(): Boolean = this is NetworkResult.Success<T>
