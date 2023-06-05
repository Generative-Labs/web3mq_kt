package com.ty.module_common.utils

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
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

    fun hideKeyboard(activity: Activity) {
        val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view = activity.currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun copy(context: Context, data: String?) {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val mClipData = ClipData.newPlainText("Label", data)
        cm.setPrimaryClip(mClipData)
    }
}