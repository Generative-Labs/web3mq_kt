package com.ty.web3mq.interfaces

import web3mq.Message.Web3MQMessage

interface ChatsMessageCallback {
    fun onMessage(message: Web3MQMessage)
}