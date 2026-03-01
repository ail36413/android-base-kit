package com.ail.lib_network.http.auth

/**
 * Optional app-provided handler that will be called when the library detects an unauthorized
 * situation that should lead to app-level logout/navigation.
 *
 * App module can provide an implementation (via Dagger optional binding) to perform UI
 * navigation (e.g. open login screen) and session clearing.
 */
interface UnauthorizedHandler {
    fun onUnauthorized()
}

