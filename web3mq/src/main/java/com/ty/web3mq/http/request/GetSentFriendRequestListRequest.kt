package com.ty.web3mq.http.request

class GetSentFriendRequestListRequest : BaseRequest() {
    var page = 0
    var size = 0
    var userid: String? = null
    var timestamp: Long = 0
    var web3mq_signature: String? = null
}