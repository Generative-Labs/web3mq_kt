package com.ty.module_notification.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ty.module_notification.R
import com.ty.module_notification.adapter.viewHolder.NotificationHistoryViewHolder
import com.ty.web3mq.Web3MQFollower
import com.ty.web3mq.http.beans.FollowerBean
import com.ty.web3mq.http.beans.FollowersBean
import com.ty.web3mq.http.beans.NotificationBean
import com.ty.web3mq.interfaces.GetMyFollowingCallback
import com.ty.web3mq.utils.ConvertUtil

class NotificationAdapter(
    private val context: Context,
    notificationBeans: ArrayList<NotificationBean>
) : RecyclerView.Adapter<RecyclerView.ViewHolder?>() {
    private val notificationBeans: ArrayList<NotificationBean>
    private var onItemClickListener: OnFollowClickListener? = null
    private var myFollowingList: java.util.ArrayList<FollowerBean>? = null
    init {
        this.notificationBeans = notificationBeans
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(
            R.layout.item_notification_history,
            parent,
            false
        )
        return NotificationHistoryViewHolder(v)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val notification: NotificationBean = notificationBeans[position]
        val viewHolder: NotificationHistoryViewHolder = holder as NotificationHistoryViewHolder
        viewHolder.setNotificationBean(notification, myFollowingList)
        viewHolder.btn_follow.setOnClickListener(View.OnClickListener {
            if (onItemClickListener != null) {
                onItemClickListener!!.onItemClick(position)
            }
        })
    }

    fun setOnFollowClickListener(onItemClickListener: OnFollowClickListener?) {
        this.onItemClickListener = onItemClickListener
    }

    interface OnFollowClickListener {
        fun onItemClick(position: Int)
    }

    override fun getItemCount(): Int {
        return notificationBeans.size
    }

    fun checkFollowStatus() {
        Web3MQFollower.getMyFollowing(1, 20, object : GetMyFollowingCallback{
            override fun onSuccess(response: String) {
                val followersBean: FollowersBean = ConvertUtil.convertJsonToFollowersBean(response)!!
                myFollowingList = followersBean.user_list
                notifyDataSetChanged()
            }

            override fun onFail(error: String) {}
        })
    }
}