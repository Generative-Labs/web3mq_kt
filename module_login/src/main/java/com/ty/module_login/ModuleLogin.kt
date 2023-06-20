package com.ty.module_login

import android.content.Context
import android.content.Intent
import com.ty.module_login.activity.StartActivity
import com.ty.module_login.interfaces.LoginSuccessCallback

object ModuleLogin {
    private var callback: LoginSuccessCallback? = null

    /**
     * 启动
     */
    fun launch(context: Context) {

        var intent = Intent(context, StartActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        context.startActivity(intent)
    }

    var onLoginSuccessCallback: LoginSuccessCallback?
        get() = callback
        set(callback) {
            this.callback = callback
        }
}