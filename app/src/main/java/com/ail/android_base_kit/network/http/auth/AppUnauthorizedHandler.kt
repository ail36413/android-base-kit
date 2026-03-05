package com.ail.android_base_kit.network.http.auth

import com.ail.android_base_kit.App
import com.ail.lib_network.http.auth.TokenProvider
import com.ail.lib_network.http.auth.UnauthorizedHandler
import java.util.Optional
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppUnauthorizedHandler @Inject constructor(
    private val tokenProviderOptional: Optional<TokenProvider>
) : UnauthorizedHandler {
    override fun onUnauthorized() {
        try {
            if (tokenProviderOptional.isPresent) {
                try {
                    tokenProviderOptional.get().clear()
                } catch (_: Exception) {
                    // ignore
                }
            }
        } catch (_: Exception) {
        }

        // Ensure we run navigation on main thread; App.logoutAndGoLogin posts to main thread already
        try {
            App.instance.logoutAndGoLogin()
        } catch (_: Exception) {
        }
    }
}
