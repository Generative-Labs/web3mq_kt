package com.ty.web3mq.interfaces

interface SignupCallback {
    fun onSuccess()
    fun onFail(error: String)
}