package com.ail.lib_util.ui

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

/** 软键盘显示/隐藏工具。 */
object KeyboardUtil {

    /** 请求显示软键盘并让目标 View 获取焦点。 */
    fun show(view: View) {
        view.requestFocus()
        view.post {
            val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    /** 根据 Activity 当前焦点隐藏软键盘。 */
    fun hide(activity: Activity) {
        val targetView = activity.currentFocus ?: activity.window?.decorView ?: return
        hide(targetView)
    }

    /** 对指定 View 所在窗口隐藏软键盘。 */
    fun hide(view: View) {
        val token = view.windowToken ?: return
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(token, 0)
    }
}
