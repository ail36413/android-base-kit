package com.ail.lib_util.ui

import android.graphics.drawable.Drawable
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.ail.lib_util.UtilKit

/** 资源读取工具（string/color/drawable/dimen）。 */
object ResourceUtil {

    /** 读取字符串资源，支持 format 参数。 */
    fun string(@StringRes resId: Int, vararg args: Any): String {
        UtilKit.requireInit()
        return if (args.isEmpty()) {
            UtilKit.appContext.getString(resId)
        } else {
            UtilKit.appContext.getString(resId, *args)
        }
    }

    /** 读取复数字符串资源。 */
    fun quantityString(@PluralsRes resId: Int, quantity: Int, vararg args: Any): String {
        UtilKit.requireInit()
        return UtilKit.appContext.resources.getQuantityString(resId, quantity, *args)
    }

    /** 读取颜色资源。 */
    fun color(@ColorRes resId: Int): Int {
        UtilKit.requireInit()
        return ContextCompat.getColor(UtilKit.appContext, resId)
    }

    /** 读取 drawable 资源。 */
    fun drawable(@DrawableRes resId: Int): Drawable? {
        UtilKit.requireInit()
        return ContextCompat.getDrawable(UtilKit.appContext, resId)
    }

    /** 读取尺寸资源并返回像素值。 */
    fun dimenPx(@DimenRes resId: Int): Int {
        UtilKit.requireInit()
        return UtilKit.appContext.resources.getDimensionPixelSize(resId)
    }
}
