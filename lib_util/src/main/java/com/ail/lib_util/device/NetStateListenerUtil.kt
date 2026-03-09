package com.ail.lib_util.device

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.Build
import com.ail.lib_util.UtilKit
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * 网络状态监听工具（回调式）。
 *
 * 依赖权限：`android.permission.ACCESS_NETWORK_STATE`（库清单已声明，默认会随 manifest merge 注入）。
 */
object NetStateListenerUtil {

    private val callbackMap = ConcurrentHashMap<String, ConnectivityManager.NetworkCallback>()

    /**
     * 注册网络状态监听。
     *
     * - 注册成功后会立刻回调一次当前网络状态。
     * - 返回 token 可用于单独取消监听。
     *
     * @param listener 网络变化回调；connected 表示当前是否可用，type 为当前网络类型。
     * @return 监听 token；失败返回空串。
     */
    @SuppressLint("MissingPermission")
    fun register(listener: (connected: Boolean, type: NetworkUtil.NetworkType) -> Unit): String {
        val cm = connectivityManager() ?: return ""
        val token = UUID.randomUUID().toString()
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                listener(true, NetworkUtil.networkType())
            }

            override fun onLost(network: Network) {
                listener(NetworkUtil.isConnected(), NetworkUtil.networkType())
            }

            override fun onUnavailable() {
                listener(false, NetworkUtil.NetworkType.NONE)
            }
        }

        val registered = runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                cm.registerDefaultNetworkCallback(callback)
            } else {
                val request = NetworkRequest.Builder().build()
                cm.registerNetworkCallback(request, callback)
            }
            true
        }.getOrDefault(false)

        if (!registered) return ""
        callbackMap[token] = callback
        listener(NetworkUtil.isConnected(), NetworkUtil.networkType())
        return token
    }

    /**
     * 注销指定 token 的监听。
     *
     * @param token 注册时返回的监听 token；为空或无效时忽略。
     */
    fun unregister(token: String?) {
        if (token.isNullOrBlank()) return
        val callback = callbackMap.remove(token) ?: return
        val cm = connectivityManager() ?: return
        runCatching { cm.unregisterNetworkCallback(callback) }
    }

    /** 注销全部监听（建议在页面销毁或进程退出前调用）。 */
    fun unregisterAll() {
        callbackMap.keys.toList().forEach { unregister(it) }
    }

    /** 获取系统网络服务；未初始化 UtilKit 时会抛出初始化异常。 */
    private fun connectivityManager(): ConnectivityManager? {
        UtilKit.requireInit()
        return UtilKit.appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
    }
}
