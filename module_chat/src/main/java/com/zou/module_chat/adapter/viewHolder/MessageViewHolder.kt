package com.zou.module_chat.adapter.viewHolder

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ty.web3mq.utils.DateUtils
import com.zou.module_chat.R

class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var tv_userid: TextView
    var tv_content: TextView
    var tv_timestamp: TextView
    var iv_icon: ImageView? = null

    init {
        tv_userid = itemView.findViewById(R.id.tv_userid)
        tv_content = itemView.findViewById(R.id.tv_content)
        tv_timestamp = itemView.findViewById(R.id.tv_timestamp)
    }

    fun setTv_userid(userid: String?) {
        tv_userid.text = userid
    }

    fun setTv_content(content: String?) {
        tv_content.text = content
    }

    fun setTv_timestamp(timestamp: Long) {
        tv_timestamp.setText(DateUtils.getTimeStringH(timestamp))
    }

    fun setAvatarUrl(avatarUrl: String?, context: Context?) {}
}