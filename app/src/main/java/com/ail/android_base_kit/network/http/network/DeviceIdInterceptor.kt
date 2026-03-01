package com.bohai.network

import okhttp3.Interceptor
import okhttp3.Response

/**
 * 示例：项目层自定义拦截器 - 为请求添加设备/渠道标识
 * Key 越大越靠后执行（在 AuthToken 之后）
 */
class DeviceIdInterceptor(
    private val deviceId: String
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("X-Device-Id", deviceId)
            .build()
        return chain.proceed(request)
    }
}
