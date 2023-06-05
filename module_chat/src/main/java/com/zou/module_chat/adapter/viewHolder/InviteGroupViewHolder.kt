package com.zou.module_chat.adapter.viewHolder

import android.content.Context
import android.text.TextUtils
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ty.web3mq.http.beans.FollowerBean
import com.zou.module_chat.R

class InviteGroupViewHolder(itemView: View, private val context: Context) :
    RecyclerView.ViewHolder(itemView) {
    var tv_user_name: TextView
    private val iv_avatar: ImageView
    var checkbox: CheckBox

    init {
        tv_user_name = itemView.findViewById(R.id.tv_user_name)
        iv_avatar = itemView.findViewById(R.id.iv_avatar)
        checkbox = itemView.findViewById(R.id.checkbox)
    }

    fun setFollower(follower: FollowerBean) {
        tv_user_name.setText(follower.userid)
        if (!TextUtils.isEmpty(follower.avatar_url)) {
            Glide.with(context).load(follower.avatar_url).into(iv_avatar)
        }
    }
}