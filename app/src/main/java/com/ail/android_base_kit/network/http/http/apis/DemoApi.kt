package com.ail.android_base_kit.network.http.http.apis

import com.ail.lib_network.http.annotations.Timeout
import com.ail.android_base_kit.network.http.model.ApiResponse
import com.ail.android_base_kit.network.http.model.CreateUserRequest
import com.ail.android_base_kit.network.http.model.User
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface DemoApi {
    @POST("anything/user/create")
    suspend fun createUser(@Body req: CreateUserRequest): ApiResponse<User>

    @Timeout(connect = 30, read = 30, write = 30)
    @POST("anything/user/create")
    suspend fun createUserTimeout(@Body req: CreateUserRequest): ApiResponse<User>

    @POST("anything/user/create_with_header")
    suspend fun createUserWithHeader(
        @Header("X-Demo-Key") headerValue: String,
        @Body req: CreateUserRequest
    ): ApiResponse<User>
}
