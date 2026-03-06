package com.ail.lib_util.text

import java.util.zip.Adler32
import java.util.zip.CRC32

/** 常见校验值计算工具。 */
object ChecksumUtil {

    /** 文本 CRC32（16 进制字符串）。 */
    fun crc32(text: String?): String {
        val crc = CRC32()
        val bytes = text.orEmpty().toByteArray(Charsets.UTF_8)
        crc.update(bytes)
        return crc.value.toString(16)
    }

    /** 文本 Adler32（16 进制字符串）。 */
    fun adler32(text: String?): String {
        val adler = Adler32()
        val bytes = text.orEmpty().toByteArray(Charsets.UTF_8)
        adler.update(bytes)
        return adler.value.toString(16)
    }

    /** 简单异或校验，常用于轻量协议报文。 */
    fun xorChecksum(bytes: ByteArray?): Int {
        if (bytes == null || bytes.isEmpty()) return 0
        return bytes.fold(0) { acc, value -> acc xor (value.toInt() and 0xFF) }
    }
}
