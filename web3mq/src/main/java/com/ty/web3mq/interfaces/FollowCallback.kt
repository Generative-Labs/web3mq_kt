package com.ty.web3mq.interfaces

interface FollowCallback {
    fun onSuccess()
    fun onFail(error: String)
}