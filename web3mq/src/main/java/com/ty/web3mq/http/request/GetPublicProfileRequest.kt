package com.ty.web3mq.http.request

class GetPublicProfileRequest : BaseRequest() {
    var my_userid: String? = null
    var did_type: String? = null
    var did_value: String? = null
    var timestamp: Long = 0
}