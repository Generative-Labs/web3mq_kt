package com.ty.web3mq.http.request

import com.ty.web3mq.http.request.BaseRequest

class AddFriendsRequest : BaseRequest() {
    var userid: String? = null
    var target_userid: String? = null
    var content: String? = null
    var timestamp: Long = 0
    var web3mq_signature: String? = null
}