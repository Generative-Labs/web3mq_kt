package com.ty.module_notification.adapter.viewHolder

import android.graphics.Color
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ty.module_notification.R
import com.ty.web3mq.http.beans.FollowerBean
import com.ty.web3mq.http.beans.NotificationBean
import com.ty.web3mq.utils.DateUtils

class NotificationHistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var tv_notification_title: TextView
    var tv_notification_content: TextView
    var tv_notification_time: TextView
    var btn_follow: Button

    init {
        tv_notification_title = itemView.findViewById(R.id.tv_notification_title)
        tv_notification_content = itemView.findViewById(R.id.tv_notification_content)
        tv_notification_time = itemView.findViewById(R.id.tv_notification_time)
        btn_follow = itemView.findViewById(R.id.btn_follow)
    }

    fun setNotificationBean(
        notificationBean: NotificationBean,
        myFollowingList: java.util.ArrayList<FollowerBean>?
    ) {
        tv_notification_title.text = notificationBean.payload!!.title
        tv_notification_content.text = notificationBean.payload!!.content
        tv_notification_time.text = DateUtils.getTimeStringNotification(notificationBean.timestamp)
        when (notificationBean.payload!!.type) {
            NotificationBean.TYPE_FRIEND_REQUEST -> {
                if (myFollowingList != null &&
                    inFollowingList(notificationBean.from!!,myFollowingList)) {
                    btn_follow.setBackgroundResource(com.ty.module_common.R.drawable.shape_bg_btn_following)
                    btn_follow.setTextColor(Color.parseColor("#18181B"))
                    btn_follow.text = "Following"
                    btn_follow.isEnabled = false
                } else {
                    btn_follow.setBackgroundResource(com.ty.module_common.R.drawable.shape_bg_btn_follow)
                    btn_follow.setTextColor(Color.parseColor("#FFFFFF"))
                    btn_follow.text = "Follow"
                    btn_follow.isEnabled = true
                }
                btn_follow.visibility = View.VISIBLE
            }
            NotificationBean.TYPE_AGREE_FRIEND_REQUEST -> {}
            else -> btn_follow.visibility = View.GONE
        }
    }

    private fun inFollowingList(
        user_id: String,
        myFollowingList: ArrayList<FollowerBean>
    ): Boolean {
        for (followerBean in myFollowingList) {
            if (user_id == followerBean.userid) {
                return true
            }
        }
        return false
    }
}