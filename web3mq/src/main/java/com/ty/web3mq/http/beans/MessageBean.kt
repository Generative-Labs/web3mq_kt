package com.ty.web3mq.http.beans

class MessageBean {
    var cipher_suite: String? = null
    var from: String? = null
    var topic: String? = null
    var from_sign: String? = null
    var messageid: String? = null
    var payload_type: String? = null
    var timestamp: Long = 0
    var status: MessageStatus? = null
    var payload: String? = null
    var version = 0
}