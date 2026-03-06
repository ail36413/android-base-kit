package com.ail.lib_util.text

/** 布尔值解析与转换工具。 */
object BooleanUtil {

    private val trueSet = setOf("1", "true", "yes", "y", "on")
    private val falseSet = setOf("0", "false", "no", "n", "off")

    /**
     * 将文本解析为布尔值。
     *
     * @param text 待解析文本，支持 1/0、true/false、yes/no、on/off。
     * @param defaultValue 无法识别时返回的默认值。
     */
    fun parse(text: String?, defaultValue: Boolean = false): Boolean {
        val value = text?.trim()?.lowercase().orEmpty()
        if (value in trueSet) return true
        if (value in falseSet) return false
        return defaultValue
    }

    /** 返回布尔值取反结果。 */
    fun toggle(value: Boolean): Boolean = !value

    /** 将布尔值映射为 1/0。 */
    fun toInt(value: Boolean): Int = if (value) 1 else 0
}
