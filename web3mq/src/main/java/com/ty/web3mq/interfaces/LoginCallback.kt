package com.ty.web3mq.interfaces

interface LoginCallback {
    fun onSuccess()
    fun onFail(error: String)
}