package com.bohai.android_base_kit.di

import android.util.Log
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

import com.bohai.UserService
import com.bohai.apis.DemoApi
import com.bohai.apis.PayApi
import com.bohai.apis.UploadApi
import com.bohai.network.AuthTokenInterceptor
import com.bohai.network.DeviceIdInterceptor
import com.ail.lib_network.http.annotations.AppInterceptor
import com.ail.lib_network.http.annotations.INetLogger
import com.ail.lib_network.http.annotations.NetworkConfig
import com.ail.lib_network.http.auth.TokenProvider
import com.ail.lib_network.http.auth.UnauthorizedHandler
import com.ail.lib_network.http.util.NetworkClientFactory
import com.ail.lib_network.websocket.annotation.WebSocketClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.Optional

@Module
@InstallIn(SingletonComponent::class)
object AppNetworkModule {

    @Provides
    @Singleton
    fun provideNetworkConfig(): NetworkConfig {
        return NetworkConfig(
            baseUrl = "https://httz.xmbhzt.com/",
            isLogEnabled = true,
            extraHeaders = mapOf(
                "X-App-Version" to "1111",
                "X-Version-Code" to "1.0.0"
            )
        )
    }

    @Provides
    @Singleton
    fun provideNetLogger(): INetLogger = object : INetLogger {
        override fun d(tag: String, msg: String) {
            Log.d(tag, "[HTTP] $msg")
        }
        override fun e(tag: String, msg: String, throwable: Throwable?) {
            Log.e(tag, "[HTTP] $msg", throwable)
        }
    }



    // 示例性的 TokenProvider：用于 demo中让 Library 的 NetworkModule 自动注册 TokenAuthenticator。
    // 已替换为 AppTokenProvider 类（在 com.bohai.auth）更贴近生产实现。
    @Provides
    @Singleton
    fun provideTokenProvider(provider: com.bohai.auth.AppTokenProvider): com.ail.lib_network.http.auth.TokenProvider = provider

    @Provides
    @Singleton
    fun provideUnauthorizedHandler(handler: com.bohai.android_base_kit.auth.AppUnauthorizedHandler): UnauthorizedHandler = handler

    /**
     * 项目层自定义拦截器，Key 为执行顺序（数值越小越先执行）
     * 示例：
     * - 100: 添加 Token（AuthTokenInterceptor）
     * - 200: 添加设备 ID（DeviceIdInterceptor）
     * 若无自定义拦截器，可返回 emptyMap()
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
        // Demo pay baseUrl; replace with real baseUrl when available
        return factory.createRetrofit("https://pay.example.com/")
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
     * 提供 WebSocket OkHttpClient(可选)
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

    @Provides
    @Singleton
    fun provideWebSocketLogEnabled(): Boolean = true // WebSocket 日志已开启

    @Provides
    @Singleton
    fun provideWebSocketLoggerImpl(): INetLogger = object : INetLogger {
        override fun d(tag: String, msg: String) {
            Log.d(tag, "[WebSocket] $msg")
        }
        override fun e(tag: String, msg: String, throwable: Throwable?) {
            Log.e(tag, "[WebSocket] $msg", throwable)
        }
    }

}