package com.ail.lib_util.text

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Lightweight encryption helpers.
 *
 * AES output format: Base64(iv + cipherBytes)
 */
object EncryptUtil {

    private const val AES_TRANSFORMATION = "AES/CBC/PKCS5Padding"
    private const val HMAC_SHA256 = "HmacSHA256"

    /** AES encrypt UTF-8 text and return Base64(iv+cipher). */
    fun aesEncrypt(text: String, key: String): String {
        if (text.isEmpty()) return ""
        val secret = deriveAesKey(key)
        val iv = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val cipher = Cipher.getInstance(AES_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(secret, "AES"), IvParameterSpec(iv))
        val encrypted = cipher.doFinal(text.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(iv + encrypted, Base64.NO_WRAP)
    }

    /** AES decrypt Base64(iv+cipher). Return null when decrypt fails. */
    fun aesDecrypt(base64: String, key: String): String? {
        if (base64.isBlank()) return ""
        return runCatching {
            val raw = Base64.decode(base64, Base64.DEFAULT)
            if (raw.size <= 16) return null
            val iv = raw.copyOfRange(0, 16)
            val cipherBytes = raw.copyOfRange(16, raw.size)
            val secret = deriveAesKey(key)
            val cipher = Cipher.getInstance(AES_TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(secret, "AES"), IvParameterSpec(iv))
            cipher.doFinal(cipherBytes).toString(Charsets.UTF_8)
        }.getOrNull()
    }

    /** HMAC-SHA256 hex digest (lowercase). */
    fun hmacSha256(text: String, key: String): String {
        val mac = Mac.getInstance(HMAC_SHA256)
        mac.init(SecretKeySpec(key.toByteArray(Charsets.UTF_8), HMAC_SHA256))
        val bytes = mac.doFinal(text.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun deriveAesKey(key: String): ByteArray {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(key.toByteArray(Charsets.UTF_8)).copyOf(16)
    }
}

