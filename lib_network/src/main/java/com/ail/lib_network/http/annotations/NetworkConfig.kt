package com.ail.lib_network.http.annotations

import com.ail.lib_network.http.model.ResponseFieldMapping
import java.io.File

/**
 * 全局网络配置参数（项目层提供）
 * 优先级：单接口配置 > 本配置 > 基础库默认值
 *
 * 注意：
 * - 本配置在应用启动阶段即被创建，推荐遵循 Fail-Fast 原则，在这里做基础校验，
 *   避免运行时才因为 baseUrl / 超时配置错误导致难以排查的崩溃。
 *
 * @param baseUrl 必须由项目层提供，基础库不提供默认值，且必须以 http/https 开头
 * @param connectTimeout 连接超时（秒），不设则用基础库默认 15
 * @param readTimeout 读取超时（秒），不设则用基础库默认 15
 * @param writeTimeout 写入超时（秒），不设则用基础库默认 15
 * @param defaultSuccessCode 全局默认成功码，不设则用基础库默认 0
 * @param isLogEnabled 是否开启网络日志，不设则用基础库默认 false
 * @param extraHeaders 每次请求都会带上的公共请求头（如 X-App-Version、X-Version-Code），由项目层提供
 * @param cacheDir 可选：OkHttp 缓存目录（若提供且 cacheSize 也提供则启用 Cache）
 * @param cacheSize 可选：缓存大小（字节），与 cacheDir 配合使用
 * @param enableRetryInterceptor 如果为 true，NetworkModule 会在 OkHttpClient 中注册 RetryInterceptor
 * @param retryMaxAttempts RetryInterceptor 的最大重试次数（默认 2）
 * @param retryInitialBackoffMs Retry 初始退避毫秒数（默认 300ms）
 * @param responseFieldMapping 全局响应字段映射，默认按 {code,msg,data} 解析
 */
data class NetworkConfig(
    val baseUrl: String,
    val connectTimeout: Long = 15L,
    val readTimeout: Long = 15L,
    val writeTimeout: Long = 15L,
    val defaultSuccessCode: Int = 0,
    val isLogEnabled: Boolean = false,
    val extraHeaders: Map<String, String> = emptyMap(),
    val cacheDir: File? = null,
    val cacheSize: Long? = null,
    val enableRetryInterceptor: Boolean = false,
    val retryMaxAttempts: Int = 2,
    val retryInitialBackoffMs: Long = 300L,
    val responseFieldMapping: ResponseFieldMapping = ResponseFieldMapping()
) {

    init {
        require(baseUrl.isNotBlank()) {
            "NetworkConfig.baseUrl must not be blank."
        }
        require(baseUrl.startsWith("http://") || baseUrl.startsWith("https://")) {
            "NetworkConfig.baseUrl must start with http:// or https://, actual: $baseUrl"
        }
        require(baseUrl.endsWith('/')) {
            "NetworkConfig.baseUrl must end with '/'. Retrofit baseUrl requires trailing slash, actual: $baseUrl"
        }
        require(connectTimeout > 0) {
            "NetworkConfig.connectTimeout must be > 0 seconds, actual: $connectTimeout"
        }
        require(readTimeout > 0) {
            "NetworkConfig.readTimeout must be > 0 seconds, actual: $readTimeout"
        }
        require(writeTimeout > 0) {
            "NetworkConfig.writeTimeout must be > 0 seconds, actual: $writeTimeout"
        }
        require(retryMaxAttempts >= 0) {
            "NetworkConfig.retryMaxAttempts must be >= 0"
        }
        require(retryInitialBackoffMs >= 0) {
            "NetworkConfig.retryInitialBackoffMs must be >= 0"
        }
    }
}
