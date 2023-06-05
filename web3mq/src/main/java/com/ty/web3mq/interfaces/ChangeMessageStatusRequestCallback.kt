package com.ty.web3mq.interfaces

interface ChangeMessageStatusRequestCallback {
    fun onSuccess()
    fun onFail(error: String?)
}