package com.zou.module_chat.adapter.viewHolder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ty.web3mq.utils.AppUtils
import com.ty.web3mq.utils.DateUtils
import com.zou.module_chat.R

class ChatsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var tv_title: TextView
    var tv_content: TextView
    var tv_timestamp: TextView
    var tv_unread_count: TextView
    var iv_icon: ImageView

    init {
        tv_title = itemView.findViewById(R.id.tv_title)
        tv_content = itemView.findViewById(R.id.tv_content)
        tv_timestamp = itemView.findViewById(R.id.tv_timestamp)
        iv_icon = itemView.findViewById(R.id.iv_icon)
        tv_unread_count = itemView.findViewById(R.id.tv_unread_count)
    }

    fun setTv_title(title: String?) {
        tv_title.text = title
    }

    fun setTv_content(content: String?) {
        tv_content.text = content
    }

    fun setTv_timestamp(timestamp: Long) {
        if (timestamp != 0L) {
            val date: String = DateUtils.getTimeString(timestamp)
            tv_timestamp.text = date
        } else {
            tv_timestamp.text = ""
        }
    }

    fun setIv_icon(drawable_id: Int) {
        iv_icon.setImageDrawable(itemView.context.getDrawable(drawable_id))
    }

    fun setTv_unread_count(count: Int) {
        if (count > 0) {
            tv_unread_count.visibility = View.VISIBLE
            tv_unread_count.text = count.toString() + ""
        } else {
            tv_unread_count.visibility = View.GONE
        }
    }
}