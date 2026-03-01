package com.ail.lib_network.http.di

import com.ail.lib_network.http.annotations.AppInterceptor
import com.ail.lib_network.http.annotations.INetLogger
import com.ail.lib_network.http.annotations.NetworkConfigProvider
import com.ail.lib_network.http.auth.TokenAuthenticator
import com.ail.lib_network.http.auth.TokenProvider
import com.ail.lib_network.http.interceptor.DynamicBaseUrlInterceptor
import com.ail.lib_network.http.interceptor.DynamicTimeoutInterceptor
import com.ail.lib_network.http.interceptor.ExtraHeadersInterceptor
import com.ail.lib_network.http.interceptor.PrettyNetLogger
import com.ail.lib_network.http.util.DefaultRetryStrategy
import com.ail.lib_network.http.util.NetworkClientFactory
import com.ail.lib_network.http.util.RetryInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Optional
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // A simple no-op logger fallback when app does not provide INetLogger
    private val NOOP_INET_LOGGER: INetLogger = object : INetLogger {
        override fun d(tag: String, msg: String) {}
        override fun e(tag: String, msg: String, throwable: Throwable?) {}
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        configProvider: NetworkConfigProvider,
        netLoggerOptional: Optional<INetLogger>,
        @AppInterceptor optionalCustomInterceptors: Optional<Map<Int, @JvmSuppressWildcards Interceptor>>,
        // use Optional to allow app to not provide TokenProvider
        tokenProvider: Optional<TokenProvider>
    ): OkHttpClient {
        val config = configProvider.current
        val netLogger = netLoggerOptional.orElse(NOOP_INET_LOGGER)
        val customInterceptors = optionalCustomInterceptors.orElse(emptyMap())

        val builder = OkHttpClient.Builder()
            .connectTimeout(config.connectTimeout, TimeUnit.SECONDS)
            .readTimeout(config.readTimeout, TimeUnit.SECONDS)
            .writeTimeout(config.writeTimeout, TimeUnit.SECONDS)
            // 拦截器执行顺序说明（应用拦截器）：
            // 1. 动态 BaseUrl：尽早确定最终 host/schema/port
            // 2. 动态超时：基于注解覆写本次请求的超时配置
            // 3. 公共头：在 URL/超时都确定之后，补齐通用 Header
            // 4. 自定义拦截器：项目层按 key 排序后插入
            // 5. 日志拦截器：最后一环，打印最终请求信息
            .addInterceptor(DynamicBaseUrlInterceptor())
            .addInterceptor(DynamicTimeoutInterceptor())
            .addInterceptor(ExtraHeadersInterceptor(configProvider))

        customInterceptors.toSortedMap().forEach { (_, interceptor) ->
            builder.addInterceptor(interceptor)
        }

        // Optional: add retry interceptor as configured
        if (config.enableRetryInterceptor) {
            try {
                builder.addInterceptor(
                    RetryInterceptor(
                        DefaultRetryStrategy(
                            maxRetries = config.retryMaxAttempts,
                            initialBackoffMillis = config.retryInitialBackoffMs
                        )
                    )
                )
            } catch (_: Exception) {
                // ignore retry setup failure
            }
        }

        // If app provided a TokenProvider, register TokenAuthenticator (optional behavior)
        if (tokenProvider.isPresent) {
            try {
                builder.authenticator(TokenAuthenticator(tokenProvider.get()))
            } catch (_: Throwable) {
                // ignore auth setup failure
            }
        }

        if (config.isLogEnabled) {
            val loggingInterceptor = HttpLoggingInterceptor(PrettyNetLogger(netLogger)).apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            builder.addInterceptor(loggingInterceptor)
        }

        // If cacheDir and cacheSize are provided, enable OkHttp cache
        if (config.cacheDir != null && config.cacheSize != null && config.cacheSize > 0) {
            try {
                builder.cache(Cache(config.cacheDir, config.cacheSize))
            } catch (_: Exception) {
                // ignore cache setup failure, logging can be added via INetLogger if needed
            }
        }

        return builder.build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        factory: NetworkClientFactory,
        configProvider: NetworkConfigProvider
    ): Retrofit {
        val config = configProvider.current
        return factory.createRetrofit(config.baseUrl)
    }

    /**
     * 默認的 Retrofit 工廠實現：復用全局 OkHttpClient + GsonConverterFactory。
     * 如需多 Retrofit 實例，項目層可以自行注入自定義實現覆蓋此工廠。
     */
    @Provides
    @Singleton
    fun provideNetworkClientFactory(
        client: OkHttpClient
    ): NetworkClientFactory {
        return object : NetworkClientFactory {
            override fun createRetrofit(baseUrl: String): Retrofit {
                return Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            }
        }
    }
}
