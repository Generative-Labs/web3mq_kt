package com.ty.web3mq.http.request

class HandleFriendRequest : BaseRequest() {
    var userid: String? = null
    var target_userid: String? = null
    var timestamp: Long = 0
    var action: String? = null
    var web3mq_signature: String? = null
}