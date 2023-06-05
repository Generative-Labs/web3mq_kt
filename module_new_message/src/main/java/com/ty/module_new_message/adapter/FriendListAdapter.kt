package com.ty.module_new_message.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.recyclerview.widget.RecyclerView
import com.ty.module_new_message.R
import com.ty.module_new_message.adapter.viewHolder.FriendListViewHolder
import com.ty.web3mq.http.beans.FollowerBean
import java.util.ArrayList

class FriendListAdapter(followItems: ArrayList<FollowerBean>, context: Context) :
    RecyclerView.Adapter<RecyclerView.ViewHolder?>() {
    private val followItems: ArrayList<FollowerBean>
    private val context: Context
    private var style = STYLE_DEFAULT
    private val checkedFollower: ArrayList<FollowerBean> = ArrayList<FollowerBean>()
    private var onItemClickListener: OnItemClickListener? = null

    init {
        this.followItems = followItems
        this.context = context
    }

    fun setStyle(style: String) {
        this.style = style
    }

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener?) {
        this.onItemClickListener = onItemClickListener
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_friend_list, parent, false)
        return FriendListViewHolder(v, context)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewHolder: FriendListViewHolder = holder as FriendListViewHolder
        val bean: FollowerBean = followItems[position]
        viewHolder.setStyle(style)
        viewHolder.setFollower(bean)
        viewHolder.checkbox.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                checkedFollower.add(bean)
            } else {
                checkedFollower.remove(bean)
            }
        })
        viewHolder.itemView.setOnClickListener(View.OnClickListener {
            if (onItemClickListener != null) {
                onItemClickListener!!.onItemClick(position)
            }
        })
    }

    fun getCheckedFollower(): ArrayList<FollowerBean> {
        return checkedFollower
    }

    override fun getItemCount(): Int {
        return followItems.size
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    companion object {
        const val STYLE_DEFAULT = "default"
        const val STYLE_SEARCH = "search"
        const val STYLE_CREATE_ROOM = "create_room"
    }
}