package com.ty.web3mq.http.request

class GetNotificationHistoryRequest : BaseRequest() {
    var userid: String? = null
    var notice_type: String? = null
    var page = 0
    var size = 0
    var timestamp: Long = 0
    var web3mq_signature: String? = null
}