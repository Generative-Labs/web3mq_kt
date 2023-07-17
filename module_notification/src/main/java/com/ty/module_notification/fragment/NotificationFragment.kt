package com.ty.module_notification.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.ty.module_common.config.AppConfig
import com.ty.module_common.fragment.BaseFragment
import com.ty.module_common.view.Web3MQListView
import com.ty.module_notification.R
import com.ty.module_notification.adapter.NotificationAdapter
import com.ty.web3mq.Web3MQFollower
import com.ty.web3mq.Web3MQNotification
import com.ty.web3mq.Web3MQSign
import com.ty.web3mq.http.beans.NotificationBean
import com.ty.web3mq.http.beans.NotificationsBean
import com.ty.web3mq.interfaces.*
import com.ty.web3mq.utils.CryptoUtils
import com.ty.web3mq.utils.DefaultSPHelper
import com.ty.web3mq.websocket.bean.BridgeMessageMetadata
import com.ty.web3mq.websocket.bean.BridgeMessageProposer

class NotificationFragment : BaseFragment() {
    private var adapter: NotificationAdapter? = null
    private var notifications: ArrayList<NotificationBean> = ArrayList<NotificationBean>()
    private var list_notification: Web3MQListView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent(R.layout.fragment_notification)
    }

    override fun onBaseCreateView() {
        super.onBaseCreateView()
        requestData()
        initView()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            requestData()
        }
    }

     fun listenToNotificationMessageEvent() {
        Web3MQNotification.setOnNotificationMessageEvent(object : NotificationMessageCallback {
            override fun onNotificationMessage(response: ArrayList<NotificationBean>) {
                notifications.addAll(0, response)
                adapter!!.notifyDataSetChanged()
            }
        })
    }

    fun removeNotificationMessageEvent() {}
    private fun requestData() {
        Web3MQNotification.getNotificationHistory(1, 20, object : GetNotificationHistoryCallback{
                override fun onSuccess(notificationsBean: NotificationsBean) {
                    notifications.clear()
                    notifications = notificationsBean.result!!
                    if (notifications.size > 0) {
                        list_notification!!.hideEmptyView()
                        updateList()
                    } else {
                        list_notification!!.showEmptyView()
                    }
                    list_notification!!.isRefreshing = false
                }

                override fun onFail(error: String) {
                    Log.e(TAG, "onFail:$error")
                    list_notification!!.isRefreshing = false
                }
            })
    }

    private fun initView() {
        list_notification = rootView!!.findViewById(R.id.list_notification)
        list_notification!!.setEmptyMessage("No notification message")
        list_notification!!.setEmptyIcon(R.mipmap.ic_empty_notification)
        list_notification!!.setOnRefreshListener { requestData() }
    }

    private fun updateList() {
        adapter = NotificationAdapter(requireContext(), notifications)
        adapter!!.setOnFollowClickListener(object : NotificationAdapter.OnFollowClickListener {
            override fun onItemClick(position: Int) {
                val notificationBean = notifications[position]
                val action = NotificationBean.ACTION_FOLLOW
                toFollow(action, notificationBean.from!!)
            }
        })
        list_notification!!.setAdapter(adapter)
        adapter!!.checkFollowStatus()
    }

    private fun toFollow(action: String, target_user_id: String) {
        if (Web3MQSign.initialized()) {
            val deepLink: String = Web3MQSign.generateConnectDeepLink(null, AppConfig.WebSite, AppConfig.REDIRECT_HOME_PAGE)!!
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(deepLink))
            startActivity(intent)
        } else {
            Web3MQSign.init(AppConfig.DAppID, object : BridgeConnectCallback {
                override fun onConnectCallback() {
                    val deepLink: String = Web3MQSign.generateConnectDeepLink(
                        null,
                        AppConfig.WebSite,
                        AppConfig.REDIRECT_HOME_PAGE
                    )!!
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(deepLink))
                    startActivity(intent)
                }

                override fun onError(error: String) {
                    TODO("Not yet implemented")
                }
            })
        }
        Web3MQSign.setOnConnectResponseCallback(object : OnConnectResponseCallback {
            override fun onApprove(walletInfo: BridgeMessageMetadata, address: String) {
                toSign(action, walletInfo.walletType!!, address, target_user_id)
            }

            override fun onReject() {
                Toast.makeText(getActivity(), "connect reject", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun toSign(
        action: String,
        wallet_type: String,
        wallet_address: String,
        target_user_id: String
    ) {
        val deepLink: String = Web3MQSign.generateSignDeepLink()!!
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(deepLink))
        startActivity(intent)
        val proposer = BridgeMessageProposer()
        proposer.name = "Web3MQ_DAPP_DEMO"
        proposer.url = AppConfig.WebSite
        proposer.redirect = AppConfig.REDIRECT_HOME_PAGE
        val timestamp = System.currentTimeMillis()
        val userid: String = DefaultSPHelper.getUserID()!!
        val nonce: String = CryptoUtils.SHA3_ENCODE(userid + action + target_user_id + timestamp)
        val sign_raw: String =
            Web3MQFollower.getFollowSignContent(wallet_type, wallet_address, nonce)
        Web3MQSign.sendSignRequest(sign_raw, wallet_address, false, null)
        Web3MQSign.setOnSignResponseMessageCallback(object : OnSignResponseMessageCallback{
                override fun onApprove(signature: String) {
                    Web3MQFollower.follow(
                        target_user_id,
                        action,
                        signature,
                        sign_raw,
                        timestamp,
                        object : FollowCallback{
                            override fun onSuccess() {
                                Toast.makeText(getActivity(), "follow success", Toast.LENGTH_SHORT)
                                    .show()
                                adapter!!.checkFollowStatus()
                            }

                            override fun onFail(error: String) {
                                Toast.makeText(
                                    getActivity(),
                                    "follow fail: $error",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        })
                }

                override fun onReject() {
                    Toast.makeText(getActivity(), "sign reject", Toast.LENGTH_SHORT).show()
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    companion object {
        fun getInstance() = NotificationFragment()
        private const val TAG = "NotificationsFragment"
    }
}