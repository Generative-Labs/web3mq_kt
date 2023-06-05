package com.ty.web3mq.http.request

class SearchUsersRequest : BaseRequest() {
    var userid: String? = null
    var web3mq_signature: String? = null
    var timestamp: Long = 0
    var keyword: String? = null
}