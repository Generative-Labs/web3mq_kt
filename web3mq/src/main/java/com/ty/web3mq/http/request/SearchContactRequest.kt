package com.ty.web3mq.http.request

class SearchContactRequest : BaseRequest() {
    var userid: String? = null
    var keyword: String? = null
    var timestamp: Long = 0
    var web3mq_signature: String? = null
}