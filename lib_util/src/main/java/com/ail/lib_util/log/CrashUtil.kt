package com.ail.lib_util.log

import android.os.Build
import android.os.Process
import com.ail.lib_util.UtilKit
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean

/** 全局崩溃捕获与落盘工具。 */
object CrashUtil {

    data class Config(
        /** 崩溃日志子目录名（位于 app 私有 filesDir 下）。 */
        val dirName: String = "crash_logs",
        /** 最多保留文件数量。 */
        val maxFileCount: Int = 20,
    )

    data class CrashInfo(
        val threadName: String,
        val timestamp: Long,
        val file: File?,
        val throwable: Throwable,
    )

    @Volatile
    private var currentConfig: Config = Config()

    @Volatile
    private var previousHandler: Thread.UncaughtExceptionHandler? = null

    @Volatile
    private var callback: ((CrashInfo) -> Unit)? = null

    private val installed = AtomicBoolean(false)

    /** 是否已安装全局崩溃捕获。 */
    fun isInstalled(): Boolean = installed.get()

    /**
     * 安装全局崩溃处理器。
     *
     * @param onCrash 崩溃回调（写文件后触发）。
     */
    @Synchronized
    fun install(config: Config = Config(), onCrash: ((CrashInfo) -> Unit)? = null) {
        UtilKit.requireInit()
        if (installed.get()) {
            callback = onCrash ?: callback
            return
        }
        currentConfig = config.copy(maxFileCount = config.maxFileCount.coerceAtLeast(1))
        callback = onCrash
        previousHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            val file = writeCrash(thread, throwable)
            callback?.invoke(
                CrashInfo(
                    threadName = thread.name,
                    timestamp = System.currentTimeMillis(),
                    file = file,
                    throwable = throwable,
                ),
            )
            previousHandler?.uncaughtException(thread, throwable)
        }
        installed.set(true)
    }

    /** 卸载并恢复之前的全局崩溃处理器。 */
    @Synchronized
    fun uninstall() {
        if (!installed.get()) return
        Thread.setDefaultUncaughtExceptionHandler(previousHandler)
        installed.set(false)
    }

    /** 手动记录一个已捕获异常（不会杀进程）。 */
    fun recordHandled(throwable: Throwable, thread: Thread = Thread.currentThread()): File? {
        return writeCrash(thread, throwable)
    }

    /** 崩溃日志目录。 */
    fun crashDir(): File {
        UtilKit.requireInit()
        val dir = File(UtilKit.appContext.filesDir, currentConfig.dirName)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    /** 按修改时间倒序返回日志文件。 */
    fun listCrashFiles(): List<File> {
        val dir = crashDir()
        return dir.listFiles()?.sortedByDescending { it.lastModified() } ?: emptyList()
    }

    /** 读取指定崩溃日志。 */
    fun readCrash(fileName: String): String {
        if (fileName.isBlank()) return ""
        val target = File(crashDir(), fileName)
        if (!target.exists()) return ""
        return runCatching { target.readText(Charsets.UTF_8) }.getOrDefault("")
    }

    /** 清空崩溃日志目录。 */
    fun clear() {
        listCrashFiles().forEach { it.delete() }
    }

    private fun writeCrash(thread: Thread, throwable: Throwable): File? {
        return runCatching {
            val dir = crashDir()
            val file = File(dir, buildFileName())
            file.writeText(buildCrashBody(thread, throwable), Charsets.UTF_8)
            trimFiles(dir)
            file
        }.getOrNull()
    }

    private fun buildFileName(): String {
        val time = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(Date())
        return "crash_${time}_${Process.myPid()}.log"
    }

    private fun buildCrashBody(thread: Thread, throwable: Throwable): String {
        val sb = StringBuilder(512)
        sb.appendLine("time=${System.currentTimeMillis()}")
        sb.appendLine("thread=${thread.name}")
        sb.appendLine("pid=${Process.myPid()}")
        sb.appendLine("brand=${Build.BRAND}")
        sb.appendLine("model=${Build.MODEL}")
        sb.appendLine("sdk=${Build.VERSION.SDK_INT}")
        sb.appendLine("package=${UtilKit.appContext.packageName}")
        sb.appendLine("---- stacktrace ----")
        sb.append(throwable.stackTraceToString())
        return sb.toString()
    }

    private fun trimFiles(dir: File) {
        val files = dir.listFiles()?.sortedByDescending { it.lastModified() } ?: return
        if (files.size <= currentConfig.maxFileCount) return
        files.drop(currentConfig.maxFileCount).forEach { it.delete() }
    }
}

