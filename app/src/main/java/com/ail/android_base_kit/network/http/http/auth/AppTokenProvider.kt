package com.bohai.auth

import com.ail.lib_network.http.auth.TokenProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.concurrent.withLock

/**
 * App-level TokenProvider (production-style demo):
 * - Thread-safe in-memory token storage
 * - refreshTokenBlocking() performs a synchronous HTTP refresh using a dedicated OkHttp client
 * - refreshTokenSuspend() delegates to the blocking refresh on IO dispatcher
 *
 * Important: The refresh call uses its own OkHttpClient instance with no Authenticator to avoid deadlocks
 * when called from an OkHttp Authenticator.
 */
@Singleton
class AppTokenProvider @Inject constructor() : TokenProvider {

    private val lock = ReentrantLock()
    @Volatile
    private var token: String? = null

    // Dedicated client for refreshing token. No Authenticator, short timeouts.
    private val refreshClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    override fun getAccessToken(): String? = token

    override fun refreshTokenBlocking(): Boolean {
        // Ensure only one thread performs the network refresh at a time
        lock.withLock {
            try {
                // Example refresh endpoint and payload (replace with your real endpoint / body)
                val refreshUrl = "https://httz.xmbhzt.com/auth/refresh"
                val json = JSONObject().apply {
                    put("grant_type", "refresh_token")
                    put("refresh_token", "dummy_refresh_token")
                }
                val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
                val request = Request.Builder()
                    .url(refreshUrl)
                    .post(body)
                    .build()

                refreshClient.newCall(request).execute().use { resp ->
                    if (!resp.isSuccessful) return false
                    val respBody = resp.body?.string() ?: return false
                    val obj = JSONObject(respBody)
                    // Expecting response like {"accessToken": "..."}
                    val newToken = when {
                        obj.has("accessToken") -> obj.getString("accessToken")
                        obj.has("access_token") -> obj.getString("access_token")
                        else -> null
                    }
                    if (newToken.isNullOrBlank()) return false
                    token = newToken
                    return true
                }
            } catch (t: Throwable) {
                // log if needed
                return false
            }
        }
    }

    override suspend fun refreshTokenSuspend(): Boolean {
        return withContext(Dispatchers.IO) { refreshTokenBlocking() }
    }

    override fun clear() {
        lock.withLock { token = null }
    }

    // Helper for demo/testing to seed a token value
    fun setAccessToken(value: String?) {
        lock.withLock { token = value }
    }
}
