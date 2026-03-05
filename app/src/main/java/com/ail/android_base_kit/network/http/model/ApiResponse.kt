package com.ail.android_base_kit.network.http.model

import com.ail.lib_network.http.model.GlobalResponse

/**
 * 全局默认返回模型别名。
 * 字段映射在 NetworkConfig.responseFieldMapping 中统一配置。
 */
typealias ApiResponse<T> = GlobalResponse<T>
