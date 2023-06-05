package com.zou.module_chat

import android.content.Context
import android.content.Intent
import com.ty.module_common.config.Constants
import com.zou.module_chat.activity.MessageActivity

object ModuleChat {
    var toNewMessageRequestListener: ToNewMessageRequestListener? = null
    fun toMessageUI(context: Context, chat_type: String?, chat_id: String?) {
        val intent = Intent(context, MessageActivity::class.java)
        intent.putExtra(Constants.ROUTER_KEY_CHAT_TYPE, chat_type)
        intent.putExtra(Constants.ROUTER_KEY_CHAT_ID, chat_id)
        context.startActivity(intent)
    }

    interface ToNewMessageRequestListener {
        fun toRequestFollow(userid: String?)
    }
}