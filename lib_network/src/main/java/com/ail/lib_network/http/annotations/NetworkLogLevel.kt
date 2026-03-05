package com.ail.lib_network.http.annotations

/**
 * HTTP logging level for lib_network.
 * AUTO keeps backward compatibility with isLogEnabled.
 */
enum class NetworkLogLevel {
    AUTO,
    NONE,
    BASIC,
    HEADERS,
    BODY
}
