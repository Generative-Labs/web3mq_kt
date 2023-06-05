package com.ty.module_contact.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ty.module_contact.R
import com.ty.module_contact.bean.FollowItem
import com.ty.module_contact.adapter.viewHolder.FollowersViewHolder
import java.util.ArrayList

class FollowersAdapter(
    private val followItems: ArrayList<FollowItem>,
    private val context: Context
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var onFollowClickListener: OnFollowClickListener? = null
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_followers, parent, false)
        return FollowersViewHolder(v, context)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewHolder = holder as FollowersViewHolder
        val item = followItems[position]
        viewHolder.setFollower(item)
        viewHolder.btn_follow.setOnClickListener {
            if (onFollowClickListener != null) {
                onFollowClickListener!!.onItemClick(position)
            }
        }
    }

    override fun getItemCount(): Int {
        return followItems.size
    }

    fun setOnFollowClickListener(onFollowClickListener: OnFollowClickListener?) {
        this.onFollowClickListener = onFollowClickListener
    }

    interface OnFollowClickListener {
        fun onItemClick(position: Int)
    }
}