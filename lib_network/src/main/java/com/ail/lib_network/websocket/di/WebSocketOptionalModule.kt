package com.ail.lib_network.websocket.di

import com.ail.lib_network.websocket.annotation.WebSocketClient
import dagger.BindsOptionalOf
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient

@Module
@InstallIn(SingletonComponent::class)
abstract class WebSocketOptionalModule {

    @BindsOptionalOf
    @WebSocketClient
    abstract fun bindOptionalWebSocketClient(): OkHttpClient
}

