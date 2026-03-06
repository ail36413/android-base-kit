package com.ail.lib_util.device

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import com.ail.lib_util.UtilKit

/** 剪贴板读写工具。 */
object ClipboardUtil {

    /**
     * 复制文本到系统剪贴板。
     *
     * @param text 需要复制的内容。
     * @param label 剪贴板标签，便于系统或其他应用识别来源。
     */
    fun copyText(text: CharSequence, label: CharSequence = "text") {
        UtilKit.requireInit()
        val clipboardManager = UtilKit.appContext
            .getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            ?: return
        clipboardManager.setPrimaryClip(ClipData.newPlainText(label, text))
    }

    /**
     * 读取剪贴板首条文本。
     *
     * @return 无内容或读取失败时返回空串。
     */
    fun getText(): String {
        UtilKit.requireInit()
        val clipboardManager = UtilKit.appContext
            .getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            ?: return ""
        val clipData = clipboardManager.primaryClip ?: return ""
        if (clipData.itemCount <= 0) return ""
        return clipData.getItemAt(0).coerceToText(UtilKit.appContext)?.toString().orEmpty()
    }

    /** 当前剪贴板是否存在主内容。 */
    fun hasText(): Boolean {
        UtilKit.requireInit()
        val clipboardManager = UtilKit.appContext
            .getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            ?: return false
        return clipboardManager.hasPrimaryClip()
    }
}
