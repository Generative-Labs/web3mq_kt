package com.ty.web3mq.websocket.bean

class BridgeMessage {
    var publicKey: String? = null
    var content: String? = null

    companion object {
        const val ACTION_CONNECT_REQUEST = "connectRequest"
        const val ACTION_CONNECT_RESPONSE = "connectResponse"
        const val ACTION_SIGN_REQUEST = "signRequest"
        const val ACTION_SIGN_RESPONSE = "signResponse"
    }
}