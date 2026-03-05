package com.ail.lib_network.websocket.di

import com.ail.lib_network.websocket.IWebSocketLogger
import com.ail.lib_network.websocket.annotation.WebSocketClient
import dagger.BindsOptionalOf
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient

/**
 * WebSocket 可选依赖绑定模块
 * 使 OkHttpClient 和 IWebSocketLogger 成为可选注入
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class WebSocketOptionalModule {

    @BindsOptionalOf
    @WebSocketClient
    abstract fun bindOptionalWebSocketClient(): OkHttpClient

    @BindsOptionalOf
    abstract fun bindOptionalWebSocketLogger(): IWebSocketLogger
}

