package com.bohai.network

import android.util.Base64
import okhttp3.Interceptor
import okhttp3.Response

/**
 * 示例：项目层自定义拦截器 - 为请求自动添加 Token
 * Key 越小越先执行，基础库会按 Key 升序添加拦截器
 *
 * 说明：HTTP Header 值仅允许 ASCII 字符。若 token 含中文等非 ASCII 字符，
 * 会先做 Base64 编码再写入，避免 OkHttp 抛 IllegalArgumentException。
 * 若 token 本身为 JWT 等纯 ASCII，则原样传递。
 */
class AuthTokenInterceptor(
    private val getToken: () -> String?
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = getToken() ?: return chain.proceed(chain.request())
        if (token.isBlank()) return chain.proceed(chain.request())

        // Header 值仅允许 ASCII；非 ASCII（如中文）时 Base64 编码
        val value = if (token.all { it.code in 0x20..0x7E }) {
            "Bearer $token"
        } else {
            "Bearer " + Base64.encodeToString(token.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
        }
        val request = chain.request().newBuilder()
            .addHeader("Authorization", value)
            .build()
        return chain.proceed(request)
    }
}
