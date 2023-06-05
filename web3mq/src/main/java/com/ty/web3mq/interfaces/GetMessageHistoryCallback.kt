package com.ty.web3mq.interfaces

import com.ty.web3mq.http.beans.MessagesBean


interface GetMessageHistoryCallback {
    fun onSuccess(messagesBean: MessagesBean)
    fun onFail(error: String?)
}