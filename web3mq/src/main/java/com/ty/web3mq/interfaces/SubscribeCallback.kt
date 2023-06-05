package com.ty.web3mq.interfaces

interface SubscribeCallback {
    fun onSuccess()
    fun onFail(error: String?)
}