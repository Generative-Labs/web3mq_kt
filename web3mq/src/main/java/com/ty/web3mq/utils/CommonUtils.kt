package com.ty.web3mq.utils

import android.content.Context
import java.text.SimpleDateFormat
import java.util.*

object CommonUtils {
    val date: String
        get() {
            val date = Date()
            val strDateFormat = "dd/MM/yyyy hh:mm"
            val sdf = SimpleDateFormat(strDateFormat)
            return sdf.format(date)
        }

    fun appendPrefix(categoryType: Int, pbType: Byte, data: ByteArray): ByteArray {
        val length = data.size
        val new_data = ByteArray(length + 2)
        new_data[0] = Integer.valueOf(categoryType).toByte()
        new_data[1] = pbType
        System.arraycopy(data, 0, new_data, 2, length)
        return new_data
    }

    fun dp2px(ctx: Context, dp: Float): Int {
        val scale = ctx.resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }
}