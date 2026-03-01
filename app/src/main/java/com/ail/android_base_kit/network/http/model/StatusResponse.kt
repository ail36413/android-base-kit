package com.bohai.android_base_kit.model

import com.ail.lib_network.http.model.IBaseResponse

/**
 * 示例：默认返回格式为 status + code + msg + data 的响应包装。
 *
 * 约定：
 * - status=true 视为成功，code 统一映射为 successCode（默认 0）
 * - status=false 视为失败，使用 rawCode
 */
data class StatusResponse<T>(
    val status: Boolean,
    val rawCode: Int,
    override val msg: String,
    override val data: T?,
    val successCode: Int = 0
) : IBaseResponse<T> {
    override val code: Int
        get() = if (status) successCode else rawCode
}

