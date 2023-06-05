package com.ty.web3mq.interfaces

interface ConnectCallback {
    fun onSuccess()
    fun onFail(error: String)
    fun alreadyConnected()
}