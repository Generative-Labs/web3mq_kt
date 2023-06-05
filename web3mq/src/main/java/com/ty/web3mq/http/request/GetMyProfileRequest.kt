package com.ty.web3mq.http.request

class GetMyProfileRequest : BaseRequest() {
    var userid: String? = null
    var web3mq_signature: String? = null
    var timestamp: Long = 0
}