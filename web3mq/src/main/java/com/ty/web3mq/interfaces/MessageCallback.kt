package com.ty.web3mq.interfaces

import com.ty.web3mq.websocket.bean.MessageBean


interface MessageCallback {
    fun onMessage(message: MessageBean)
}