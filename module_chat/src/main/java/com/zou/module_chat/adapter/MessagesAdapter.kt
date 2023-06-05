package com.zou.module_chat.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zou.module_chat.R
import com.zou.module_chat.adapter.viewHolder.MessageViewHolder
import com.zou.module_chat.bean.MessageItem
import java.util.ArrayList

class MessagesAdapter(messageList: ArrayList<MessageItem>, context: Context) :
    RecyclerView.Adapter<RecyclerView.ViewHolder?>() {
    private val messageList: ArrayList<MessageItem>
    private var onItemClickListener: OnItemClickListener? = null
    private val context: Context

    init {
        this.messageList = messageList
        this.context = context
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_OTHER) {
            val v: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_message_other, parent, false)
            MessageViewHolder(v)
        } else {
            val v: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_message_mine, parent, false)
            MessageViewHolder(v)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewHolder: MessageViewHolder = holder as MessageViewHolder
        val messageItem: MessageItem = messageList[position]
        viewHolder.setTv_userid(messageItem.from)
        viewHolder.setTv_content(messageItem.content)
        viewHolder.setTv_timestamp(messageItem.timestamp)
        viewHolder.setAvatarUrl(messageItem.avatar_url, context)
    }

    override fun getItemViewType(position: Int): Int {
        val messageItem: MessageItem = messageList[position]
        return if (messageItem.isMine) {
            VIEW_TYPE_MINE
        } else {
            VIEW_TYPE_OTHER
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener?) {
        this.onItemClickListener = onItemClickListener
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    companion object {
        private const val VIEW_TYPE_MINE = 0
        private const val VIEW_TYPE_OTHER = 1
    }
}