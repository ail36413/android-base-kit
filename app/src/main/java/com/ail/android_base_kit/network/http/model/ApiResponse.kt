package com.bohai.model

import com.bohai.android_base_kit.model.StatusResponse

/**
 * 全局默认返回模型别名。
 * 只需在这里切换类型，所有接口统一生效。
 */
typealias ApiResponse<T> = StatusResponse<T>

