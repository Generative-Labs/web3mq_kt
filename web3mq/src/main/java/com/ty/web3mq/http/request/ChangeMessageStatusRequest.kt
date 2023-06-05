package com.ty.web3mq.http.request

class ChangeMessageStatusRequest : BaseRequest() {
    var userid: String? = null
    var messages: Array<String>? = null
    var topic: String? = null
    var status: String? = null
    var timestamp: Long = 0
    var web3mq_signature: String? = null
}