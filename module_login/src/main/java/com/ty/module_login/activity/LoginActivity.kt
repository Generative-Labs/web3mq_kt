package com.ty.module_login.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.ty.module_common.activity.BaseActivity
import com.ty.module_common.config.Constants
import com.ty.module_login.ModuleLogin
import com.ty.module_login.R
import com.ty.module_login.view.InputPwdView
import com.ty.web3mq.Web3MQSign
import com.ty.web3mq.Web3MQUser
import com.ty.web3mq.interfaces.LoginCallback
import com.ty.web3mq.interfaces.OnSignResponseMessageCallback
import com.ty.web3mq.utils.CryptoUtils
import com.ty.web3mq.utils.Ed25519

class LoginActivity : BaseActivity() {
    private var view_input_pwd: InputPwdView? = null
    private var cl_pwd_error: ConstraintLayout? = null
    private var tv_wallet_address: TextView? = null
    private var btn_login: Button? = null
    private var iv_back: ImageView? = null
    private val TAG = "LoginActivity"
    var user_id: String? = null
    var wallet_type: String? = null
    var wallet_address: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent(R.layout.activity_login)
        user_id = intent.getStringExtra(Constants.ROUTER_KEY_LOGIN_USER_ID)
        wallet_type = intent.getStringExtra(Constants.ROUTER_KEY_LOGIN_WALLET_TYPE)
        wallet_address = intent.getStringExtra(Constants.ROUTER_KEY_LOGIN_WALLET_ADDRESS)
        initView()
        setListener()
    }

    private fun initView() {
        cl_pwd_error = findViewById(R.id.cl_pwd_error)
        tv_wallet_address = findViewById(R.id.tv_wallet_address)
        view_input_pwd = findViewById(R.id.view_input_pwd)
        btn_login = findViewById(R.id.btn_login)
        iv_back = findViewById(R.id.iv_back)
        if (wallet_address != null) {
            tv_wallet_address!!.text = wallet_address
        }
    }

    private fun setListener() {
        view_input_pwd!!.setEmptyWatcher(object : InputPwdView.EmptyWatcher {
            override fun onEmptyChange(empty: Boolean) {
                btn_login!!.isEnabled = !empty
            }
        })
        btn_login!!.setOnClickListener {
            showLoading()
            sign()
        }
        iv_back!!.setOnClickListener { finish() }
    }

    private fun sign() {
        val password: String = view_input_pwd!!.pwd
        val magicString: String =
            Web3MQUser.generateMagicString(wallet_type!!, wallet_address!!, password)
        val signContent: String = Web3MQUser.getKeyGenerateSignContent(wallet_address!!, magicString)
        val intent =
            Intent(Intent.ACTION_VIEW, Uri.parse(Web3MQSign.generateSignDeepLink()))
        startActivity(intent)
        sendSign(signContent)
        Web3MQSign.setOnSignResponseMessageCallback(object : OnSignResponseMessageCallback {
                override fun onApprove(signature: String) {
                    Log.i(TAG, "signature:$signature")
                    val mainPrivateKeyHex: String = CryptoUtils.SHA256_ENCODE(signature)!!
                    val mainPublicKeyHex: String = Ed25519.generatePublicKey(mainPrivateKeyHex)
                    Log.i(TAG, "mainPrivateKeyHex:$mainPrivateKeyHex")
                    Log.i(TAG, "mainPublicKeyHex:$mainPublicKeyHex")
                    loginRequest(
                        user_id!!,
                        wallet_type!!,
                        wallet_address!!,
                        mainPrivateKeyHex,
                        mainPublicKeyHex
                    )
                }

                override fun onReject() {
                    hideLoading()
                    Toast.makeText(this@LoginActivity, "sign reject", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun sendSign(sign_raw: String) {
//        BridgeMessageProposer proposer = new BridgeMessageProposer();
//        proposer.name = "Web3MQ_DAPP_DEMO";
//        proposer.url = "www.web3mq_dapp.com";
//        proposer.redirect = "web3mq_dapp_login://";
        Web3MQSign.sendSignRequest(sign_raw, wallet_address!!, false, null)
    }

    //    public void setUserInfo(String userid,String wallet_type,String wallet_address){
    //        this.userID = userid;
    //        this.wallet_type = wallet_type;
    //        this.wallet_address = wallet_address;
    //        Log.i(TAG,"setUserInfo userid:"+userid+" wallet_type:"+wallet_type+" wallet_address:"+wallet_address);
    //    }
    private fun loginRequest(
        user_id: String,
        wallet_type: String,
        wallet_address: String,
        main_prv_key: String,
        main_pubkey: String
    ) {
        Web3MQUser.login(
            user_id,
            wallet_type,
            wallet_address,
            main_prv_key,
            main_pubkey,
            object : LoginCallback{
                override fun onSuccess() {
                    Log.i(TAG, "login success")
                    //                sendConnectCommand();
                    hideLoading()
                    ModuleLogin.onLoginSuccessCallback!!.onLoginSuccess()
                }

                override fun onFail(error: String) {
                    hideLoading()
                    Log.i(TAG, "login error $error")
                    cl_pwd_error!!.visibility = View.VISIBLE
                }
            })
    } //    private void sendConnectCommand(){

    //        Web3MQClient.getInstance().sendConnectCommand(new OnConnectCommandCallback() {
    //            @Override
    //            public void onConnectCommandResponse() {
    //                hideLoading();
    //
    //                //TODO
    //            }
    //        });
    //    }
}