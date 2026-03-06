package com.ail.android_base_kit.network.di

import android.util.Log
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

import com.ail.android_base_kit.network.http.UserService
import com.ail.android_base_kit.network.http.http.apis.DemoApi
import com.ail.android_base_kit.network.http.http.apis.PayApi
import com.ail.android_base_kit.network.http.http.apis.UploadApi
import com.ail.android_base_kit.network.http.network.AuthTokenInterceptor
import com.ail.android_base_kit.network.http.network.DeviceIdInterceptor
import com.ail.lib_network.http.annotations.AppInterceptor
import com.ail.lib_network.http.annotations.INetLogger
import com.ail.lib_network.http.annotations.NetworkConfig
import com.ail.lib_network.http.annotations.NetworkLogLevel
import com.ail.android_base_kit.network.http.model.ResponseMappingPresets
import com.ail.lib_network.websocket.IWebSocketLogger
import com.ail.lib_network.http.auth.TokenProvider
import com.ail.lib_network.http.auth.UnauthorizedHandler
import com.ail.lib_network.http.util.NetworkClientFactory
import com.ail.lib_network.websocket.annotation.WebSocketClient
import com.ail.android_base_kit.network.http.auth.AppUnauthorizedHandler
import com.ail.android_base_kit.network.http.http.auth.AppTokenProvider
import com.ail.lib_util.log.LogUtil
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import timber.log.Timber
import java.util.Optional

@Module
@InstallIn(SingletonComponent::class)
object AppNetworkModule {

    private const val HTTP_BIN_BASE_URL = "https://httpbin.org/"

    /**
     * 必配：lib_network 的最小必需初始化，必须提供 NetworkConfig。
     * 说明：baseUrl 仅用于 Demo 接口，必须是 http/https 且以 / 结尾。
     */
    @Provides
    @Singleton
    fun provideNetworkConfig(): NetworkConfig {
        return NetworkConfig(
            baseUrl = HTTP_BIN_BASE_URL,
            isLogEnabled = true,
            networkLogLevel = NetworkLogLevel.BODY,
            extraHeaders = mapOf(
                "X-App-Version" to "1111",
                "X-Version-Code" to "1.0.0"
            ),
            // 默认预设：code/msg/data
            responseFieldMapping = ResponseMappingPresets.standardCodeMsgData()
        )
    }

    /**
     * 选配：自定义 HTTP 日志输出；不提供时库内会使用 no-op logger。
     */
    @Provides
    @Singleton
    fun provideNetLogger(): INetLogger = object : INetLogger {
        override fun d(tag: String, msg: String) {
            LogUtil.d(tag, msg)
        }
        override fun e(tag: String, msg: String, throwable: Throwable?) {
            LogUtil.e(tag, msg, throwable)
        }
    }

    /**
     * 选配：TokenProvider，提供后库会自动启用 TokenAuthenticator 刷新逻辑。
     */
    @Provides
    @Singleton
    fun provideTokenProvider(provider: AppTokenProvider): TokenProvider = provider

    /**
     * 选配：401 未授权处理回调（如清理状态并跳转登录页）。
     */
    @Provides
    @Singleton
    fun provideUnauthorizedHandler(handler: AppUnauthorizedHandler): UnauthorizedHandler = handler

    /**
     * 选配：项目层自定义拦截器，Key 为执行顺序（数值越小越先执行）。
     */
    @Provides
    @AppInterceptor
    fun provideAppInterceptors(tokenProviderOptional: Optional<TokenProvider>): Map<Int, Interceptor> {
        val map = mutableMapOf<Int, Interceptor>()
        // 100 先执行：为请求添加 Authorization（只有在提供 TokenProvider 时才添加）
        if (tokenProviderOptional.isPresent) {
            map[100] = AuthTokenInterceptor(getToken = {
                // read token dynamically from provider
                tokenProviderOptional.orElse(null)?.getAccessToken()
            })
        }
        // 200 后执行：为请求添加设备标识
        map[200] = DeviceIdInterceptor(deviceId = "device_android_001")
        return map
    }

    @Provides
    @Singleton
    @NormalRetrofit
    fun provideNormalRetrofit(retrofit: Retrofit): Retrofit = retrofit

    @Provides
    @Singleton
    @PayRetrofit
    fun providePayRetrofit(factory: NetworkClientFactory): Retrofit {
        // Public test base URL for pay demo
        return factory.createRetrofit(HTTP_BIN_BASE_URL)
    }

    @Provides
    @Singleton
    fun provideUserService(@NormalRetrofit retrofit: Retrofit): UserService {
        return retrofit.create(UserService::class.java)
    }

    // Provide DemoApi and UploadApi via Retrofit so they can be @Inject-ed
    @Provides
    @Singleton
    fun provideDemoApi(@NormalRetrofit retrofit: Retrofit): DemoApi = retrofit.create(DemoApi::class.java)

    @Provides
    @Singleton
    fun provideUploadApi(@NormalRetrofit retrofit: Retrofit): UploadApi = retrofit.create(UploadApi::class.java)

    @Provides
    @Singleton
    fun providePayApi(@PayRetrofit retrofit: Retrofit): PayApi = retrofit.create(PayApi::class.java)

    /**
     * 选配：提供 WebSocket OkHttpClient；不提供时库内会使用默认配置。
     */
    @Provides
    @Singleton
    @WebSocketClient
    fun provideWebSocketOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .pingInterval(30, TimeUnit.SECONDS)
            .build()
    }

    /**
     * 选配：提供 WebSocket 日志实现；与 HTTP 日志完全独立。
     */
    @Provides
    @Singleton
    fun provideWebSocketLogger(): IWebSocketLogger = object : IWebSocketLogger {
        override fun d(tag: String, msg: String) {
            LogUtil.d(tag, msg)
        }
        override fun e(tag: String, msg: String, throwable: Throwable?) {
            LogUtil.e(tag, msg, throwable)
        }
    }

}