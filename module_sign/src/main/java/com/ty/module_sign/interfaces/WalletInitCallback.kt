package com.ty.module_sign.interfaces

interface WalletInitCallback {
    fun initSuccess()
    fun onFail(error: String)
}