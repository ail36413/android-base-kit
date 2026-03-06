package com.ail.lib_util.text

import android.util.Base64
import java.net.URLDecoder
import java.net.URLEncoder
import java.security.MessageDigest

/** 常见编码与摘要工具（Base64/URL/MD5/SHA-256）。 */
object EncodeUtil {

    private const val UTF8 = "UTF-8"

    /** UTF-8 文本转 Base64（无换行）。 */
    fun base64Encode(text: String): String = base64Encode(text.toByteArray(Charsets.UTF_8))

    /** 二进制转 Base64（无换行）。 */
    fun base64Encode(bytes: ByteArray): String {
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    /**
     * Base64 解码为 UTF-8 文本。
     * 失败返回空串（兼容旧行为）。
     */
    fun base64Decode(encoded: String): String {
        return base64DecodeToBytes(encoded)?.toString(Charsets.UTF_8).orEmpty()
    }

    /** Base64 解码为二进制，失败返回 null。 */
    fun base64DecodeToBytes(encoded: String): ByteArray? {
        return runCatching { Base64.decode(encoded, Base64.DEFAULT) }.getOrNull()
    }

    /** URL 编码（UTF-8）。失败时返回原文。 */
    fun urlEncode(text: String): String {
        return runCatching { URLEncoder.encode(text, UTF8) }.getOrDefault(text)
    }

    /** URL 解码（UTF-8）。失败时返回原文。 */
    fun urlDecode(text: String): String {
        return runCatching { URLDecoder.decode(text, UTF8) }.getOrDefault(text)
    }

    /** URL 解码（UTF-8）。失败时返回 null。 */
    fun urlDecodeOrNull(text: String): String? {
        return runCatching { URLDecoder.decode(text, UTF8) }.getOrNull()
    }

    /** Base64 URL-SAFE 编码（无换行、无 padding）。 */
    fun base64UrlEncode(text: String): String {
        return Base64.encodeToString(
            text.toByteArray(Charsets.UTF_8),
            Base64.NO_WRAP or Base64.NO_PADDING or Base64.URL_SAFE,
        )
    }

    /**
     * Base64 URL-SAFE 解码为 UTF-8 文本。
     * 失败返回空串（兼容旧行为）。
     */
    fun base64UrlDecode(encoded: String): String {
        return base64UrlDecodeToBytes(encoded)?.toString(Charsets.UTF_8).orEmpty()
    }

    /** Base64 URL-SAFE 解码为二进制，失败返回 null。 */
    fun base64UrlDecodeToBytes(encoded: String): ByteArray? {
        return runCatching { Base64.decode(encoded, Base64.URL_SAFE) }.getOrNull()
    }

    /** 计算文本 MD5（小写十六进制）。 */
    fun md5(text: String): String = digest(text, "MD5")

    /** 计算文本 SHA-256（小写十六进制）。 */
    fun sha256(text: String): String = digest(text, "SHA-256")

    /** 通用摘要计算实现。 */
    private fun digest(text: String, algorithm: String): String {
        val digest = MessageDigest.getInstance(algorithm)
        val bytes = digest.digest(text.toByteArray(Charsets.UTF_8))
        return bytes.joinToString(separator = "") { byte -> "%02x".format(byte) }
    }
}
