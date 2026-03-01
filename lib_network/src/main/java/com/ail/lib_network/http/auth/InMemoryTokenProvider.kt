package com.ail.lib_network.http.auth

import kotlinx.coroutines.runBlocking
import java.util.concurrent.atomic.AtomicReference

/** 简单的内存 TokenProvider 实现，供 demo 与测试使用。 */
class InMemoryTokenProvider(
    initialAccessToken: String? = null,
    private val refresher: () -> Boolean = { false }
) : TokenProvider {

    private val tokenRef = AtomicReference<String?>(initialAccessToken)

    override fun getAccessToken(): String? = tokenRef.get()

    override fun refreshTokenBlocking(): Boolean {
        // 调用传入的 refresher，成功时不直接设置 token（应用层负责持久化并让 getAccessToken 返回新值），
        // 也可以在 refresher 内部直接修改 tokenRef（如果需要）。
        return try {
            refresher()
        } catch (_: Throwable) {
            false
        }
    }

    override suspend fun refreshTokenSuspend(): Boolean = runBlocking { refreshTokenBlocking() }

    override fun clear() {
        tokenRef.set(null)
    }

    fun setAccessToken(token: String?) {
        tokenRef.set(token)
    }
}
