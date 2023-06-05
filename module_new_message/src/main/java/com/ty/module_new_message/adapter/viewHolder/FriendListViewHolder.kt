package com.ty.module_new_message.adapter.viewHolder

import android.content.Context
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ty.module_common.config.Constants
import com.ty.module_new_message.R
import com.ty.module_new_message.adapter.FriendListAdapter
import com.ty.web3mq.http.beans.FollowerBean

class FriendListViewHolder(itemView: View, private val context: Context) :
    RecyclerView.ViewHolder(itemView) {
    var tv_user_name: TextView
    var btn_action: Button
    private val iv_avatar: ImageView
    var checkbox: CheckBox
    private var style: String = FriendListAdapter.STYLE_DEFAULT

    init {
        tv_user_name = itemView.findViewById(R.id.tv_user_name)
        iv_avatar = itemView.findViewById(R.id.iv_avatar)
        btn_action = itemView.findViewById(R.id.btn_action)
        checkbox = itemView.findViewById(R.id.checkbox)
    }

    fun setStyle(style: String) {
        this.style = style
    }

    fun setFollower(follower: FollowerBean) {
        when (style) {
            FriendListAdapter.STYLE_DEFAULT -> {
                btn_action.visibility = View.GONE
                checkbox.visibility = View.GONE
            }
            FriendListAdapter.STYLE_SEARCH -> {
                btn_action.visibility = View.VISIBLE
                checkbox.visibility = View.GONE
            }
            FriendListAdapter.STYLE_CREATE_ROOM -> {
                btn_action.visibility = View.GONE
                checkbox.visibility = View.VISIBLE
            }
        }
        tv_user_name.setText(follower.userid)
        if (!TextUtils.isEmpty(follower.avatar_url)) {
            Glide.with(context).load(follower.avatar_url).into(iv_avatar)
        }
        when (follower.follow_status) {
            Constants.FOLLOW_STATUS_FOLLOWER -> btn_action.text = "Follow"
            Constants.FOLLOW_STATUS_EACH -> btn_action.visibility = View.GONE
            Constants.FOLLOW_STATUS_FOLLOWING -> btn_action.text = "Request"
        }
    }
}