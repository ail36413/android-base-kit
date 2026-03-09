package com.ail.lib_util.storage

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import com.ail.lib_util.UtilKit
import java.io.File

/** URI 文件信息工具，支持 `content://` 与 `file://`。 */
object UriFileUtil {

    /** 获取文件名，失败返回空串。 */
    fun fileName(uri: Uri?): String {
        if (uri == null) return ""
        return when (uri.scheme) {
            ContentResolver.SCHEME_CONTENT -> queryString(uri, OpenableColumns.DISPLAY_NAME)
            ContentResolver.SCHEME_FILE, null -> File(uri.path.orEmpty()).name
            else -> uri.lastPathSegment.orEmpty()
        }
    }

    /** 字符串重载：获取文件名。 */
    fun fileName(uriString: String?): String = fileName(uriString.toUriOrNull())

    /** 获取 MIME，失败返回空串。 */
    fun mimeType(uri: Uri?): String {
        if (uri == null) return ""
        return when (uri.scheme) {
            ContentResolver.SCHEME_CONTENT -> {
                UtilKit.requireInit()
                UtilKit.appContext.contentResolver.getType(uri).orEmpty()
            }
            ContentResolver.SCHEME_FILE, null -> {
                val ext = File(uri.path.orEmpty()).extension.lowercase()
                if (ext.isEmpty()) "" else MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext).orEmpty()
            }
            else -> ""
        }
    }

    /** 字符串重载：获取 MIME。 */
    fun mimeType(uriString: String?): String = mimeType(uriString.toUriOrNull())

    /** 获取文件大小（字节），失败返回 0。 */
    fun size(uri: Uri?): Long {
        if (uri == null) return 0L
        return when (uri.scheme) {
            ContentResolver.SCHEME_CONTENT -> queryLong(uri, OpenableColumns.SIZE)
            ContentResolver.SCHEME_FILE, null -> File(uri.path.orEmpty()).length().coerceAtLeast(0L)
            else -> 0L
        }
    }

    /** 字符串重载：获取大小。 */
    fun size(uriString: String?): Long = size(uriString.toUriOrNull())

    private fun queryString(uri: Uri, column: String): String {
        return runCatching {
            UtilKit.requireInit()
            UtilKit.appContext.contentResolver.query(uri, arrayOf(column), null, null, null)?.use { cursor ->
                if (!cursor.moveToFirst()) return@use ""
                val index = cursor.getColumnIndex(column)
                if (index < 0) "" else cursor.getString(index).orEmpty()
            }.orEmpty()
        }.getOrDefault("")
    }

    private fun queryLong(uri: Uri, column: String): Long {
        return runCatching {
            UtilKit.requireInit()
            UtilKit.appContext.contentResolver.query(uri, arrayOf(column), null, null, null)?.use { cursor ->
                if (!cursor.moveToFirst()) return@use 0L
                val index = cursor.getColumnIndex(column)
                if (index < 0) 0L else cursor.getLong(index)
            } ?: 0L
        }.getOrDefault(0L)
    }

    private fun String?.toUriOrNull(): Uri? = this?.trim()?.takeIf { it.isNotEmpty() }?.let(Uri::parse)
}
