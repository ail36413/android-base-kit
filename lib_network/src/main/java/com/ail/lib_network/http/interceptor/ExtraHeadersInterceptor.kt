package com.ail.lib_network.http.interceptor

import com.ail.lib_network.http.annotations.NetworkConfig
import com.ail.lib_network.http.annotations.NetworkConfigProvider
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Response

/**
 * 将 `NetworkConfig.extraHeaders` 注入到每次请求的 Header 中。
 * 为了避免高并发下遍历变更的 map 引发并发问题，
 * 实现中对 current.extraHeaders 做了一次快照（toList）并缓存构建好的 Headers 对象以减少分配。
 */
class ExtraHeadersInterceptor(
    private val configProvider: NetworkConfigProvider
) : Interceptor {

    // 简单的缓存：保存上一次的 NetworkConfig 引用和对应的 Headers
    @Volatile
    private var cachedConfigRef: NetworkConfig? = null
    @Volatile
    private var cachedHeaders: Headers? = null

    init {
        configProvider.registerListener {
            // Invalidate cache when config changes
            cachedConfigRef = null
            cachedHeaders = null
        }
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val currentConfig = configProvider.current
        val headers = if (cachedConfigRef === currentConfig && cachedHeaders != null) {
            cachedHeaders!!
        } else {
            val hdrs = currentConfig.extraHeaders.toList()
            val built = Headers.Builder().apply {
                hdrs.forEach { (name, value) -> add(name, value) }
            }.build()
            cachedConfigRef = currentConfig
            cachedHeaders = built
            built
        }

        if (headers.size == 0) return chain.proceed(chain.request())

        val rb = chain.request().newBuilder()
        for (i in 0 until headers.size) {
            rb.addHeader(headers.name(i), headers.value(i))
        }
        return chain.proceed(rb.build())
    }
}
