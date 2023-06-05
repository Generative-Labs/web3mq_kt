package com.ty.web3mq.http.request

class GetMessageHistoryRequest : BaseRequest() {
    var userid: String? = null
    var topic: String? = null
    var page = 0
    var size = 0
    var timestamp: Long = 0
    var web3mq_signature: String? = null
}