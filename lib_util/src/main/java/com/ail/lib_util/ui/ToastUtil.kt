package com.ail.lib_util.ui

import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.ail.lib_util.UtilKit

/** 主线程安全的 Toast 工具。 */
object ToastUtil {

    private val mainHandler = Handler(Looper.getMainLooper())

    @Volatile
    private var toast: Toast? = null

    /** 显示短时 Toast。 */
    fun showShort(message: CharSequence?) {
        show(message, Toast.LENGTH_SHORT)
    }

    /** 显示长时 Toast。 */
    fun showLong(message: CharSequence?) {
        show(message, Toast.LENGTH_LONG)
    }

    /** 取消当前 Toast。 */
    fun cancel() {
        mainHandler.post {
            toast?.cancel()
            toast = null
        }
    }

    private fun show(message: CharSequence?, duration: Int) {
        if (message.isNullOrEmpty()) return
        UtilKit.requireInit()
        mainHandler.post {
            toast?.cancel()
            toast = Toast.makeText(UtilKit.appContext, message, duration)
            toast?.show()
        }
    }
}
