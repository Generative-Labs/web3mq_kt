package com.ty.web3mq.interfaces

import com.ty.web3mq.http.beans.UserInfo


interface GetUserinfoCallback {
    fun onSuccess(userInfo: UserInfo)
    fun onUserNotRegister()
    fun onFail(error: String)
}