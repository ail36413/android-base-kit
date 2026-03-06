package com.ail.lib_util.storage

import java.util.concurrent.ConcurrentHashMap

/** 轻量内存缓存（支持过期时间）。 */
object CacheUtil {

    private data class Entry(val value: Any, val expireAt: Long)

    private const val NEVER_EXPIRE = Long.MAX_VALUE
    private val memory = ConcurrentHashMap<String, Entry>()

    /**
     * 写入缓存。
     *
     * @param ttlMs 过期毫秒；<=0 表示永不过期。
     */
    fun put(key: String, value: Any, ttlMs: Long = 0L) {
        val expireAt = if (ttlMs <= 0L) NEVER_EXPIRE else System.currentTimeMillis() + ttlMs
        memory[key] = Entry(value, expireAt)
    }

    /**
     * 读取缓存并按泛型转换。
     * 过期数据会在读取时自动移除并返回 null。
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> get(key: String): T? {
        val entry = memory[key] ?: return null
        if (isExpired(entry)) {
            memory.remove(key)
            return null
        }
        return entry.value as? T
    }

    /** 删除指定缓存。 */
    fun remove(key: String) {
        memory.remove(key)
    }

    /** 是否存在未过期缓存。 */
    fun contains(key: String): Boolean = get<Any>(key) != null

    /** 当前缓存数量（会先清理过期项）。 */
    fun size(): Int {
        cleanupExpired()
        return memory.size
    }

    /** 清空所有缓存。 */
    fun clear() {
        memory.clear()
    }

    private fun cleanupExpired() {
        val now = System.currentTimeMillis()
        val iterator = memory.entries.iterator()
        while (iterator.hasNext()) {
            val item = iterator.next()
            if (item.value.expireAt <= now) iterator.remove()
        }
    }

    private fun isExpired(entry: Entry): Boolean {
        return entry.expireAt != NEVER_EXPIRE && entry.expireAt <= System.currentTimeMillis()
    }
}
