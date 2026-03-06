package com.ail.lib_util.click

import android.view.View
import java.util.concurrent.ConcurrentHashMap

/**
 * Click helper to avoid repeated fast clicks.
 */
object ClickUtil {

    private val clickRecord = ConcurrentHashMap<String, Long>()

    /**
     * 判断是否为快速重复点击。
     *
     * @param key 点击隔离标识；同 key 共享防抖窗口。
     * @param intervalMs 防抖时间窗口，<=0 时使用默认 600ms。
     */
    fun isFastClick(key: String = "global", intervalMs: Long = 600L): Boolean {
        val now = System.currentTimeMillis()
        val safeInterval = if (intervalMs <= 0L) 600L else intervalMs
        val last = clickRecord[key] ?: 0L
        val fast = now - last < safeInterval
        clickRecord[key] = now
        return fast
    }

    /**
     * 为 View 设置防抖点击监听。
     *
     * @param key 自定义防抖 key；为空时按 View 实例自动隔离。
     */
    fun setDebouncedClickListener(
        view: View,
        intervalMs: Long = 600L,
        key: String? = null,
        onClick: (View) -> Unit,
    ) {
        // Default key isolates each concrete view instance to avoid cross-view interference.
        val resolvedKey = key?.takeIf { it.isNotBlank() }
            ?: "view_${view.id}_${System.identityHashCode(view)}"

        view.setOnClickListener {
            if (!isFastClick(resolvedKey, intervalMs)) {
                onClick(it)
            }
        }
    }

    /** 清除指定 key 的点击记录。 */
    fun clear(key: String) {
        clickRecord.remove(key)
    }

    /** 清除所有点击记录。 */
    fun clearAll() {
        clickRecord.clear()
    }
}
