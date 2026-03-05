package com.ail.android_base_kit.network.http.model

import com.ail.lib_network.http.model.IBaseResponse
import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

/**
 * 示例返回模型：兼容标准 {status, rawCode, msg}，并优先从 httpbin 的 {json} 读取业务数据。
 */
data class StatusResponse<T>(
    @SerializedName("status")
    val status: Boolean = true,
    @SerializedName("rawCode")
    val rawCode: Int = 0,
    @SerializedName("msg")
    override val msg: String = "ok",
    // httpbin 的 data 字段常为字符串，这里仅占位接收，避免按 T 反序列化导致解析异常。
    @SerializedName("data")
    private val rawData: JsonElement? = null,
    @SerializedName("json")
    private val jsonPayload: T? = null,
    // 供本地构造 demo 用
    private val localData: T? = null,
    val successCode: Int = 0
) : IBaseResponse<T> {
    override val data: T?
        get() = localData ?: jsonPayload

    override val code: Int
        get() = if (status) successCode else rawCode
}
