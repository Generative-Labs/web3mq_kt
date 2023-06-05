package com.ty.web3mq.http.request

class GetMyCreateTopicListRequest : BaseRequest() {
    var userid: String? = null
    var page = 0
    var size = 0
    var timestamp: Long = 0
    var web3mq_signature: String? = null
}