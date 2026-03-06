package com.ail.lib_util

/**
 * Global configuration for lib_util.
 */
data class UtilConfig(
    /** 是否启用 Timber 日志树。 */
    val enableTimber: Boolean = true,
    /** Debug 模式日志开关；false 时默认仅保留 error 级别。 */
    val debugLog: Boolean = false,
    /** 日志默认 tag 前缀。 */
    val tagPrefix: String = "UtilKit",
    /** MMKV 自定义根目录；为空时使用默认目录。 */
    val mmkvRootDir: String? = null,
)
