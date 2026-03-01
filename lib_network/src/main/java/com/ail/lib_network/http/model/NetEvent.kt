package com.ail.lib_network.http.model

/**
 * 网络事件阶段：開始或結束。
 */
enum class NetEventStage {
    START,
    END
}

/**
 * 網絡請求監控事件。
 *
 * @param name 可選的請求名稱標識（如「getUserInfo」），默認可為 null
 * @param stage 事件階段：開始或結束
 * @param timestampMs 事件發生時間（毫秒）
 * @param durationMs 請求持續時間（毫秒），僅在 END 階段有效
 * @param resultType 結果類型標識，如 SUCCESS / TECHNICAL_FAILURE / BUSINESS_FAILURE
 * @param errorCode 技術或業務錯誤碼，成功時為 null
 * @param tag 可選的業務上下文標識（例如："uploadUserAvatar"），便於外部監控關聯請求
 */
data class NetEvent(
    val name: String?,
    val stage: NetEventStage,
    val timestampMs: Long,
    val durationMs: Long? = null,
    val resultType: String? = null,
    val errorCode: Int? = null,
    val tag: String? = null
)
