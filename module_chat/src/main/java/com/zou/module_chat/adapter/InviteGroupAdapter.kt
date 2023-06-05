package com.zou.module_chat.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.recyclerview.widget.RecyclerView
import com.ty.web3mq.http.beans.FollowerBean
import com.zou.module_chat.R
import com.zou.module_chat.adapter.viewHolder.InviteGroupViewHolder

class InviteGroupAdapter(followItems: ArrayList<FollowerBean>, context: Context) :
    RecyclerView.Adapter<RecyclerView.ViewHolder?>() {
    private val followItems: ArrayList<FollowerBean>
    private val context: Context
    private val checkedFollower: ArrayList<FollowerBean> = ArrayList<FollowerBean>()

    init {
        this.followItems = followItems
        this.context = context
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_invite_group, parent, false)
        return InviteGroupViewHolder(v, context)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewHolder: InviteGroupViewHolder = holder as InviteGroupViewHolder
        val bean: FollowerBean = followItems[position]
        viewHolder.setFollower(bean)
        viewHolder.checkbox.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                checkedFollower.add(bean)
            } else {
                checkedFollower.remove(bean)
            }
        })
    }

    fun getCheckedFollower(): ArrayList<FollowerBean> {
        return checkedFollower
    }

    override fun getItemCount(): Int {
        return followItems.size
    }
}