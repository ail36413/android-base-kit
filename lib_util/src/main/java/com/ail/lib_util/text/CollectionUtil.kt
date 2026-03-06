package com.ail.lib_util.text

/** 集合空安全处理工具。 */
object CollectionUtil {

    /** 是否为空集合。 */
    fun <T> isNullOrEmpty(collection: Collection<T>?): Boolean = collection.isNullOrEmpty()

    /** 安全 joinToString，集合为空返回空串。 */
    fun <T> joinToStringSafe(collection: Collection<T>?, separator: String = ","): String {
        return collection?.joinToString(separator).orEmpty()
    }

    /** 去重并保持原顺序。 */
    fun <T> distinctStable(list: List<T>?): List<T> = list?.distinct().orEmpty()

    /** 安全分块，size<=0 或空列表返回空。 */
    fun <T> chunkedSafe(list: List<T>?, size: Int): List<List<T>> {
        if (list.isNullOrEmpty() || size <= 0) return emptyList()
        return list.chunked(size)
    }

    /** 构建可变非空列表。 */
    fun <T : Any> mutableListOfNotNull(vararg items: T?): MutableList<T> {
        return items.filterNotNull().toMutableList()
    }
}
