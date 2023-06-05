package com.ty.web3mq.interfaces

interface SendFriendRequestCallback {
    fun onSuccess()
    fun onFail(error: String?)
}