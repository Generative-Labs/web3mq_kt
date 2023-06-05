package com.ty.web3mq.interfaces

interface ResetPwdCallback {
    fun onSuccess()
    fun onFail(error: String?)
}