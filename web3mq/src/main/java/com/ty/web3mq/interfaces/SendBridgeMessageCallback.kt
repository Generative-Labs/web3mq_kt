package com.ty.web3mq.interfaces

interface SendBridgeMessageCallback {
    fun onReceived()
    fun onFail()
    fun onTimeout()
}