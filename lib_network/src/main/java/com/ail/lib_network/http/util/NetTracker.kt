package com.ail.lib_network.http.util

import com.ail.lib_network.http.annotations.INetTracker
import com.ail.lib_network.http.model.NetEvent

/**
 * 全局網絡監控分發器。
 *
 * - 默認不設置 delegate，不產生任何開銷；
 * - 項目層如需監控，僅需在合適時機設置 [delegate] 為自定義的 [INetTracker] 實現。
 *
 * 例如：
 *
 * ```kotlin
 * class AppNetTracker : INetTracker { ... }
 *
 * NetTracker.delegate = AppNetTracker()
 * ```
 */
object NetTracker {

    @Volatile
    var delegate: INetTracker? = null

    fun track(event: NetEvent) {
        delegate?.onEvent(event)
    }
}
