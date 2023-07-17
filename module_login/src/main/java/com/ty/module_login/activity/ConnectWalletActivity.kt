package com.ty.module_login.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.ty.module_common.activity.BaseActivity
import com.ty.module_common.config.AppConfig
import com.ty.module_common.config.Constants
import com.ty.module_login.R
import com.ty.web3mq.Web3MQSign
import com.ty.web3mq.Web3MQUser
import com.ty.web3mq.http.beans.UserInfo
import com.ty.web3mq.interfaces.BridgeConnectCallback
import com.ty.web3mq.interfaces.GetUserinfoCallback
import com.ty.web3mq.interfaces.OnConnectResponseCallback
import com.ty.web3mq.websocket.bean.BridgeMessageMetadata
import java.util.*

class ConnectWalletActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent(R.layout.activity_connect_wallet)
        connectWallet()
    }

    private fun connectWallet() {
        Web3MQSign.init(AppConfig.DAppID, object : BridgeConnectCallback {
            override fun onConnectCallback() {
                val deepLink: String? = Web3MQSign.generateConnectDeepLink(
                    null,
                    AppConfig.WebSite,
                    AppConfig.REDIRECT_WALLET_CONNECT
                )
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(deepLink))
                startActivity(intent)
            }

            override fun onError(error: String) {
                Toast.makeText(this@ConnectWalletActivity, error, Toast.LENGTH_SHORT).show()
                this@ConnectWalletActivity.finish()
            }
        })
        Web3MQSign.setOnConnectResponseCallback(object : OnConnectResponseCallback{
            override fun onApprove(walletInfo: BridgeMessageMetadata, address: String) {
                val walletName: String = walletInfo.name!!
                val walletType: String = walletInfo.walletType!!
                Web3MQUser.getUserInfo(walletType, address, object : GetUserinfoCallback {
                        override fun onSuccess(userInfo: UserInfo) {
                            // get user info success
                            Log.i(TAG, "getUserInfo onSuccess")
                            val intent = Intent(this@ConnectWalletActivity, LoginActivity::class.java)
                            intent.putExtra(Constants.ROUTER_KEY_LOGIN_USER_ID, userInfo.userid)
                            intent.putExtra(Constants.ROUTER_KEY_LOGIN_WALLET_TYPE,userInfo.wallet_type)
                            intent.putExtra(Constants.ROUTER_KEY_LOGIN_WALLET_ADDRESS, userInfo.wallet_address!!.toLowerCase())
                            startActivity(intent)
                        }

                        override fun onUserNotRegister() {
                            // user not register
                            Log.i(TAG, "getUserInfo onUserNotRegister")
                            val intent = Intent(this@ConnectWalletActivity, RegisterActivity::class.java)
                            intent.putExtra(Constants.ROUTER_KEY_LOGIN_WALLET_NAME, walletName)
                            intent.putExtra(Constants.ROUTER_KEY_LOGIN_WALLET_TYPE,walletType)
                            intent.putExtra(Constants.ROUTER_KEY_LOGIN_WALLET_ADDRESS, address.lowercase(Locale.getDefault()))
                            startActivity(intent)
                        }

                        override fun onFail(error: String) {
                            Log.i(TAG, "getUserInfo onFail")
                            Toast.makeText(
                                this@ConnectWalletActivity,
                                "getUserInfo error:$error",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
            }

            override fun onReject() {
                Toast.makeText(this@ConnectWalletActivity, "connect rejected", Toast.LENGTH_SHORT)
                    .show()
                finish()
            }
        })
    }

    companion object {
        private const val TAG = "ConnectWalletActivity"
    }
}