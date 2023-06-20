package com.ty.module_contact.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.ty.module_common.config.AppConfig
import com.ty.module_common.fragment.BaseFragment
import com.ty.module_common.view.Web3MQListView
import com.ty.module_contact.R
import com.ty.module_contact.adapter.FollowersAdapter
import com.ty.module_contact.bean.FollowItem
import com.ty.web3mq.Web3MQFollower
import com.ty.web3mq.Web3MQSign
import com.ty.web3mq.http.beans.FollowerBean
import com.ty.web3mq.http.beans.FollowersBean
import com.ty.web3mq.interfaces.*
import com.ty.web3mq.utils.ConvertUtil
import com.ty.web3mq.utils.CryptoUtils
import com.ty.web3mq.utils.DefaultSPHelper
import com.ty.web3mq.websocket.bean.BridgeMessageMetadata
import com.ty.web3mq.websocket.bean.BridgeMessageProposer
import java.util.ArrayList

class FollowingFragment : BaseFragment() {
    private var list_followers: Web3MQListView? = null
    private val items: ArrayList<FollowItem> = ArrayList<FollowItem>()
    private var adapter: FollowersAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent(R.layout.fragment_following)
    }

    override fun onBaseCreateView() {
        super.onBaseCreateView()
        requestData()
        initView()
        setListener()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            requestData()
        }
    }

    private fun requestData() {
        Web3MQFollower.getMyFollowing(1, 20, object : GetMyFollowingCallback{
            override fun onSuccess(response: String) {
                list_followers!!.setRefreshing(false)
                val followersBean: FollowersBean = ConvertUtil.convertJsonToFollowersBean(response)!!
                val followerBeans: ArrayList<FollowerBean> = followersBean.user_list!!
                items.clear()
                if (followersBean.total_count > 0) {
                    list_followers!!.hideEmptyView()
                    for (followerBean in followerBeans) {
                        val followItem = FollowItem()
                        followItem.userName = followerBean.userid
                        followItem.follow_status = followerBean.follow_status
                        followItem.avatar_url = followerBean.avatar_url
                        followItem.userid = followerBean.userid
                        items.add(followItem)
                    }
                    updateListView()
                } else {
                    list_followers!!.showEmptyView()
                }
            }

            override fun onFail(error: String) {
                list_followers!!.setRefreshing(false)
                list_followers!!.showEmptyView()
            }
        })
    }

    private fun initView() {
        list_followers = rootView!!.findViewById(R.id.list_followers)
        list_followers!!.setEmptyIcon(R.mipmap.ic_empty_contact)
        list_followers!!.setEmptyMessage("Your contact list is empty")
    }

    private fun setListener() {
        list_followers!!.setOnRefreshListener { requestData() }
    }

    private fun updateListView() {
        if (adapter == null) {
            adapter = FollowersAdapter(items, requireContext())
            adapter!!.setOnFollowClickListener(object : FollowersAdapter.OnFollowClickListener {
                override fun onItemClick(position: Int) {
                    val item: FollowItem = items[position]
                    toFollow(Web3MQFollower.ACTION_CANCEL, item.userid!!)
                }
            })
            list_followers!!.setAdapter(adapter)
        } else {
            adapter!!.notifyDataSetChanged()
        }
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
            })
        }
        Web3MQSign.setOnConnectResponseCallback(object : OnConnectResponseCallback{
            override fun onApprove(walletInfo: BridgeMessageMetadata, address: String) {
                toSign(action, walletInfo.walletType!!, address, target_user_id)
            }

            override fun onReject() {
                Toast.makeText(activity, "connect reject", Toast.LENGTH_SHORT).show()
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
        Web3MQSign.setOnSignResponseMessageCallback(object : OnSignResponseMessageCallback {
                override fun onApprove(signature: String) {
                    Web3MQFollower.follow(
                        target_user_id,
                        action,
                        signature,
                        sign_raw,
                        timestamp,
                        object : FollowCallback {
                            override fun onSuccess() {
                                Toast.makeText(
                                    activity,
                                    "unFollow success",
                                    Toast.LENGTH_SHORT
                                ).show()
                                requestData()
                            }

                            override fun onFail(error: String) {
                                Toast.makeText(
                                    activity,
                                    "follow fail: $error",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        })
                }

                override fun onReject() {
                    Toast.makeText(activity, "sign reject", Toast.LENGTH_SHORT).show()
                }
            })
    }

    companion object {
        fun getInstance() = FollowingFragment()
    }
}