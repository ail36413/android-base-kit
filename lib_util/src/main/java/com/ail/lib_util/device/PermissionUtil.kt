package com.ail.lib_util.device

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/** 运行时权限工具（检查、筛选、请求）。 */
object PermissionUtil {

    /** 单个权限是否已授予。 */
    fun isGranted(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    /** 多个权限是否全部已授予。 */
    fun areGranted(context: Context, vararg permissions: String): Boolean {
        if (permissions.isEmpty()) return true
        return permissions.all { isGranted(context, it) }
    }

    /** 返回未授予的权限列表。 */
    fun deniedPermissions(context: Context, vararg permissions: String): List<String> {
        if (permissions.isEmpty()) return emptyList()
        return permissions.filterNot { isGranted(context, it) }
    }

    /** 是否应展示请求权限说明（rationale）。 */
    fun shouldShowRationale(activity: Activity, permission: String): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
    }

    /** 发起运行时权限请求（旧 requestCode 方式）。 */
    fun request(activity: Activity, requestCode: Int, vararg permissions: String) {
        if (permissions.isEmpty()) return
        val dedup = permissions.filter { it.isNotBlank() }.distinct().toTypedArray()
        if (dedup.isEmpty()) return
        ActivityCompat.requestPermissions(activity, dedup, requestCode)
    }

    /** 回调中是否全部授权。 */
    fun allGranted(grantResults: IntArray): Boolean {
        return grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }
    }

    /** 打开当前应用设置页。 */
    fun openAppSettings(): Boolean = IntentUtil.openAppSettings()
}

