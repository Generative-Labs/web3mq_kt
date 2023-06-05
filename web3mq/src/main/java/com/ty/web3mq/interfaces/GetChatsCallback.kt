package com.ty.web3mq.interfaces

import com.ty.web3mq.http.beans.ChatsBean


interface GetChatsCallback {
    fun onSuccess(chatsBean: ChatsBean)
    fun onFail(error: String)
}