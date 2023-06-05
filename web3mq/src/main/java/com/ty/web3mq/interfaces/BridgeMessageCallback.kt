package com.ty.web3mq.interfaces

interface BridgeMessageCallback {
    fun onBridgeMessage(comeFrom: String, publicKey: String, content: String?)
}