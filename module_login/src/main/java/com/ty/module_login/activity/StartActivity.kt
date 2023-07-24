package com.ty.module_login.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.ty.module_common.Web3MQUI
import com.ty.module_common.activity.BaseActivity
import com.ty.module_common.config.AppConfig
import com.ty.module_login.ModuleLogin
import com.ty.module_login.R
import com.ty.web3mq.Web3MQClient
import com.ty.web3mq.Web3MQUser
import com.ty.web3mq.interfaces.ConnectCallback

class StartActivity : BaseActivity(), ConnectCallback{
    private lateinit var tv_connect_wallet: TextView
    private lateinit var tv_check_out: TextView
    private lateinit var iv_logo: ImageView
    private lateinit var cl_reconnect: ConstraintLayout
    private lateinit var cl_loading: ConstraintLayout
    private lateinit var btn_reconnect: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent(R.layout.activity_start)
        initView()
        setListener()
        cl_loading.visibility = View.VISIBLE
        Web3MQClient.init(this@StartActivity, AppConfig.APIKey)
        Web3MQClient.startConnect(this)
    }

    private fun initView() {
        tv_connect_wallet = findViewById(R.id.tv_connect_wallet)
        tv_check_out = findViewById(R.id.tv_check_out)
        iv_logo = findViewById(R.id.iv_logo)
        cl_reconnect = findViewById(R.id.cl_reconnect)
        btn_reconnect = findViewById(R.id.btn_reconnect)
        cl_loading = findViewById(R.id.cl_loading)
    }

    private fun setListener() {
        tv_connect_wallet.setOnClickListener {
            val intent = Intent(this@StartActivity, ConnectWalletActivity::class.java)
            startActivity(intent)
        }
        btn_reconnect.setOnClickListener {
            Web3MQClient.startConnect(this)
//            sendConnectCommand()
            cl_reconnect.visibility = View.GONE
        }
    }

    override fun onSuccess() {
        runOnUiThread { cl_loading.visibility = View.GONE }
        if(Web3MQUser.hasLogged()){
            ModuleLogin.onLoginSuccessCallback?.onLoginSuccess();
            this@StartActivity.finish()
        }
    }

    override fun onFail(error: String) {
        Log.i("StartActivity","onFail startConnect error: $error")
        runOnUiThread {
            cl_loading.visibility = View.GONE
            cl_reconnect.visibility = View.VISIBLE
        }
    }

    override fun alreadyConnected() {
        runOnUiThread { cl_loading.visibility = View.GONE }
    }
}