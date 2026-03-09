package com.ail.lib_util.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.ail.lib_util.UtilKit

/** 屏幕与系统栏信息工具（宽高、方向、状态栏/导航栏高度）。 */
object ScreenUtil {

    /** 基于 WindowInsets 的系统栏信息。 */
    data class SystemBarInsets(
        val statusBarTop: Int,
        val navigationBarBottom: Int,
        val left: Int,
        val right: Int,
    )

    /** 屏幕宽度（px）。 */
    fun screenWidthPx(context: Context? = null): Int = displayMetrics(context).widthPixels

    /** 屏幕高度（px）。 */
    fun screenHeightPx(context: Context? = null): Int = displayMetrics(context).heightPixels

    /** 屏幕密度 DPI。 */
    fun densityDpi(context: Context? = null): Int = displayMetrics(context).densityDpi

    /** 是否横屏。 */
    fun isLandscape(context: Context? = null): Boolean {
        return configuration(context).orientation == Configuration.ORIENTATION_LANDSCAPE
    }

    /** 是否竖屏。 */
    fun isPortrait(context: Context? = null): Boolean {
        return configuration(context).orientation == Configuration.ORIENTATION_PORTRAIT
    }

    /**
     * 读取 View 当前系统栏 Insets。
     *
     * 适用于已 attach 到窗口的 View；取不到 Insets 时返回 null。
     */
    fun systemBarInsets(view: View): SystemBarInsets? {
        val insets = ViewCompat.getRootWindowInsets(view)
            ?.getInsets(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
            ?: return null
        return SystemBarInsets(
            statusBarTop = insets.top,
            navigationBarBottom = insets.bottom,
            left = insets.left,
            right = insets.right,
        )
    }

    /** 状态栏高度（px）。 */
    fun statusBarHeight(context: Context? = null): Int {
        return systemDimenPx("status_bar_height", context)
    }

    /** 导航栏高度（px）。 */
    fun navigationBarHeight(context: Context? = null): Int {
        if (!hasNavigationBar(context)) return 0
        return systemDimenPx("navigation_bar_height", context)
    }

    /** 是否存在导航栏（资源维度推断）。 */
    @SuppressLint("InternalInsetResource", "DiscouragedApi")
    fun hasNavigationBar(context: Context? = null): Boolean {
        val res = resources(context)
        val id = res.getIdentifier("navigation_bar_height", "dimen", "android")
        return id > 0 && res.getDimensionPixelSize(id) > 0
    }

    @SuppressLint("DiscouragedApi")
    private fun systemDimenPx(name: String, context: Context?): Int {
        val res = resources(context)
        val id = res.getIdentifier(name, "dimen", "android")
        return if (id > 0) res.getDimensionPixelSize(id) else 0
    }

    private fun displayMetrics(context: Context?) = resources(context).displayMetrics

    private fun configuration(context: Context?) = resources(context).configuration

    private fun resources(context: Context?): Resources = when {
        context != null -> context.resources
        UtilKit.isInitialized() -> UtilKit.appContext.resources
        else -> Resources.getSystem()
    }

    /**
     * 可用内容区宽度（px）。
     *
     * 优先基于 [view] 的 WindowInsets，失败时回退屏幕宽度减左右系统栏。
     */
    fun availableContentWidthPx(view: View? = null, context: Context? = null): Int {
        val screenWidth = screenWidthPx(context)
        val insets = view?.let { systemBarInsets(it) }
        return if (insets != null) {
            (screenWidth - insets.left - insets.right).coerceAtLeast(0)
        } else {
            screenWidth.coerceAtLeast(0)
        }
    }

    /**
     * 可用内容区高度（px）。
     *
     * 优先基于 [view] 的 WindowInsets，失败时回退屏幕高度减状态栏和导航栏。
     */
    fun availableContentHeightPx(view: View? = null, context: Context? = null): Int {
        val screenHeight = screenHeightPx(context)
        val insets = view?.let { systemBarInsets(it) }
        return if (insets != null) {
            (screenHeight - insets.statusBarTop - insets.navigationBarBottom).coerceAtLeast(0)
        } else {
            (screenHeight - statusBarHeight(context) - navigationBarHeight(context)).coerceAtLeast(0)
        }
    }
}
