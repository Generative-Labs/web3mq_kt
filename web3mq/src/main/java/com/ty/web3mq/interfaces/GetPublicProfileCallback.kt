package com.ty.web3mq.interfaces

import com.ty.web3mq.http.beans.ProfileBean


interface GetPublicProfileCallback {
    fun onSuccess(profileBean: ProfileBean)
    fun onFail(error: String)
}