package com.bohai.apis

import com.ail.lib_network.http.annotations.Timeout
import com.bohai.model.ApiResponse
import com.bohai.model.CreateUserRequest
import com.bohai.model.User
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface DemoApi {
    @POST("/user/create")
    suspend fun createUser(@Body req: CreateUserRequest): ApiResponse<User>

    @Timeout(connect = 30, read = 30, write = 30)
    @POST("/user/create")
    suspend fun createUserTimeout(@Body req: CreateUserRequest): ApiResponse<User>

    @POST("/user/create")
    suspend fun createUserWithHeader(
        @Header("X-Demo-Key") headerValue: String,
        @Body req: CreateUserRequest
    ): ApiResponse<User>
}
