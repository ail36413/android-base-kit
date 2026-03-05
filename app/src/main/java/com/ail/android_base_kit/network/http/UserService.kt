package com.ail.android_base_kit.network.http

import com.ail.android_base_kit.network.http.model.ApiResponse
import com.ail.android_base_kit.network.http.model.OnlinePlatformInfo
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Streaming
import retrofit2.http.Url

interface UserService {

    @GET("anything/okx/OKRichApi/appInvite/getAppByType")
    suspend fun checkStatus(@Query("type") type: Int): ApiResponse<List<OnlinePlatformInfo>>

    /** 下载文件（支持任意完整 URL，如下载图片） */
    @Streaming
    @GET
    suspend fun downloadFile(@Url url: String): ResponseBody
}
