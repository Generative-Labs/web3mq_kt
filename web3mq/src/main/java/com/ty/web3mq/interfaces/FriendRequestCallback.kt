package com.ty.web3mq.interfaces

interface FriendRequestCallback {
    fun onSuccess()
    fun onFail(error: String?)
}