package com.ty.web3mq.http.request

class GetUserInfoRequest : BaseRequest() {
    var did_type: String? = null
    var did_value: String? = null
    var timestamp: Long = 0
}