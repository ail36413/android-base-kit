package com.ail.android_base_kit.network.http.http.apis

import com.ail.android_base_kit.network.http.model.ApiResponse
import retrofit2.http.GET

interface PayApi {
    @GET("anything/pay/status")
    suspend fun getPayStatus(): ApiResponse<String>
}
