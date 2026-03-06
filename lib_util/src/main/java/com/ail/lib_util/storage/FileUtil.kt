package com.ail.lib_util.storage

import com.ail.lib_util.UtilKit
import java.io.File

/** 应用私有目录文件工具。 */
object FileUtil {

    /**
     * 获取目标文件对象（不存在目录会自动创建）。
     *
     * @param dirName 子目录名，默认 `util_demo`。
     */
    fun file(name: String, dirName: String = "util_demo"): File {
        UtilKit.requireInit()
        val dir = File(UtilKit.appContext.filesDir, dirName)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return File(dir, normalizeFileName(name))
    }

    /** 创建目录。 */
    fun mkdirs(dirName: String = "util_demo"): Boolean {
        return runCatching {
            UtilKit.requireInit()
            val dir = File(UtilKit.appContext.filesDir, dirName)
            dir.exists() || dir.mkdirs()
        }.getOrDefault(false)
    }

    /** 覆盖写入文本。 */
    fun writeText(name: String, text: String, dirName: String = "util_demo"): Boolean {
        return runCatching {
            file(name, dirName).writeText(text, Charsets.UTF_8)
            true
        }.getOrDefault(false)
    }

    /** 追加写入文本。 */
    fun appendText(name: String, text: String, dirName: String = "util_demo"): Boolean {
        return runCatching {
            file(name, dirName).appendText(text, Charsets.UTF_8)
            true
        }.getOrDefault(false)
    }

    /** 读取文本，文件不存在时返回空串。 */
    fun readText(name: String, dirName: String = "util_demo"): String {
        return runCatching {
            val target = file(name, dirName)
            if (!target.exists()) return@runCatching ""
            target.readText(Charsets.UTF_8)
        }.getOrDefault("")
    }

    /** 文件是否存在。 */
    fun exists(name: String, dirName: String = "util_demo"): Boolean {
        return runCatching { file(name, dirName).exists() }.getOrDefault(false)
    }

    /** 删除文件，不存在也视为成功。 */
    fun delete(name: String, dirName: String = "util_demo"): Boolean {
        return runCatching {
            val target = file(name, dirName)
            !target.exists() || target.delete()
        }.getOrDefault(false)
    }

    /** 文件大小（字节）。 */
    fun size(name: String, dirName: String = "util_demo"): Long {
        return runCatching {
            val target = file(name, dirName)
            if (!target.exists()) return@runCatching 0L
            target.length()
        }.getOrDefault(0L)
    }

    /** 最后修改时间戳（毫秒）。 */
    fun lastModified(name: String, dirName: String = "util_demo"): Long {
        return runCatching {
            val target = file(name, dirName)
            if (!target.exists()) return@runCatching 0L
            target.lastModified()
        }.getOrDefault(0L)
    }

    /** 列出目录下全部文件名（按名称排序）。 */
    fun listFiles(dirName: String = "util_demo"): List<String> {
        return runCatching {
            UtilKit.requireInit()
            val dir = File(UtilKit.appContext.filesDir, dirName)
            if (!dir.exists()) return@runCatching emptyList()
            dir.listFiles()?.map { it.name }?.sorted() ?: emptyList()
        }.getOrDefault(emptyList())
    }

    /** 清空目录内容。 */
    fun clearDir(dirName: String = "util_demo"): Boolean {
        return runCatching {
            UtilKit.requireInit()
            val dir = File(UtilKit.appContext.filesDir, dirName)
            if (!dir.exists()) return@runCatching true
            dir.listFiles()?.forEach { child ->
                if (child.isDirectory) {
                    child.deleteRecursively()
                } else {
                    child.delete()
                }
            }
            true
        }.getOrDefault(false)
    }

    private fun normalizeFileName(name: String): String {
        val raw = name.trim()
        if (raw.isEmpty()) return "default.txt"
        return raw
            .replace("\\", "_")
            .replace("/", "_")
            .replace("..", "_")
    }
}
