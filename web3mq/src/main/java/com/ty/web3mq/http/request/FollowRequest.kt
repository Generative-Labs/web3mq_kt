package com.ty.web3mq.http.request

class FollowRequest : BaseRequest() {
    var userid: String? = null
    var target_userid: String? = null
    var action: String? = null
    var timestamp: Long = 0
    var did_type: String? = null
    var did_signature: String? = null
    var did_pubkey: String? = null
    var sign_content: String? = null
}