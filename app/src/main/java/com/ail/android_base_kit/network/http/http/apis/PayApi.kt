package com.bohai.apis

import com.bohai.model.ApiResponse
import retrofit2.http.GET

interface PayApi {
    @GET("/pay/status")
    suspend fun getPayStatus(): ApiResponse<String>
}
