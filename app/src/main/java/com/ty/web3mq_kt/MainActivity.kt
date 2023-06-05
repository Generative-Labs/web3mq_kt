package com.ty.web3mq_kt

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.ty.web3mq.Web3MQClient
import com.ty.web3mq.interfaces.ConnectCallback
import com.zou.web3mq_kt.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Web3MQClient.init(this, "rkkJARiziBQCscgg")
        Web3MQClient.startConnect(object : ConnectCallback {
            override fun onSuccess() {
                Log.i("MainActivity", "Web3MQClient Connect Success")
            }

            override fun onFail(error: String?) {
                Log.i("MainActivity", "Web3MQClient Connect onFail")
            }

            override fun alreadyConnected() {
                Log.i("MainActivity", "Web3MQClient Connect alreadyConnected")
            }
        })
    }
}