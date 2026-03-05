package com.ail.android_base_kit.network.http.model

import com.ail.lib_network.http.model.ResponseFieldMapping

/**
 * 网络返回字段映射预设。
 *
 * - STANDARD: code/msg/data
 * - STATUS_MESSAGE: status/message/data
 */
object ResponseMappingPresets {

    fun standardCodeMsgData(): ResponseFieldMapping {
        return ResponseFieldMapping(
            codeKey = "code",
            msgKey = "msg",
            dataKey = "data",
            codeFallbackKeys = listOf("status", "rawCode"),
            msgFallbackKeys = listOf("message"),
            dataFallbackKeys = listOf("json", "payload", "result"),
            defaultMsg = "ok",
            codeValueConverter = { rawCode, mapping ->
                when (rawCode) {
                    null -> mapping.successCode
                    is Boolean -> if (rawCode) mapping.successCode else mapping.failureCode
                    is Number -> rawCode.toInt()
                    is String -> rawCode.toIntOrNull()
                        ?: if (rawCode.equals("true", true)) mapping.successCode else mapping.failureCode
                    else -> mapping.failureCode
                }
            }
        )
    }

    fun statusMessageData(): ResponseFieldMapping {
        return ResponseFieldMapping(
            codeKey = "status",
            msgKey = "message",
            dataKey = "data",
            codeFallbackKeys = listOf("code", "rawCode"),
            msgFallbackKeys = listOf("msg"),
            dataFallbackKeys = listOf("json", "payload", "result"),
            defaultMsg = "ok",
            codeValueConverter = { rawCode, mapping ->
                when (rawCode) {
                    null -> mapping.successCode
                    is Boolean -> if (rawCode) mapping.successCode else mapping.failureCode
                    is Number -> rawCode.toInt()
                    is String -> rawCode.toIntOrNull()
                        ?: if (rawCode.equals("true", true)) mapping.successCode else mapping.failureCode
                    else -> mapping.failureCode
                }
            }
        )
    }
}

