package com.ty.web3mq.http.request

class PostMyProfileRequest : BaseRequest() {
    var userid: String? = null
    var web3mq_signature: String? = null
    var timestamp: Long = 0
    var avatar_url: String? = null
    var nickname: String? = null
}