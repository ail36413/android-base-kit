package com.ail.lib_util.time

import java.util.Calendar

/** 时间范围工具（区间判断、当天起止计算）。 */
object DateRangeUtil {

    /** 判断 [targetMillis] 是否落在 [startMillis]..[endMillis]（闭区间）内。 */
    fun isInRange(targetMillis: Long, startMillis: Long, endMillis: Long): Boolean {
        if (startMillis > endMillis) return false
        return targetMillis in startMillis..endMillis
    }

    /** 计算指定时间所在天的 00:00:00.000。 */
    fun startOfDay(millis: Long): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    /** 计算指定时间所在天的 23:59:59.999。 */
    fun endOfDay(millis: Long): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return cal.timeInMillis
    }

    /** 判断两个区间是否有交集。 */
    fun overlap(start1: Long, end1: Long, start2: Long, end2: Long): Boolean {
        if (start1 > end1 || start2 > end2) return false
        return start1 <= end2 && start2 <= end1
    }
}
