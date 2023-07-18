package com.zou.module_chat.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.ty.module_common.activity.BaseActivity
import com.ty.module_common.config.AppConfig
import com.ty.module_common.config.Constants
import com.ty.web3mq.Web3MQFollower
import com.ty.web3mq.Web3MQPermission
import com.ty.web3mq.Web3MQSign
import com.ty.web3mq.http.beans.NotificationBean
import com.ty.web3mq.http.beans.UserPermissionsBean
import com.ty.web3mq.interfaces.*
import com.ty.web3mq.utils.CryptoUtils
import com.ty.web3mq.utils.DefaultSPHelper
import com.ty.web3mq.websocket.bean.BridgeMessageMetadata
import com.ty.web3mq.websocket.bean.BridgeMessageProposer
import com.zou.module_chat.ModuleChat
import com.zou.module_chat.R

class UserPermissionActivity : BaseActivity() {
//    @Autowired
    // TODO get chat_id
    var chat_id: String? = null
//    @Autowired
    // TODO get chat type
    var chat_type: String? = null
    var chat_user_permission: String? = null
    var follow_status: String? = null
    var btn_action: Button? = null
    var btn_cancel: Button? = null
    var btn_follow: Button? = null
    var tv_warn: TextView? = null
    private var state: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (chat_type == Constants.CHAT_TYPE_GROUP) {
            val intent = Intent(this@UserPermissionActivity, MessageActivity::class.java)
            intent.putExtra(Constants.ROUTER_KEY_CHAT_TYPE, chat_type)
            intent.putExtra(Constants.ROUTER_KEY_CHAT_ID, chat_id)
            startActivity(intent)

//            ARouter.getInstance().build(RouterPath.CHAT_MESSAGE)
//                .withString(Constants.ROUTER_KEY_CHAT_TYPE, chat_type)
//                .withString(Constants.ROUTER_KEY_CHAT_ID, chat_id).navigation()
        } else if (chat_type == Constants.CHAT_TYPE_USER) {
            setContent(R.layout.activity_chat_user_permission)
            initView()
            checkPermission()
        }
    }

    private fun initView() {
        btn_action = findViewById(R.id.btn_action)
        btn_cancel = findViewById(R.id.btn_cancel)
        btn_follow = findViewById(R.id.btn_follow)
        tv_warn = findViewById(R.id.tv_warn)
    }

    private fun checkPermission() {
        Web3MQPermission.getUserPermission(chat_id!!, object : GetUserPermissionCallback{
                override fun onSuccess(userPermissionsBean: UserPermissionsBean) {
                    chat_user_permission = userPermissionsBean.chat_permission
                    follow_status = userPermissionsBean.follow_status
                    handlePermission()
                    handleState()
                }

                override fun onFail(error: String) {
                    Toast.makeText(
                        this@UserPermissionActivity,
                        "get permission error : $error",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            })
    }

    private fun handleState() {
        when (state) {
            STATE_NO_NEED -> {
                val intent = Intent(this@UserPermissionActivity, MessageActivity::class.java)
                intent.putExtra(Constants.ROUTER_KEY_CHAT_TYPE, chat_type)
                intent.putExtra(Constants.ROUTER_KEY_CHAT_ID, chat_id)
                startActivity(intent)
            }
//                ARouter.getInstance().build(RouterPath.CHAT_MESSAGE)
//                .withString(Constants.ROUTER_KEY_CHAT_TYPE, chat_type)
//                .withString(Constants.ROUTER_KEY_CHAT_ID, chat_id).navigation()
            STATE_NEED_FOLLOW -> {
                tv_warn!!.text =
                    "The other party has set the privacy permission you need to follow each other"
                btn_follow!!.visibility = View.GONE
                btn_action!!.text = "Follow"
                btn_action!!.setOnClickListener { //TODO follow
                    toFollow(NotificationBean.ACTION_FOLLOW, chat_id)
                }
                btn_cancel!!.setOnClickListener { finish() }
            }
            STATE_NEED_REQUEST -> {
                tv_warn!!.text =
                    "The other party set the privacy permission need to ask the other party to follow you"
                btn_follow!!.visibility = View.GONE
                btn_action!!.text = "Request"
                btn_action!!.setOnClickListener { //TODO request
                    ModuleChat.toNewMessageRequestListener?.toRequestFollow(chat_id)
                }
                btn_cancel!!.setOnClickListener { finish() }
            }
            STATE_NEED_FOLLOW_AND_REQUEST -> {
                tv_warn!!.text =
                    "The other party has set privacy rights, you need to follow and send a request message"
                btn_follow!!.visibility = View.VISIBLE
                btn_follow!!.text = "Follow"
                btn_follow!!.setOnClickListener { //TODO follow
                    toFollow(NotificationBean.ACTION_FOLLOW, chat_id)
                }
                btn_action!!.text = "Request"
                btn_action!!.setOnClickListener { //TODO request
                    ModuleChat.toNewMessageRequestListener?.toRequestFollow(chat_id)
                }
                btn_cancel!!.setOnClickListener { finish() }
            }
        }
    }

    private fun handlePermission() {
        when (chat_user_permission) {
            Constants.CHAT_USER_PERMISSION_PUBLIC -> state = STATE_NO_NEED
            Constants.CHAT_USER_PERMISSION_FOLLOWER -> state =
                if (follow_status == Constants.FOLLOW_STATUS_FOLLOWER || follow_status == Constants.FOLLOW_STATUS_EACH) {
                    STATE_NO_NEED
                } else {
                    STATE_NEED_FOLLOW
                }
            Constants.CHAT_USER_PERMISSION_FOLLOWING -> state =
                if (follow_status == Constants.FOLLOW_STATUS_FOLLOWING || follow_status == Constants.FOLLOW_STATUS_EACH) {
                    STATE_NO_NEED
                } else {
                    STATE_NEED_REQUEST
                }
            Constants.CHAT_USER_PERMISSION_FRIEND -> state =
                if (follow_status == Constants.FOLLOW_STATUS_EACH) {
                    STATE_NO_NEED
                } else if (follow_status == Constants.FOLLOW_STATUS_FOLLOWER) {
                    STATE_NEED_REQUEST
                } else if (follow_status == Constants.FOLLOW_STATUS_FOLLOWING) {
                    STATE_NEED_FOLLOW
                } else {
                    STATE_NEED_FOLLOW_AND_REQUEST
                }
        }
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
        Web3MQSign.setOnConnectResponseCallback(object : OnConnectResponseCallback {
            override fun onApprove(walletInfo: BridgeMessageMetadata, address: String) {
                toSign(action, walletInfo.walletType!!, address, target_user_id)
            }

            override fun onReject() {
                Toast.makeText(this@UserPermissionActivity, "connect reject", Toast.LENGTH_SHORT)
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
                                    this@UserPermissionActivity,
                                    "follow success",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            override fun onFail(error: String) {
                                Toast.makeText(
                                    this@UserPermissionActivity,
                                    "follow fail: $error",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        })
                }

            override fun onReject() {
                    Toast.makeText(this@UserPermissionActivity, "sign reject", Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }

    companion object {
        private const val STATE_NO_NEED = "no_need"
        private const val STATE_NEED_FOLLOW = "need_follow"
        private const val STATE_NEED_REQUEST = "need_request"
        private const val STATE_NEED_FOLLOW_AND_REQUEST = "need_follow_and_request"
    }
}