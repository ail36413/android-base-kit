package com.ail.lib_util.storage

import com.ail.lib_util.UtilKit
import com.tencent.mmkv.MMKV
import kotlin.jvm.JvmName

/**
 * Simple MMKV wrapper with default namespace.
 */
object KvUtil {

    /**
     * 获取 MMKV 实例。
     *
     * @param mmapId 命名空间；为空时使用默认实例。
     */
    private fun kv(mmapId: String? = null): MMKV {
        UtilKit.requireInit()
        return if (mmapId.isNullOrBlank()) {
            MMKV.defaultMMKV()
        } else {
            MMKV.mmkvWithID(mmapId)
        }
    }

    /** 存储字符串。 */
    fun putString(key: String, value: String?, mmapId: String? = null): Boolean =
        kv(mmapId).encode(key, value)

    /** 读取字符串。 */
    fun getString(key: String, default: String? = null, mmapId: String? = null): String? =
        kv(mmapId).decodeString(key, default)

    /** 存储整型。 */
    fun putInt(key: String, value: Int, mmapId: String? = null): Boolean =
        kv(mmapId).encode(key, value)

    /** 读取整型。 */
    fun getInt(key: String, default: Int = 0, mmapId: String? = null): Int =
        kv(mmapId).decodeInt(key, default)

    /** 存储长整型。 */
    fun putLong(key: String, value: Long, mmapId: String? = null): Boolean =
        kv(mmapId).encode(key, value)

    /** 读取长整型。 */
    fun getLong(key: String, default: Long = 0L, mmapId: String? = null): Long =
        kv(mmapId).decodeLong(key, default)

    /** 存储布尔值。 */
    fun putBoolean(key: String, value: Boolean, mmapId: String? = null): Boolean =
        kv(mmapId).encode(key, value)

    /** 读取布尔值。 */
    fun getBoolean(key: String, default: Boolean = false, mmapId: String? = null): Boolean =
        kv(mmapId).decodeBool(key, default)

    /** 存储单精度浮点数。 */
    fun putFloat(key: String, value: Float, mmapId: String? = null): Boolean =
        kv(mmapId).encode(key, value)

    /** 读取单精度浮点数。 */
    fun getFloat(key: String, default: Float = 0f, mmapId: String? = null): Float =
        kv(mmapId).decodeFloat(key, default)

    /** 存储双精度浮点数。 */
    fun putDouble(key: String, value: Double, mmapId: String? = null): Boolean =
        kv(mmapId).encode(key, value)

    /** 读取双精度浮点数。 */
    fun getDouble(key: String, default: Double = 0.0, mmapId: String? = null): Double =
        kv(mmapId).decodeDouble(key, default)

    /** 存储字节数组。 */
    fun putBytes(key: String, value: ByteArray?, mmapId: String? = null): Boolean =
        kv(mmapId).encode(key, value)

    /** 读取字节数组。 */
    fun getBytes(key: String, default: ByteArray? = null, mmapId: String? = null): ByteArray? =
        kv(mmapId).decodeBytes(key, default)

    /** 存储字符串集合。 */
    fun putStringSet(key: String, value: Set<String>?, mmapId: String? = null): Boolean =
        kv(mmapId).encode(key, value)

    /** 读取字符串集合。 */
    fun getStringSet(key: String, default: Set<String>? = null, mmapId: String? = null): Set<String>? =
        kv(mmapId).decodeStringSet(key, default)

    /** 删除单个 key。 */
    fun remove(key: String, mmapId: String? = null) {
        kv(mmapId).removeValueForKey(key)
    }

    /** 批量删除 key（Array 版本）。 */
    fun removeKeys(keys: Array<String>, mmapId: String? = null) {
        kv(mmapId).removeValuesForKeys(keys)
    }

    /** 批量删除 key（vararg 版本）。 */
    @JvmName("removeKeysVararg")
    fun removeKeys(vararg keys: String, mmapId: String? = null) {
        if (keys.isEmpty()) return
        kv(mmapId).removeValuesForKeys(keys)
    }

    /** 清空指定命名空间全部数据。 */
    fun clearAll(mmapId: String? = null) {
        kv(mmapId).clearAll()
    }

    /** 判断是否存在指定 key。 */
    fun containsKey(key: String, mmapId: String? = null): Boolean =
        kv(mmapId).containsKey(key)

    /** 获取所有 key。 */
    fun allKeys(mmapId: String? = null): Array<String> = kv(mmapId).allKeys() ?: emptyArray()

    /** 获取数据条目数量。 */
    fun count(mmapId: String? = null): Long = kv(mmapId).count()
}
