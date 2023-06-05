package com.ty.web3mq.interfaces

import com.ty.web3mq.http.beans.GroupBean


interface CreateGroupCallback {
    fun onSuccess(groupBean: GroupBean)
    fun onFail(error: String)
}