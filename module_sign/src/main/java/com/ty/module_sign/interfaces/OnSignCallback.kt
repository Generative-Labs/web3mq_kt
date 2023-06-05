package com.ty.module_sign.interfaces

interface OnSignCallback {
    fun sign(sign_raw: String): String?
    fun signApprove(redirect: String?)
    fun signReject(redirect: String?)
}