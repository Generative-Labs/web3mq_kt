package com.ty.module_contact.adapter.viewHolder

import android.content.Context
import android.graphics.Color
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ty.module_common.config.Constants
import com.ty.module_contact.R
import com.ty.module_contact.bean.FollowItem

class FollowersViewHolder(itemView: View, private val context: Context) :
    RecyclerView.ViewHolder(itemView) {
    var tv_user_name: TextView
    var btn_follow: Button
    private val iv_avatar: ImageView

    init {
        tv_user_name = itemView.findViewById(R.id.tv_user_name)
        iv_avatar = itemView.findViewById(R.id.iv_avatar)
        btn_follow = itemView.findViewById(R.id.btn_follow)
    }

    fun setFollower(follower: FollowItem) {
        tv_user_name.setText(follower.userName)
        if (!TextUtils.isEmpty(follower.avatar_url)) {
            Glide.with(context).load(follower.avatar_url).into(iv_avatar)
        }
        if (follower.follow_status == Constants.FOLLOW_STATUS_FOLLOWER) {
            btn_follow.setBackgroundResource(com.ty.module_common.R.drawable.shape_bg_btn_follow)
            btn_follow.setTextColor(Color.parseColor("#FFFFFF"))
            btn_follow.text = "Follow"
        } else {
            btn_follow.setBackgroundResource(com.ty.module_common.R.drawable.shape_bg_btn_following)
            btn_follow.setTextColor(Color.parseColor("#18181B"))
            btn_follow.text = "Following"
        }
    }
}