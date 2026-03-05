package com.ail.lib_network.websocket.di

import com.ail.lib_network.websocket.IWebSocketLogger
import com.ail.lib_network.websocket.IWebSocketManager
import com.ail.lib_network.websocket.WebSocketLogger
import com.ail.lib_network.websocket.WebSocketManager
import com.ail.lib_network.websocket.annotation.WebSocketClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.Optional
import okhttp3.OkHttpClient
import javax.inject.Singleton

/**
 * WebSocket 模块配置
 * 支持自定义 OkHttpClient 和日志实现
 */
@Module
@InstallIn(SingletonComponent::class)
object WebSocketModule {

    /**
     * 提供 WebSocketManager 实例
     * @param okHttpClient 可选的自定义 OkHttpClient
     * @param logger 可选的自定义日志实现
     */
    @Provides
    @Singleton
    fun provideWebSocketManager(
        @WebSocketClient okHttpClient: Optional<OkHttpClient>,
        logger: Optional<IWebSocketLogger>
    ): IWebSocketManager {
        // 设置日志实现（如果提供）
        logger.ifPresent { WebSocketLogger.setLogger(it) }

        val client = okHttpClient.orElseGet {
            OkHttpClient.Builder()
                .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .pingInterval(30, java.util.concurrent.TimeUnit.SECONDS)
                .build()
        }
        return WebSocketManager(client)
    }
}