package com.ty.module_sign.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.ty.module_common.fragment.BaseFragment
import com.ty.module_sign.R
import com.ty.module_sign.interfaces.OnConnectCallback
import com.ty.module_sign.interfaces.OnSignCallback
import com.ty.module_sign.interfaces.WalletInitCallback
import com.ty.web3mq.Web3MQClient
import com.ty.web3mq.Web3MQSign
import com.ty.web3mq.interfaces.BridgeConnectCallback
import com.ty.web3mq.interfaces.ConnectCallback
import com.ty.web3mq.interfaces.SendBridgeMessageCallback
import com.ty.web3mq.utils.AppUtils
import com.ty.web3mq.websocket.bean.BridgeMessageMetadata
import com.ty.web3mq.websocket.bean.sign.Participant

class WalletSignFragment : BaseFragment(), ConnectCallback{

    private var bottomSheetDialog: BottomSheetDialog? = null
    private var connectCallback: OnConnectCallback? = null
    private var onSignCallback: OnSignCallback? = null
    private var dAppID: String? = null
    private var topicId: String? = null
    private var pubKey: String? = null
    private var callback: WalletInitCallback? = null
    private lateinit var cl_reconnect: ConstraintLayout
    private lateinit var btn_reconnect: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent(R.layout.fragment_wallet_sign)
    }

    override fun onBaseCreateView() {
        super.onBaseCreateView()
        cl_reconnect = rootView.findViewById(R.id.cl_reconnect)
        btn_reconnect = rootView.findViewById(R.id.btn_reconnect)
        btn_reconnect.setOnClickListener {
            Web3MQClient.startConnect(this)
            cl_reconnect.visibility = View.GONE
        }
    }

    fun init(dAppID: String, topicId: String, pubKey: String, callback: WalletInitCallback) {
        this.dAppID = dAppID
        this.topicId = topicId
        this.pubKey = pubKey
        this.callback = callback
        Web3MQClient.startConnect(this)
    }

    fun setOnSignCallback(onSignCallback: OnSignCallback?) {
        this.onSignCallback = onSignCallback
    }

    fun setOnConnectCallback(onConnectCallback: OnConnectCallback?) {
        connectCallback = onConnectCallback
    }

    fun showConnectBottomDialog(
        context: Context,
        id: String,
        website: String?,
        iconUrl: String?,
        walletInfo: BridgeMessageMetadata,
        address: String
    ) {
        if (bottomSheetDialog != null && bottomSheetDialog!!.isShowing) {
            bottomSheetDialog!!.dismiss()
        }
        bottomSheetDialog = BottomSheetDialog(context)
        val view =
            View.inflate(getContext(), R.layout.bottom_dialog_connect, null)
        val btn_connect = view.findViewById<Button>(R.id.btn_connect)
        val btn_cancel = view.findViewById<Button>(R.id.btn_cancel)
        val tv_website_url = view.findViewById<TextView>(R.id.tv_website_url)
        val iv_website_icon = view.findViewById<ImageView>(R.id.iv_website_icon)
        val tv_address = view.findViewById<TextView>(R.id.tv_address)
        if (website != null) {
            tv_website_url.text = website
        }
        if (iconUrl != null) {
            Glide.with(context).load(iconUrl).into(iv_website_icon)
        }
        tv_address.text = address
        btn_connect.setOnClickListener {
            Web3MQSign.sendConnectResponse(id, address, true, walletInfo, false, null)
            bottomSheetDialog!!.dismiss()
            connectCallback?.connectApprove()
        }
        btn_cancel.setOnClickListener {
            Web3MQSign.sendConnectResponse(id, address, false, null, false, null)
            bottomSheetDialog!!.dismiss()
            connectCallback?.connectReject()
        }
        bottomSheetDialog!!.setContentView(view)
        bottomSheetDialog!!.show()
    }

    fun showSignBottomDialog(
        id: String,
        participant: Participant,
        address: String,
        sign_content: String
    ) {
        if (bottomSheetDialog != null && bottomSheetDialog!!.isShowing) {
            bottomSheetDialog!!.dismiss()
        }
        bottomSheetDialog = BottomSheetDialog(requireActivity())
        val view = View.inflate(activity, R.layout.bottom_dialog_sign, null)
        val btn_sign = view.findViewById<Button>(R.id.btn_sign)
        val btn_cancel = view.findViewById<Button>(R.id.btn_cancel)
        val tv_website_url = view.findViewById<TextView>(R.id.tv_website_url)
        val iv_website_icon = view.findViewById<ImageView>(R.id.iv_website_icon)
        val tv_address = view.findViewById<TextView>(R.id.tv_address)
        val tv_sign_content = view.findViewById<TextView>(R.id.tv_sign_content)
        if (participant.website != null) {
            tv_website_url.setText(participant.website)
        }
        if (participant.iconUrl != null) {
            Glide.with(activity).load(participant.iconUrl).into(iv_website_icon)
        }
        tv_address.text = address
        tv_sign_content.text = sign_content
        btn_sign.setOnClickListener {
            if (onSignCallback != null) {
                val signature: String = onSignCallback!!.sign(sign_content)!!
                Web3MQSign.sendSignResponse(
                    id,
                    true,
                    signature,
                    false,
                    object : SendBridgeMessageCallback {
                        override fun onReceived() {
                            Toast.makeText(getActivity(), "onReceived", Toast.LENGTH_SHORT).show()
                        }

                        override fun onFail() {
                            Toast.makeText(getActivity(), "onFail", Toast.LENGTH_SHORT).show()
                        }

                        override fun onTimeout() {
                            Toast.makeText(getActivity(), "onTimeout", Toast.LENGTH_SHORT).show()
                        }
                    })
                bottomSheetDialog!!.dismiss()
                onSignCallback!!.signApprove(participant.redirect)
            }
        }
        btn_cancel.setOnClickListener {
            if (onSignCallback != null) {
                Web3MQSign.sendSignResponse(id, false, null, false, null)
                bottomSheetDialog!!.dismiss()
                onSignCallback!!.signReject(participant.redirect)
            }
        }
        bottomSheetDialog!!.setContentView(view)
        bottomSheetDialog!!.show()
    }

    override fun onSuccess() {
        Web3MQSign.init(dAppID!!, object : BridgeConnectCallback {
            override fun onConnectCallback() {
                callback!!.initSuccess()
            }

            override fun onError(error: String) {
                callback!!.onFail(error)
            }
        })
        Web3MQSign.setTargetTopicID(topicId!!)
        Web3MQSign.setTargetPubKey(pubKey!!)
    }

    override fun onFail(error: String) {
        requireActivity().runOnUiThread {
            cl_reconnect.visibility = View.VISIBLE
            bottomSheetDialog?.dismiss()
            callback?.onFail("connect websocket error:$error")
        }
    }

    override fun alreadyConnected() {
        Web3MQSign.init(dAppID!!, object : BridgeConnectCallback {
            override fun onConnectCallback() {
                callback?.initSuccess()
            }

            override fun onError(error: String) {
                callback?.onFail(error)
            }
        })
        Web3MQSign.setTargetTopicID(topicId!!)
        Web3MQSign.setTargetPubKey(pubKey!!)
    }
}