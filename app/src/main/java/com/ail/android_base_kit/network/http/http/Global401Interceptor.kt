package com.bohai.android_base_kit.http

/**
 * Deprecated: Global401Interceptor is no longer needed because the library's RequestExecutor
 * performs centralized business-401 handling and the app provides an UnauthorizedHandler.
 *
 * Kept as a no-op placeholder to avoid accidental references; can be removed later.
 */
@Deprecated("No-op: centralized handling moved to RequestExecutor + UnauthorizedHandler")
class Global401Interceptor {
    // intentionally empty - do not use
}
