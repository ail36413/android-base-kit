package com.ail.lib_util.text

/** 模板占位替换工具，默认占位语法为 `%{key}`。 */
object TemplateUtil {

    private val pattern = Regex("%\\{([a-zA-Z0-9_]+)}")

    /**
     * 按 [values] 对模板进行替换。
     * 未提供值的 key 会替换为空串。
     */
    fun render(template: String?, values: Map<String, Any?>): String {
        val raw = template.orEmpty()
        if (raw.isEmpty() || values.isEmpty()) return raw
        return pattern.replace(raw) { match ->
            val key = match.groupValues[1]
            values[key]?.toString().orEmpty()
        }
    }

    /** 提取模板中的全部占位 key（去重后返回）。 */
    fun keys(template: String?): List<String> {
        val raw = template.orEmpty()
        if (raw.isEmpty()) return emptyList()
        return pattern.findAll(raw).map { it.groupValues[1] }.distinct().toList()
    }
}
