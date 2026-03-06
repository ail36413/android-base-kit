package com.ail.lib_util.log

import android.util.Log
import timber.log.Timber

/**
 * 统一日志入口。
 *
 * - 优先使用 Timber（若已初始化）
 * - Timber 未初始化时回退到 `android.util.Log`
 */
object LogUtil {

    private const val FALLBACK_TAG = "LogUtil"

    /** 输出 Debug 日志（默认 tag）。 */
    fun d(message: String, throwable: Throwable? = null) {
        log(null, Log.DEBUG, message, throwable)
    }

    /** 输出 Debug 日志（指定 tag）。 */
    fun d(tag: String, message: String, throwable: Throwable? = null) {
        log(tag, Log.DEBUG, message, throwable)
    }

    /** 输出 Info 日志（默认 tag）。 */
    fun i(message: String, throwable: Throwable? = null) {
        log(null, Log.INFO, message, throwable)
    }

    /** 输出 Info 日志（指定 tag）。 */
    fun i(tag: String, message: String, throwable: Throwable? = null) {
        log(tag, Log.INFO, message, throwable)
    }

    /** 输出 Warn 日志（默认 tag）。 */
    fun w(message: String, throwable: Throwable? = null) {
        log(null, Log.WARN, message, throwable)
    }

    /** 输出 Warn 日志（指定 tag）。 */
    fun w(tag: String, message: String, throwable: Throwable? = null) {
        log(tag, Log.WARN, message, throwable)
    }

    /** 输出 Error 日志（默认 tag）。 */
    fun e(message: String, throwable: Throwable? = null) {
        log(null, Log.ERROR, message, throwable)
    }

    /** 输出 Error 日志（指定 tag）。 */
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        log(tag, Log.ERROR, message, throwable)
    }

    private fun log(tag: String?, priority: Int, message: String, throwable: Throwable?) {
        if (Timber.forest().isNotEmpty()) {
            val tree = if (tag.isNullOrBlank()) Timber else Timber.tag(tag)
            when (priority) {
                Log.DEBUG -> if (throwable == null) tree.d(message) else tree.d(throwable, message)
                Log.INFO -> if (throwable == null) tree.i(message) else tree.i(throwable, message)
                Log.WARN -> if (throwable == null) tree.w(message) else tree.w(throwable, message)
                else -> if (throwable == null) tree.e(message) else tree.e(throwable, message)
            }
            return
        }

        val finalTag = tag ?: FALLBACK_TAG
        // Fallback path when UtilKit/Timber is not initialized.
        Log.println(priority, finalTag, message)
        throwable?.let { Log.println(priority, finalTag, it.stackTraceToString()) }
    }
}
