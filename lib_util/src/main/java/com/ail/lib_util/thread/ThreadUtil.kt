package com.ail.lib_util.thread

import android.os.Handler
import android.os.Looper
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Lightweight thread helpers without extra dependencies.
 */
object ThreadUtil {

    private val mainHandler = Handler(Looper.getMainLooper())
    private val executorLock = Any()

    @Volatile
    private var ioExecutor: ExecutorService = Executors.newCachedThreadPool()

    /** 当前是否在主线程。 */
    fun isMainThread(): Boolean = Looper.myLooper() == Looper.getMainLooper()

    /** 在主线程执行任务。 */
    fun runOnMain(block: () -> Unit) {
        if (isMainThread()) {
            block()
        } else {
            mainHandler.post(block)
        }
    }

    /**
     * 延迟在主线程执行任务。
     *
     * @return 可用于取消的 Runnable。
     */
    fun runOnMainDelay(delayMs: Long, block: () -> Unit): Runnable {
        val safeDelay = if (delayMs < 0L) 0L else delayMs
        val runnable = Runnable(block)
        mainHandler.postDelayed(runnable, safeDelay)
        return runnable
    }

    /** 取消主线程延迟任务。 */
    fun cancelMainTask(task: Runnable) {
        mainHandler.removeCallbacks(task)
    }

    /** 在 IO 线程池执行任务。 */
    fun runOnIo(block: () -> Unit) {
        ensureExecutor().execute(block)
    }

    /**
     * 关闭线程池。
     *
     * @param cancelMainTasks 是否同步清空主线程队列中的延迟任务。
     */
    fun shutdown(cancelMainTasks: Boolean = false) {
        synchronized(executorLock) {
            ioExecutor.shutdownNow()
        }
        if (cancelMainTasks) {
            mainHandler.removeCallbacksAndMessages(null)
        }
    }

    private fun ensureExecutor(): ExecutorService {
        val current = ioExecutor
        if (!current.isShutdown && !current.isTerminated) return current

        synchronized(executorLock) {
            val latest = ioExecutor
            if (latest.isShutdown || latest.isTerminated) {
                ioExecutor = Executors.newCachedThreadPool()
            }
            return ioExecutor
        }
    }
}
