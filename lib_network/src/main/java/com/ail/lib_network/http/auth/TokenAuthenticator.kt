package com.ail.lib_network.http.auth

import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * 基于 OkHttp Authenticator 的 token 刷新实现。
 *
 * 说明：
 * - Authenticator.authenticate 在拦截器链之后、网络失败后被调用，且为同步阻塞调用。
 * - 本实现使用 ReentrantLock 串行化刷新，避免并发重复刷新。
 * - 如果刷新成功，返回使用新 token 的 Request；否则返回 null（放弃重试）。
 */
class TokenAuthenticator(
    private val tokenProvider: TokenProvider,
    private val headerName: String = "Authorization",
    private val tokenPrefix: String = "Bearer "
) : Authenticator {

    private val lock = ReentrantLock()

    override fun authenticate(route: Route?, response: Response): Request? {
        // 避免无限重试：如果 priorResponse 已经是 401 则放弃
        val prior = response.priorResponse
        if (prior != null && prior.code == 401) return null

        lock.withLock {
            // 如果其他线程已经刷新并更新了 token，则复用新 token
            val current = tokenProvider.getAccessToken()
            val requestToken = response.request.header(headerName)?.removePrefix(tokenPrefix)
            if (current != null && current != requestToken) {
                return response.request.newBuilder()
                    .header(headerName, "$tokenPrefix$current")
                    .build()
            }

            // 执行刷新
            val refreshed = try {
                tokenProvider.refreshTokenBlocking()
            } catch (_: Throwable) {
                false
            }

            if (!refreshed) return null

            val newToken = tokenProvider.getAccessToken() ?: return null
            return response.request.newBuilder()
                .header(headerName, "$tokenPrefix$newToken")
                .build()
        }
    }
}
