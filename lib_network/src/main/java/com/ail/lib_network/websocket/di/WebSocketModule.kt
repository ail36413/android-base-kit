package com.ail.lib_network.websocket.di

import com.ail.lib_network.websocket.IWebSocketManager
import com.ail.lib_network.websocket.WebSocketManager
import com.ail.lib_network.websocket.annotation.WebSocketClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.Optional
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WebSocketModule {

    @Provides
    @Singleton
    fun provideWebSocketManager(
        @WebSocketClient okHttpClient: Optional<OkHttpClient>
    ): IWebSocketManager {
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