package com.ail.lib_network.http.annotations

import com.ail.lib_network.http.model.NetEvent

/**
 * 網絡請求監控接口。
 *
 * 項目層可以實現此接口，收集每次請求的開始/結束事件，統計耗時、成功率等。
 * 基礎庫通過 [com.ail.lib_network.http.util.NetTracker] 單例調用，不強制依賴 DI。
 */
interface INetTracker {

    /**
     * 收到一個網絡事件（開始或結束）。
     */
    fun onEvent(event: NetEvent)
}
