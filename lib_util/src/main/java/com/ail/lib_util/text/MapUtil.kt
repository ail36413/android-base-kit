package com.ail.lib_util.text

/** Map 常见操作工具。 */
object MapUtil {

    /**
     * 从 Map 读取值，不存在时返回 [defaultValue]。
     */
    fun <K, V> getOrDefault(map: Map<K, V>?, key: K, defaultValue: V): V {
        return map?.get(key) ?: defaultValue
    }

    /**
     * 合并两个 Map，后者同 key 会覆盖前者。
     */
    fun <K, V> merge(base: Map<K, V>?, override: Map<K, V>?): Map<K, V> {
        if (base.isNullOrEmpty()) return override.orEmpty()
        if (override.isNullOrEmpty()) return base
        return LinkedHashMap<K, V>(base.size + override.size).apply {
            putAll(base)
            putAll(override)
        }
    }

    /**
     * 过滤掉 value 为 null 的键值对。
     */
    fun <K, V> filterNotNullValues(map: Map<K, V?>?): Map<K, V> {
        if (map.isNullOrEmpty()) return emptyMap()
        val result = LinkedHashMap<K, V>(map.size)
        map.forEach { (k, v) ->
            if (v != null) result[k] = v
        }
        return result
    }

    /** 返回可变副本；原 Map 为空时返回空 MutableMap。 */
    fun <K, V> toMutable(map: Map<K, V>?): MutableMap<K, V> {
        return map?.toMutableMap() ?: mutableMapOf()
    }
}
