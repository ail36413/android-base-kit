package com.ail.lib_util.device

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.ail.lib_util.UtilKit

/** 常用系统 Intent 调用工具。 */
object IntentUtil {

    /**
     * 打开浏览器。
     * 未带协议时默认补全为 https://。
     */
    fun openBrowser(url: String): Boolean {
        if (url.isBlank()) return false
        val finalUrl = if (url.startsWith("http://") || url.startsWith("https://")) url else "https://$url"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(finalUrl))
        return safeStart(intent)
    }

    /** 调用系统分享文本面板。 */
    fun shareText(text: String, title: String = "Share"): Boolean {
        if (text.isBlank()) return false
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        val chooser = Intent.createChooser(shareIntent, title)
        return safeStart(chooser)
    }

    /** 打开当前应用设置详情页。 */
    fun openAppSettings(): Boolean {
        UtilKit.requireInit()
        val packageName = UtilKit.appContext.packageName
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:$packageName")
        }
        return safeStart(intent)
    }

    /** 判断系统是否可处理该 Intent。 */
    fun canHandle(intent: Intent): Boolean {
        UtilKit.requireInit()
        return intent.resolveActivity(UtilKit.appContext.packageManager) != null
    }

    /**
     * 安全启动 Intent：自动加 NEW_TASK，先做可处理检查。
     *
     * @return 启动成功返回 true，失败返回 false。
     */
    fun safeStart(intent: Intent): Boolean {
        return try {
            UtilKit.requireInit()
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (!canHandle(intent)) return false
            UtilKit.appContext.startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
    }
}
