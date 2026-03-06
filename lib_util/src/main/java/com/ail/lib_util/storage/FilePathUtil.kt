package com.ail.lib_util.storage

import java.io.File

/** 路径字符串处理工具。 */
object FilePathUtil {

    /** 拼接路径片段并统一分隔符。 */
    fun join(vararg segments: String): String {
        if (segments.isEmpty()) return ""
        return segments
            .filter { it.isNotBlank() }
            .joinToString(File.separator)
            .replace("/", File.separator)
            .replace("\\", File.separator)
    }

    /** 获取文件名（含扩展名）。 */
    fun fileName(path: String?): String {
        val raw = path.orEmpty().trim()
        if (raw.isEmpty()) return ""
        val normalized = normalizeSeparator(raw)
        return normalized.substringAfterLast(File.separator)
    }

    /** 获取扩展名（不含点号）。 */
    fun extension(path: String?): String {
        val name = fileName(path)
        val dot = name.lastIndexOf('.')
        if (dot <= 0 || dot == name.lastIndex) return ""
        return name.substring(dot + 1)
    }

    /** 获取不含扩展名的文件名。 */
    fun baseName(path: String?): String {
        val name = fileName(path)
        val dot = name.lastIndexOf('.')
        if (dot <= 0) return name
        return name.substring(0, dot)
    }

    /** 获取父路径。 */
    fun parent(path: String?): String {
        val raw = path.orEmpty().trim()
        if (raw.isEmpty()) return ""
        val normalized = normalizeSeparator(raw)
        return normalized.substringBeforeLast(File.separator, "")
    }

    /** 将 `/` 与 `\` 统一替换为当前平台分隔符。 */
    fun normalizeSeparator(path: String?): String {
        val raw = path.orEmpty()
        if (raw.isEmpty()) return ""
        return raw.replace("/", File.separator).replace("\\", File.separator)
    }

    /** 判断是否为绝对路径。 */
    fun isAbsolute(path: String?): Boolean {
        val raw = path.orEmpty().trim()
        if (raw.isEmpty()) return false
        return File(raw).isAbsolute
    }
}
