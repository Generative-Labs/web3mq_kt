package com.ty.module_login.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
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
import com.ty.web3mq.Web3MQClient
import com.ty.web3mq.Web3MQSign
import com.ty.web3mq.Web3MQUser
import com.ty.web3mq.interfaces.ConnectCallback
import com.ty.web3mq.interfaces.LoginCallback
import com.ty.web3mq.interfaces.OnSignResponseMessageCallback
import com.ty.web3mq.interfaces.SignupCallback
import com.ty.web3mq.utils.CryptoUtils
import com.ty.web3mq.utils.Ed25519

class RegisterActivity : BaseActivity() {
    private var cl_pwd_error: ConstraintLayout? = null
    private var tv_wallet_address: TextView? = null
    private var btn_create_new_user: Button? = null
    private var view_create_pwd: InputPwdView? = null
    private var view_confirm_pwd: InputPwdView? = null
    private var iv_back: ImageView? = null
    private var keyGenerateSignSuccess = false
    private var mainPrivateKeyHex: String? = null
    private var user_id: String? = null
    private var pubkey_value: String? = null
    private var registerSignContent: String? = null
    private var signInTimeStamp: Long = 0

    var wallet_name: String? = null

    var wallet_type: String? = null

    var wallet_address: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent(R.layout.activity_register)
        wallet_name = intent.getStringExtra(Constants.ROUTER_KEY_LOGIN_WALLET_NAME)
        wallet_type = intent.getStringExtra(Constants.ROUTER_KEY_LOGIN_WALLET_TYPE)
        wallet_address = intent.getStringExtra(Constants.ROUTER_KEY_LOGIN_WALLET_ADDRESS)
        initView()
        setListener()
    }

    private fun initView() {
        view_create_pwd = findViewById(R.id.view_create_pwd)
        view_confirm_pwd = findViewById(R.id.view_confirm_pwd)
        cl_pwd_error = findViewById(R.id.cl_pwd_error)
        tv_wallet_address = findViewById(R.id.tv_wallet_address)
        btn_create_new_user = findViewById(R.id.btn_create_new_user)
        iv_back = findViewById(R.id.iv_back)
        tv_wallet_address!!.text = wallet_address
    }

    private fun setListener() {
        val watcher: InputPwdView.EmptyWatcher = object : InputPwdView.EmptyWatcher {
            override fun onEmptyChange(empty: Boolean) {
                if (empty) {
                    btn_create_new_user!!.isEnabled = false
                } else if (view_create_pwd!!.pwd != view_confirm_pwd!!.pwd) {
                    btn_create_new_user!!.isEnabled = false
                } else {
                    btn_create_new_user!!.isEnabled = true
                }
            }
        }
        view_create_pwd!!.setEmptyWatcher(watcher)
        view_confirm_pwd!!.setEmptyWatcher(watcher)
        btn_create_new_user!!.setOnClickListener { signKeyGenerate() }
        iv_back!!.setOnClickListener { finish() }
        Web3MQSign.setOnSignResponseMessageCallback(object : OnSignResponseMessageCallback{
                override fun onApprove(signature: String) {
                    if (!keyGenerateSignSuccess) {
                        //keyGenerate sign
                        mainPrivateKeyHex = CryptoUtils.SHA256_ENCODE(signature)
                        user_id = Web3MQUser.generateUserID(wallet_type!!, wallet_address!!)
                        Log.i(TAG, "user_id:$user_id")
                        val pubkey_type = "ed25519"
                        pubkey_value = Ed25519.generatePublicKey(mainPrivateKeyHex!!)
                        Log.i(
                            TAG,
                            "pubkey_value:" + pubkey_value + "pubkey_value len:" + pubkey_value!!.length
                        )
                        signInTimeStamp = System.currentTimeMillis()
                        Log.i(
                            TAG,
                            "sha3_224 before:$user_id$pubkey_type$pubkey_value$wallet_type$wallet_address$signInTimeStamp"
                        )
                        val nonce_content: String =
                            CryptoUtils.SHA3_ENCODE(user_id + pubkey_type + pubkey_value + wallet_type + wallet_address + signInTimeStamp)
                        Log.i(TAG, "sha3_224 nonce_content:$nonce_content")
                        registerSignContent = Web3MQUser.getRegisterSignContent(
                            wallet_name!!,
                            wallet_address!!,
                            YOUR_DOMAIN_URL,
                            nonce_content
                        )
                        keyGenerateSignSuccess = true
                        sendSign(registerSignContent)
                    } else {
                        //register sign
                        if (mainPrivateKeyHex == null || user_id == null || pubkey_value == null || registerSignContent == null || signInTimeStamp == 0L) {
                            Log.e(TAG, "keyGenerateSign error")
                            return
                        }
                        Web3MQUser.signUp(
                            user_id!!,
                            wallet_type!!,
                            wallet_address!!,
                            mainPrivateKeyHex!!,
                            registerSignContent!!,
                            signature,
                            signInTimeStamp,
                            object : SignupCallback {
                                override fun onSuccess() {
                                    //user sign up success
                                    loginRequest(
                                        user_id!!,
                                        wallet_type,
                                        wallet_address,
                                        mainPrivateKeyHex!!,
                                        pubkey_value!!
                                    )
                                }

                                override fun onFail(error: String) {
                                    hideLoading()
                                    Log.i(TAG, "signUp fail: $error")
                                }
                            })
                    }
                }

                override fun onReject() {
                    hideLoading()
                    Toast.makeText(this@RegisterActivity, "sign reject", Toast.LENGTH_SHORT).show()
                }
            })
    }

    /**
     *
     */
    private fun signKeyGenerate() {
        val password: String = view_create_pwd!!.pwd
        val magicString: String =
            Web3MQUser.generateMagicString(wallet_type!!, wallet_address!!, password)
        Log.i(TAG, "magicString:$magicString")
        val keyGenerateContent: String =
            Web3MQUser.getKeyGenerateSignContent(wallet_address!!, magicString)
        Log.i(TAG, "keyGenerateContent:$keyGenerateContent")
        sendSign(keyGenerateContent)
    }

    private fun sendSign(sign_raw: String?) {
        val intent =
            Intent(Intent.ACTION_VIEW, Uri.parse(Web3MQSign.generateSignDeepLink()))
        startActivity(intent)
        //        BridgeMessageProposer proposer = new BridgeMessageProposer();
//        proposer.name = "Web3MQ_DAPP_DEMO";
//        proposer.url = "www.web3mq_dapp.com";
//        proposer.redirect = REDIRECT_URL;
        Web3MQSign.sendSignRequest(sign_raw!!, wallet_address!!, false, null)
    }

    private fun loginRequest(
        user_id: String,
        wallet_type: String?,
        wallet_address: String?,
        main_prv_key: String,
        main_pubkey: String
    ) {
        Web3MQUser.login(
            user_id,
            wallet_type!!,
            wallet_address!!,
            main_prv_key,
            main_pubkey,
            object : LoginCallback{
                override fun onSuccess() {
                    Log.i(TAG, "login success")
                    connect()
                }

                override fun onFail(error: String) {
                    Log.i(TAG, "login error $error")
                }
            })
    }

    private fun connect() {
        Web3MQClient.startConnect(object : ConnectCallback{
            override fun onSuccess() {
                // connect success
//                sendConnectCommand();
                ModuleLogin.onLoginSuccessCallback?.onLoginSuccess()
            }

            override fun onFail(error: String) {
                hideLoading()
                Toast.makeText(this@RegisterActivity, "connect fail", Toast.LENGTH_SHORT).show()
            }

            override fun alreadyConnected() {
                ModuleLogin.onLoginSuccessCallback?.onLoginSuccess()
            }
        })
    }
    companion object {
        private const val TAG = "LoginFragment"
        private const val YOUR_DOMAIN_URL = "https://www.web3mq.com"
        private const val REDIRECT_URL = "web3mq_dapp_register://"
    }
}