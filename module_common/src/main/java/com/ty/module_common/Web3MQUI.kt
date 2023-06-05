package com.ty.module_common

import android.content.Context
import android.util.Log
import com.ty.module_common.config.AppConfig
import com.ty.web3mq.Web3MQClient
import com.ty.web3mq.interfaces.ConnectCallback

object Web3MQUI {
    private val TAG = "Web3MQUI"
    private var callback: InitCallback? = null
    fun init(context: Context) {
        Web3MQClient.init(context, AppConfig.APIKey)
        Web3MQClient.startConnect(object : ConnectCallback {
            override fun onSuccess() {
                Log.i(TAG, "Web3MQClient Connect Success")
//                if (callback != null) {
//                    callback!!.onSuccess()
//                }
            }

            override fun onFail(error: String) {
//                if (callback != null) {
//                    callback!!.onFail()
//                }
                //                initialized = false;
                Log.i(TAG, "Web3MQClient Connect Fail error:$error")
            }

            override fun alreadyConnected() {
//                if (callback != null) {
//                    callback!!.onSuccess()
//                }
                //                initialized = true;
                Log.i(TAG, "Web3MQClient Already Connected")
            }
        })
    }

//    fun setInitCallback(callback: InitCallback?) {
//        this.callback = callback
//    }

    interface InitCallback {
        fun onSuccess()
        fun onFail()
    }
}