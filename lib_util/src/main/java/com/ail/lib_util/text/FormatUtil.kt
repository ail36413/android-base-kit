package com.ail.lib_util.text

import java.util.Locale
import kotlin.math.log10
import kotlin.math.pow

/** 通用格式化工具。 */
object FormatUtil {

    /** 将毫秒时长格式化为 mm:ss 或 HH:mm:ss。 */
    fun formatDuration(durationMs: Long): String {
        val safeMs = if (durationMs < 0L) 0L else durationMs
        val totalSeconds = safeMs / 1000L
        val hours = totalSeconds / 3600L
        val minutes = (totalSeconds % 3600L) / 60L
        val seconds = totalSeconds % 60L
        return if (hours > 0L) {
            String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.US, "%02d:%02d", minutes, seconds)
        }
    }

    /** 将字节大小格式化为人类可读文本。 */
    fun formatFileSize(bytes: Long): String {
        if (bytes < 0L) return "0 B"
        if (bytes < 1024L) return "$bytes B"
        val units = arrayOf("KB", "MB", "GB", "TB")
        val digitGroup = (log10(bytes.toDouble()) / log10(1024.0)).toInt().coerceAtMost(units.size)
        val size = bytes / 1024.0.pow(digitGroup.toDouble())
        val unit = units[digitGroup - 1]
        return String.format(Locale.US, "%.2f %s", size, unit)
    }

    /** 手机号脱敏，保留前 3 位和后 4 位。 */
    fun maskPhone(phone: String): String {
        val source = phone.trim()
        if (source.length < 7) return source
        return source.replaceRange(3, source.length - 4, "****")
    }
}
