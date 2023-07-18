package com.ty.module_profile.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.ty.module_common.activity.BaseActivity
import com.ty.module_common.config.AppConfig
import com.ty.module_common.config.Constants
import com.ty.module_profile.ModuleProfile
import com.ty.module_profile.R
import com.ty.module_profile.view.FollowNumberTextView
import com.ty.web3mq.Web3MQFollower
import com.ty.web3mq.Web3MQSign
import com.ty.web3mq.Web3MQUser
import com.ty.web3mq.http.beans.NotificationBean
import com.ty.web3mq.http.beans.ProfileBean
import com.ty.web3mq.interfaces.*
import com.ty.web3mq.utils.CryptoUtils
import com.ty.web3mq.utils.DefaultSPHelper
import com.ty.web3mq.websocket.bean.BridgeMessageMetadata
import com.ty.web3mq.websocket.bean.BridgeMessageProposer

class OtherProfileActivity : BaseActivity() {
    var userid: String? = null
    var iv_back: ImageView? = null
    var iv_avatar: ImageView? = null
    var tv_wallet_address: TextView? = null
    var tv_follow_number: FollowNumberTextView? = null
    var wallet_address: String? = null
    var nickname: String? = null
    var btn_talk: ImageButton? = null
    var tv_follow: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent(R.layout.activity_other_profile)
        userid = intent.getStringExtra(Constants.ROUTER_KEY_USER_ID)
        initView()
        setListener()
        requestData()
    }

    private fun initView() {
        iv_back = findViewById(R.id.iv_back)
        iv_avatar = findViewById(R.id.iv_avatar)
        tv_follow_number = findViewById(R.id.tv_follow_number)
        btn_talk = findViewById(R.id.btn_talk)
        tv_follow = findViewById(R.id.tv_follow)
    }

    private fun setListener() {
        iv_back!!.setOnClickListener { finish() }
        tv_follow!!.setOnClickListener { toFollow(NotificationBean.ACTION_FOLLOW, userid) }
        btn_talk!!.setOnClickListener {
            ModuleProfile.onChatEvent?.onChat(userid)
        }
    }

    private fun requestData() {
        Web3MQUser.getPublicProfile(userid!!, object : GetPublicProfileCallback {
            override fun onSuccess(profileBean: ProfileBean) {
                val avatar_url: String? = profileBean.avatar_url
                if (!TextUtils.isEmpty(avatar_url)) {
                    Glide.with(this@OtherProfileActivity).load(avatar_url).into(iv_avatar)
                }
                nickname = profileBean.nickname
                wallet_address = profileBean.wallet_address
                tv_wallet_address!!.text = wallet_address
                tv_follow_number!!.setNumbers(
                    profileBean.stats!!.total_followers,
                    profileBean.stats!!.total_following
                )
            }

            override fun onFail(error: String) {
                Toast.makeText(
                    this@OtherProfileActivity,
                    "get profile error:$error",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun toFollow(action: String, target_user_id: String?) {
        if (Web3MQSign.initialized()) {
            val deepLink: String = Web3MQSign.generateConnectDeepLink(null, AppConfig.WebSite, AppConfig.REDIRECT_HOME_PAGE)!!
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(deepLink))
            startActivity(intent)
        } else {
            Web3MQSign.init(AppConfig.DAppID, object : BridgeConnectCallback{
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
        Web3MQSign.setOnConnectResponseCallback(object : OnConnectResponseCallback{
            override fun onApprove(walletInfo: BridgeMessageMetadata, address: String) {
                toSign(action, walletInfo.walletType!!, address, target_user_id)
            }

            override fun onReject() {
                Toast.makeText(this@OtherProfileActivity, "connect reject", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    private fun toSign(
        action: String,
        wallet_type: String,
        wallet_address: String,
        target_user_id: String?
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
        val sign_raw: String = Web3MQFollower.getFollowSignContent(wallet_type, wallet_address, nonce)
        Web3MQSign.sendSignRequest(sign_raw, wallet_address, false, null)
        Web3MQSign.setOnSignResponseMessageCallback(object : OnSignResponseMessageCallback{
                override fun onApprove(signature: String) {
                    Web3MQFollower.follow(
                        target_user_id!!,
                        action,
                        signature,
                        sign_raw,
                        timestamp,
                        object : FollowCallback{
                            override fun onSuccess() {
                                Toast.makeText(
                                    this@OtherProfileActivity,
                                    "follow success",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            override fun onFail(error: String) {
                                Toast.makeText(
                                    this@OtherProfileActivity,
                                    "follow fail: $error",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        })
                }

                override fun onReject() {
                    Toast.makeText(this@OtherProfileActivity, "sign reject", Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }
}