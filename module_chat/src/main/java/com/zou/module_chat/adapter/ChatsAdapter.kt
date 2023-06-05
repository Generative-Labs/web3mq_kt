package com.zou.module_chat.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zou.module_chat.R
import com.zou.module_chat.adapter.viewHolder.ChatsViewHolder
import com.zou.module_chat.bean.ChatItem
import java.util.ArrayList

class ChatsAdapter(chatList: ArrayList<ChatItem>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder?>() {
    private val chatList: ArrayList<ChatItem>
    private var onItemClickListener: OnItemClickListener? = null

    init {
        this.chatList = chatList
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat, parent, false)
        return ChatsViewHolder(v)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewHolder: ChatsViewHolder = holder as ChatsViewHolder
        val chat: ChatItem = chatList[position]
        when (chat.chat_type) {
            ChatItem.CHAT_TYPE_USER -> viewHolder.setIv_icon(R.mipmap.ic_dm)
            ChatItem.CHAT_TYPE_GROUP -> viewHolder.setIv_icon(R.mipmap.ic_group)
        }
        viewHolder.setTv_title(chat.title)
        viewHolder.setTv_content(chat.content)
        viewHolder.setTv_timestamp(chat.timestamp)
        viewHolder.setTv_unread_count(chat.unreadCount)
        holder.itemView.setOnClickListener(View.OnClickListener {
            if (onItemClickListener != null) {
                onItemClickListener!!.onItemClick(position)
            }
        })
    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener?) {
        this.onItemClickListener = onItemClickListener
    }

    //    public void updateItem() {
    //        notifyItemChanged();
    //    }
    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }
}