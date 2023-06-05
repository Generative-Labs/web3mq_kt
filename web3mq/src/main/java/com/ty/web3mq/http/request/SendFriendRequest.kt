package com.ty.web3mq.http.request

class SendFriendRequest : BaseRequest() {
    var userid: String? = null
    var target_userid: String? = null
    var timestamp: Long = 0
    var web3mq_signature: String? = null
}