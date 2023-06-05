package com.ty.web3mq.interfaces

import com.ty.web3mq.http.response.CommonResponse

interface GetMyFollowersCallback {
    fun onSuccess(str: String?)
    fun onFail(error: String?)
}