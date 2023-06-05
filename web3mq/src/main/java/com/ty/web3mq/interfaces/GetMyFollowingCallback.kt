package com.ty.web3mq.interfaces

interface GetMyFollowingCallback {
    fun onSuccess(response: String)
    fun onFail(error: String)
}