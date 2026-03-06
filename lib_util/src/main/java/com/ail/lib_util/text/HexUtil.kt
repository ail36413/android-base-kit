package com.ail.lib_util.text

/** Hex 与字节/字符串互转工具。 */
object HexUtil {

    private val hexChars = "0123456789abcdef".toCharArray()

    /** 字节数组转十六进制字符串（小写）。 */
    fun bytesToHex(bytes: ByteArray?): String {
        if (bytes == null || bytes.isEmpty()) return ""
        val result = CharArray(bytes.size * 2)
        bytes.forEachIndexed { index, byte ->
            val value = byte.toInt() and 0xFF
            result[index * 2] = hexChars[value ushr 4]
            result[index * 2 + 1] = hexChars[value and 0x0F]
        }
        return String(result)
    }

    /**
     * 十六进制字符串转字节数组。
     * 非法输入（空串、奇数长度、非 hex 字符）返回空数组。
     */
    fun hexToBytes(hex: String?): ByteArray {
        val raw = hex.orEmpty().trim().lowercase()
        if (raw.isEmpty() || raw.length % 2 != 0) return byteArrayOf()
        return runCatching {
            ByteArray(raw.length / 2) { i ->
                val high = raw[i * 2].digitToInt(16)
                val low = raw[i * 2 + 1].digitToInt(16)
                ((high shl 4) or low).toByte()
            }
        }.getOrDefault(byteArrayOf())
    }

    /** UTF-8 文本转 hex。 */
    fun stringToHexUtf8(text: String?): String = bytesToHex(text.orEmpty().toByteArray(Charsets.UTF_8))

    /** hex 转 UTF-8 文本。 */
    fun hexToStringUtf8(hex: String?): String = hexToBytes(hex).toString(Charsets.UTF_8)
}
