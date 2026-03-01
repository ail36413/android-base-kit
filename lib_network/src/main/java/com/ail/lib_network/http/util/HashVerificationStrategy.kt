package com.ail.lib_network.http.util

/**
 * Strategy to decide what to do when downloaded file's hash does not match expected value.
 */
enum class HashVerificationStrategy {
    DELETE_ON_MISMATCH,
    KEEP_ON_MISMATCH
}
