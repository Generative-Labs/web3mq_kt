package com.ty.module_profile

import android.content.Context
import android.content.Intent
import com.ty.module_common.config.Constants
import com.ty.module_profile.activity.OtherProfileActivity


object ModuleProfile  {
    var onLogoutEvent: OnLogoutEvent? = null
    var onChatEvent: OnChatEvent? = null

    interface OnLogoutEvent {
        fun onLogout()
    }

    interface OnChatEvent {
        fun onChat(userid: String?)
    }

    fun toOtherProfile(context: Context, userid: String?) {
        val intent = Intent(context , OtherProfileActivity::class.java)
        intent.putExtra(Constants.ROUTER_KEY_USER_ID, userid)
        context.startActivity(intent)
    }
}