package com.ail.lib_util.ui

import android.view.View

/** View 状态快捷工具。 */
object ViewUtil {

    /** 设置为 VISIBLE。 */
    fun visible(view: View?) {
        view?.visibility = View.VISIBLE
    }

    /** 设置为 INVISIBLE。 */
    fun invisible(view: View?) {
        view?.visibility = View.INVISIBLE
    }

    /** 设置为 GONE。 */
    fun gone(view: View?) {
        view?.visibility = View.GONE
    }

    /**
     * 按条件设置可见性。
     *
     * @param goneWhenFalse false 时为 true 使用 GONE，否则使用 INVISIBLE。
     */
    fun setVisible(view: View?, visible: Boolean, goneWhenFalse: Boolean = true) {
        view?.visibility = if (visible) {
            View.VISIBLE
        } else if (goneWhenFalse) {
            View.GONE
        } else {
            View.INVISIBLE
        }
    }

    /** 批量设置 enabled 状态。 */
    fun setEnabled(enabled: Boolean, vararg views: View?) {
        views.forEach { it?.isEnabled = enabled }
    }

    /** 批量设置 selected 状态。 */
    fun setSelected(selected: Boolean, vararg views: View?) {
        views.forEach { it?.isSelected = selected }
    }
}
