package com.ail.lib_util.device

import android.app.ActivityManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Build
import com.ail.lib_util.UtilKit

/** 应用基础信息读取工具。 */
object AppInfoUtil {

    /** 获取当前应用包名。 */
    fun packageName(): String {
        UtilKit.requireInit()
        return UtilKit.appContext.packageName
    }

    /** 获取版本名，异常时返回空串。 */
    fun versionName(): String {
        UtilKit.requireInit()
        val pm = UtilKit.appContext.packageManager
        return try {
            val pi = pm.getPackageInfo(UtilKit.appContext.packageName, 0)
            pi.versionName ?: ""
        } catch (_: Exception) {
            ""
        }
    }

    /** 获取版本号（longVersionCode），异常时返回 0。 */
    fun versionCode(): Long {
        UtilKit.requireInit()
        val pm = UtilKit.appContext.packageManager
        return try {
            val pi = pm.getPackageInfo(UtilKit.appContext.packageName, 0)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) pi.longVersionCode else pi.versionCode.toLong()
        } catch (_: Exception) {
            0L
        }
    }

    /** 当前应用是否为 Debuggable。 */
    fun isDebuggable(): Boolean {
        UtilKit.requireInit()
        return (UtilKit.appContext.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }

    /** 当前进程是否为主进程。 */
    fun isMainProcess(): Boolean {
        UtilKit.requireInit()
        val am = UtilKit.appContext.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            ?: return true
        val myPid = android.os.Process.myPid()
        val current = am.runningAppProcesses?.firstOrNull { it.pid == myPid }?.processName
        return current == UtilKit.appContext.packageName
    }
}
