package com.ty.web3mq.http.request

class InvitationGroupRequest : BaseRequest() {
    var userid: String? = null
    var groupid: String? = null
    var members: Array<String>? = null
    var timestamp: Long = 0
    var web3mq_signature: String? = null
}