package com.ail.android_base_kit.network.http.http.apis

import com.ail.android_base_kit.network.http.model.ApiResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PartMap

interface UploadApi {
    @Multipart
    @POST("anything/upload/file")
    suspend fun uploadFile(@Part file: MultipartBody.Part): ApiResponse<String>

    @Multipart
    @POST("anything/upload/multi")
    suspend fun uploadMultiple(
        @Part parts: List<MultipartBody.Part>,
        @PartMap fields: Map<String, @JvmSuppressWildcards RequestBody>
    ): ApiResponse<String>
}
