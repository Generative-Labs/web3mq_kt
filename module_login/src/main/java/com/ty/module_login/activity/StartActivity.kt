package com.ty.module_login.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.ty.module_common.Web3MQUI
import com.ty.module_common.activity.BaseActivity
import com.ty.module_common.config.AppConfig
import com.ty.module_login.ModuleLogin
import com.ty.module_login.R
import com.ty.web3mq.Web3MQClient
import com.ty.web3mq.Web3MQUser
import com.ty.web3mq.interfaces.ConnectCallback

class StartActivity : BaseActivity() {
    private var tv_connect_wallet: TextView? = null
    private var tv_check_out: TextView? = null
    private var iv_logo: ImageView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent(R.layout.activity_start)
        Web3MQClient.init(this@StartActivity, AppConfig.APIKey)
        Web3MQClient.startConnect(object : ConnectCallback {
            override fun onSuccess() {
                if(Web3MQUser.hasLogged()){
                    ModuleLogin.onLoginSuccessCallback?.onLoginSuccess();
                    this@StartActivity.finish()
                }else {
                    initView()
                    setListener()
                }
            }

            override fun onFail(error: String) {
                Toast.makeText(this@StartActivity, "startConnect error: $error", Toast.LENGTH_SHORT).show()
            }

            override fun alreadyConnected() {

            }
        })


    }

    private fun initView() {
        tv_connect_wallet = findViewById(R.id.tv_connect_wallet)
        tv_check_out = findViewById(R.id.tv_check_out)
        iv_logo = findViewById(R.id.iv_logo)
    }

    private fun setListener() {
        tv_connect_wallet!!.setOnClickListener {
            val intent = Intent(this@StartActivity, ConnectWalletActivity::class.java)
            startActivity(intent)
        }
    }
}