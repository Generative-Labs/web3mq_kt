package com.ty.walletdemo

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.ty.module_sign.fragment.WalletSignFragment
import com.ty.module_sign.interfaces.OnConnectCallback
import com.ty.module_sign.interfaces.OnSignCallback
import com.ty.module_sign.interfaces.WalletInitCallback
import com.ty.web3mq.Web3MQClient
import com.ty.web3mq.Web3MQSign
import com.ty.web3mq.WebsocketConfig
import com.ty.web3mq.interfaces.OnSignRequestMessageCallback
import com.ty.web3mq.utils.ConvertUtil
import com.ty.web3mq.utils.CryptoUtils
import com.ty.web3mq.utils.DefaultSPHelper
import com.ty.web3mq.utils.RandomUtils
import com.ty.web3mq.websocket.bean.BridgeMessageMetadata
import com.ty.web3mq.websocket.bean.ConnectRequest
import com.ty.web3mq.websocket.bean.sign.Participant
import io.github.g00fy2.quickie.QRResult
import io.github.g00fy2.quickie.ScanQRCode

class MainActivity : AppCompatActivity() {
    private var walletSignFragment: WalletSignFragment? = null
    private val api_key = "rkkJARiziBQCscgg"
    private val dAppID = "web3MQ_test_wallet:wallet"
    private val ETH_ADDRESS = "0x0dBED6A18d4026B6B637AA9f89DE4a6aCc861D84"
    private val ETH_PRV_KEY = "620db3575a3cd921e6c5dd91a9fce59573349f2f5dd81f84ae25e82c2275f4c2"
    private lateinit var iv_scan: ImageView

    //    private String handling_connect_uri = null;
    private var btn_toggle: ToggleButton? = null
    private var fromRemote = false
    private var handle_url: String? = null
    val scanQrCodeLauncher = registerForActivityResult(ScanQRCode()) { result ->
        // handle QRResult
        when(result){
            is QRResult.QRSuccess -> {
                fromRemote = true
                handleConnectUri(Uri.parse(result.content.rawValue))
            }
            else -> {}
        }

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 1)

        Web3MQClient.init(this, api_key)
        setContentView(R.layout.activity_main)
        btn_toggle = findViewById(R.id.btn_toggle)
        walletSignFragment = WalletSignFragment()
        initView()
        setListener()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.fl_content, walletSignFragment!!).commitAllowingStateLoss()
        DefaultSPHelper.clear()
        btn_toggle!!.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                Web3MQClient.switchUri(WebsocketConfig.WS_URL_TEST_NET)
            } else {
                Web3MQClient.switchUri(WebsocketConfig.WS_URL_DEV)
            }
        })

    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        Log.i(TAG, "onNewIntent")
    }

    override fun onResume() {
        super.onResume()
        Log.i(TAG, "onResume")
        val uri = intent.data
        if (uri != null) {
            fromRemote = false
            if(handle_url == null || handle_url!=uri.toString()) {
                handleConnectUri(uri)
                handle_url = uri.toString()
            }
        }
    }

    private fun initSignFragment(request: ConnectRequest) {
        walletSignFragment!!.showLoadingDialog()
        walletSignFragment!!.init(
            dAppID,
            request.topic!!,
            request.publicKey!!,
            object : WalletInitCallback{
                override fun initSuccess() {
                    val metaData = BridgeMessageMetadata()
                    metaData.name = "Web3MQ Wallet"
                    metaData.description = "ETH wallet"
                    metaData.walletType = "eth"
                    var icon: String? = null
                    if (request.icons != null && request.icons!!.size > 0) {
                        icon = request.icons!![0]
                    }
                    walletSignFragment!!.hideLoadingDialog()
                    walletSignFragment!!.showConnectBottomDialog(
                        this@MainActivity,
                        request.id!!,
                        request.url,
                        icon,
                        metaData,
                        ETH_ADDRESS
                    )
                }

                override fun onFail(error: String) {
                    walletSignFragment!!.hideLoadingDialog()
                    Toast.makeText(this@MainActivity, error, Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun handleConnectUri(uri: Uri?) {
        Log.i(TAG,"handleConnectUri")
        val request: ConnectRequest = ConvertUtil.convertDeepLinkToConnectRequest(uri.toString())
        if (request.method != null && request.method.equals("provider_authorization")) {
            Web3MQSign.walletBuildAndSaveSession(request)
            walletSignFragment!!.setOnConnectCallback(object : OnConnectCallback {
                override fun connectApprove() {
                    if (!fromRemote) {
                        moveTaskToBack(true)
                    }
                }

                override fun connectReject() {
                    if (!fromRemote) {
                        moveTaskToBack(true)
                    }
                    Log.i(TAG, "connectReject redirect:" + request.redirect)
                }
            })
            Web3MQSign.setOnSignRequestMessageCallback(object : OnSignRequestMessageCallback {
                    override fun onSignRequestMessage(
                        id: String,
                        participant: Participant?,
                        address: String,
                        sign_raw: String
                    ) {
                        Log.i(TAG, "sign_raw: $sign_raw")
                        if (TEST_MODEL) {
                            Handler().postDelayed({
                                if (RandomUtils.randomBoolean()) {
                                    val signature: String =
                                        CryptoUtils.signMessage(ETH_PRV_KEY, sign_raw)
                                    Web3MQSign.sendSignResponse(id, true, signature, false, null)
                                    if (!TextUtils.isEmpty(request.redirect)) {
                                        val intent =
                                            Intent(Intent.ACTION_VIEW, Uri.parse(request.redirect))
                                        startActivity(intent)
                                    }
                                } else {
                                    Web3MQSign.sendSignResponse(id, false, null, false, null)
                                    if (!TextUtils.isEmpty(request.redirect)) {
                                        val intent =
                                            Intent(Intent.ACTION_VIEW, Uri.parse(request.redirect))
                                        startActivity(intent)
                                    }
                                }
                            }, 1000)
                        } else {
                            walletSignFragment!!.showSignBottomDialog(
                                id,
                                participant!!,
                                address,
                                sign_raw
                            )
                        }
                    }
                })
            walletSignFragment!!.setOnSignCallback(object : OnSignCallback {
                override fun sign(sign_raw: String): String {
                    return CryptoUtils.signMessage(ETH_PRV_KEY, sign_raw)
                }

                override fun signApprove(redirect: String?) {
                    if (!fromRemote) {
                        moveTaskToBack(true)
                    }
                }

                override fun signReject(redirect: String?) {
                    if (!fromRemote) {
                        moveTaskToBack(true)
                    }
                }
            })
            initSignFragment(request)
        }
    }

    private fun initView() {
        iv_scan = findViewById(R.id.iv_scan)
    }

    private fun setListener() {
        iv_scan.setOnClickListener {
            //scan
            scanQrCodeLauncher.launch(null)
        }
    }

    fun hideSignFragment() {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.hide(walletSignFragment!!).commitAllowingStateLoss()
    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        //接收扫码结果
//        if (resultCode == Activity.RESULT_OK && requestCode == ScanCodeConfig.QUESTCODE && data != null) {
//            val extras = data.extras
//            if (extras != null) {
//                val code = extras.getString(ScanCodeConfig.CODE_KEY)
//                Log.i(TAG, "code:$code")
//                fromRemote = true
//                handleConnectUri(Uri.parse(code))
//            }
//        }
//    }

    companion object {
        private const val TAG = "MainActivity"
        private const val TEST_MODEL = false
    }
}