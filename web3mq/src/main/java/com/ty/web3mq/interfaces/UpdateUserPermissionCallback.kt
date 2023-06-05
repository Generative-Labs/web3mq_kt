package com.ty.web3mq.interfaces

interface UpdateUserPermissionCallback {
    fun onSuccess()
    fun onFail(error: String)
}