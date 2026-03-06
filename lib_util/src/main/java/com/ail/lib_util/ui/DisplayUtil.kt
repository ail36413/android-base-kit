package com.ail.lib_util.ui

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.util.TypedValue
import com.ail.lib_util.UtilKit

/** 尺寸单位换算工具（dp/sp/px）。 */
object DisplayUtil {

    /** dp 转 px（使用默认上下文）。 */
    fun dp2px(dp: Float): Int = dp2px(dp, null)

    /** dp 转 px。 */
    fun dp2px(dp: Float, context: Context?): Int {
        val density = displayMetrics(context).density
        return (dp * density + 0.5f).toInt()
    }

    /** px 转 dp（使用默认上下文）。 */
    fun px2dp(px: Float): Int = px2dp(px, null)

    /** px 转 dp。 */
    fun px2dp(px: Float, context: Context?): Int {
        val density = displayMetrics(context).density
        return (px / density + 0.5f).toInt()
    }

    /** sp 转 px（使用默认上下文）。 */
    fun sp2px(sp: Float): Int = sp2px(sp, null)

    /** sp 转 px。 */
    fun sp2px(sp: Float, context: Context?): Int {
        val px = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            sp,
            displayMetrics(context),
        )
        return (px + 0.5f).toInt()
    }

    /** px 转 sp（使用默认上下文）。 */
    fun px2sp(px: Float): Int = px2sp(px, null)

    /** px 转 sp。 */
    fun px2sp(px: Float, context: Context?): Int {
        val metrics = displayMetrics(context)
        val scaledDensity = metrics.density * displayConfiguration(context).fontScale
        return (px / scaledDensity + 0.5f).toInt()
    }

    // Aliases for better readability.
    /** `dp2px` 的语义化别名。 */
    fun dpToPx(dp: Float, context: Context? = null): Int = dp2px(dp, context)

    /** `px2dp` 的语义化别名。 */
    fun pxToDp(px: Float, context: Context? = null): Int = px2dp(px, context)

    /** `sp2px` 的语义化别名。 */
    fun spToPx(sp: Float, context: Context? = null): Int = sp2px(sp, context)

    /** `px2sp` 的语义化别名。 */
    fun pxToSp(px: Float, context: Context? = null): Int = px2sp(px, context)

    private fun displayMetrics(context: Context?) = when {
        context != null -> context.resources.displayMetrics
        UtilKit.isInitialized() -> UtilKit.appContext.resources.displayMetrics
        else -> Resources.getSystem().displayMetrics
    }

    private fun displayConfiguration(context: Context?): Configuration = when {
        context != null -> context.resources.configuration
        UtilKit.isInitialized() -> UtilKit.appContext.resources.configuration
        else -> Resources.getSystem().configuration
    }
}
