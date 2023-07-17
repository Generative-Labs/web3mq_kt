package com.ty.web3mq.interfaces

interface BridgeConnectCallback {
    fun onConnectCallback()
    fun onError(error: String)
}