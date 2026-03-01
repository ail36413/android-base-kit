package com.ail.lib_network.http.util

/**
 * 網絡錯誤文案提供器。
 *
 * - 默認直接返回基礎庫內部定義的中文提示；
 * - 項目層可在 Application 啟動時覆蓋 [provider]，實現多語言或自定義文案。
 *
 * 例如：
 *
 * ```kotlin
 * NetErrorMessage.provider = { code, defaultMsg ->
 *     when (code) {
 *         -1 -> context.getString(R.string.error_timeout)
 *         else -> defaultMsg
 *     }
 * }
 * ```
 */
object NetErrorMessage {

    @Volatile
    var provider: (code: Int, defaultMessage: String) -> String = { _, default -> default }

    fun msg(code: Int, defaultMessage: String): String = provider(code, defaultMessage)
}
