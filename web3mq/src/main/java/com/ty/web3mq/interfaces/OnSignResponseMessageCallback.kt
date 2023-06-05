package com.ty.web3mq.interfaces

interface OnSignResponseMessageCallback {
    fun onApprove(signature: String)
    fun onReject()
}