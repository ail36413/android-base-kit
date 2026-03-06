package com.ail.lib_util.device

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import com.ail.lib_util.UtilKit

/** 网络状态工具（依赖 ACCESS_NETWORK_STATE 权限）。 */
object NetworkUtil {

    /** 网络类型枚举。 */
    enum class NetworkType {
        NONE,
        WIFI,
        CELLULAR,
        ETHERNET,
        VPN,
        OTHER
    }

    /** 是否存在可用网络。 */
    fun isConnected(): Boolean = networkType() != NetworkType.NONE

    /** 是否为 Wi-Fi 网络。 */
    fun isWifi(): Boolean = networkType() == NetworkType.WIFI

    /** 是否为蜂窝网络。 */
    fun isCellular(): Boolean = networkType() == NetworkType.CELLULAR

    /** 是否为 VPN 网络。 */
    fun isVpn(): Boolean = networkType() == NetworkType.VPN

    /** 当前网络是否计费。 */
    @SuppressLint("MissingPermission")
    fun isMetered(): Boolean {
        val cm = connectivityManager() ?: return false
        return cm.isActiveNetworkMetered
    }

    /** 获取当前网络类型，不可用时返回 [NetworkType.NONE]。 */
    @SuppressLint("MissingPermission")
    fun networkType(): NetworkType {
        val cm = connectivityManager() ?: return NetworkType.NONE

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = cm.activeNetwork ?: return NetworkType.NONE
            val capabilities = cm.getNetworkCapabilities(network) ?: return NetworkType.NONE
            return when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkType.WIFI
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkType.CELLULAR
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkType.ETHERNET
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> NetworkType.VPN
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> NetworkType.OTHER
                else -> NetworkType.OTHER
            }
        }

        @Suppress("DEPRECATION")
        val legacyInfo = cm.activeNetworkInfo ?: return NetworkType.NONE
        @Suppress("DEPRECATION")
        if (!legacyInfo.isConnected) return NetworkType.NONE
        @Suppress("DEPRECATION")
        return when (legacyInfo.type) {
            ConnectivityManager.TYPE_WIFI -> NetworkType.WIFI
            ConnectivityManager.TYPE_MOBILE -> NetworkType.CELLULAR
            ConnectivityManager.TYPE_ETHERNET -> NetworkType.ETHERNET
            ConnectivityManager.TYPE_VPN -> NetworkType.VPN
            else -> NetworkType.OTHER
        }
    }

    private fun connectivityManager(): ConnectivityManager? {
        UtilKit.requireInit()
        return UtilKit.appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
    }
}
