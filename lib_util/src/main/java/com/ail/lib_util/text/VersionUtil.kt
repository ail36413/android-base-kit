package com.ail.lib_util.text

/** 语义版本号比较工具（按数字段逐位比较）。 */
object VersionUtil {

    /**
     * 比较两个版本号。
     *
     * @return >0 表示 v1 > v2，<0 表示 v1 < v2，0 表示相等。
     */
    fun compare(v1: String?, v2: String?): Int {
        val left = normalize(v1)
        val right = normalize(v2)
        val max = maxOf(left.size, right.size)
        for (i in 0 until max) {
            val l = left.getOrElse(i) { 0 }
            val r = right.getOrElse(i) { 0 }
            if (l != r) return l.compareTo(r)
        }
        return 0
    }

    /** 判断 [current] 是否大于等于 [target]。 */
    fun isAtLeast(current: String?, target: String?): Boolean = compare(current, target) >= 0

    private fun normalize(version: String?): List<Int> {
        if (version.isNullOrBlank()) return emptyList()
        return version.split('.')
            .map { part -> part.trim().takeWhile { it.isDigit() } }
            .map { it.toIntOrNull() ?: 0 }
    }
}
