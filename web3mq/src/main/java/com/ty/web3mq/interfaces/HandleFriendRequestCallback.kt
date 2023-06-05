package com.ty.web3mq.interfaces

interface HandleFriendRequestCallback {
    fun onSuccess()
    fun onFail(error: String?)
}