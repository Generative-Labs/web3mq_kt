package com.ty.web3mq.websocket.bean

class ConnectRequest {
    var topic: String? = null
    var id: String? = null
    var jsonrpc: String? = null
    var method: String? = null
    var publicKey: String? = null
    var name: String? = null
    var description: String? = null
    var url: String? = null
    var icons: ArrayList<String>? = null
    var redirect: String? = null
    var expiry: String? = null
}