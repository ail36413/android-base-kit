package com.ail.android_base_kit.network.http.model

import com.ail.lib_network.http.model.IBaseResponse

/**
 * Project-level response wrapper that implements IBaseResponse so it can be returned by Retrofit.
 */
data class DemoResponse<T>(
    override val code: Int,
    override val msg: String,
    override val data: T?
) : IBaseResponse<T>
