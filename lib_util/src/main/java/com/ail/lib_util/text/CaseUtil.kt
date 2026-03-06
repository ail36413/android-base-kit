package com.ail.lib_util.text

/** 命名风格转换工具（camel/snake/首字母大小写）。 */
object CaseUtil {

    /** 小驼峰或大驼峰转下划线命名。 */
    fun camelToSnake(text: String?): String {
        val raw = text.orEmpty()
        if (raw.isEmpty()) return raw
        return buildString(raw.length + 8) {
            raw.forEachIndexed { index, c ->
                if (c.isUpperCase()) {
                    if (index > 0) append('_')
                    append(c.lowercaseChar())
                } else {
                    append(c)
                }
            }
        }
    }

    /** 下划线命名转小驼峰。 */
    fun snakeToCamel(text: String?): String {
        val raw = text.orEmpty()
        if (raw.isEmpty()) return raw
        val parts = raw.split('_')
        return parts.firstOrNull().orEmpty() + parts.drop(1).joinToString("") {
            it.replaceFirstChar { ch -> ch.uppercaseChar() }
        }
    }

    /** 首字母大写。 */
    fun capitalizeFirst(text: String?): String {
        val raw = text.orEmpty()
        if (raw.isEmpty()) return raw
        return raw.replaceFirstChar { it.uppercaseChar() }
    }

    /** 首字母小写。 */
    fun decapitalizeFirst(text: String?): String {
        val raw = text.orEmpty()
        if (raw.isEmpty()) return raw
        return raw.replaceFirstChar { it.lowercaseChar() }
    }
}
