package com.ty.web3mq.http.request

class CreateGroupRequest : BaseRequest() {
    var userid: String? = null
    var group_name: String? = null
    var timestamp: Long = 0
    var web3mq_signature: String? = null
}