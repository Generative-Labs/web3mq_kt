package com.ty.web3mq.http.request

class ChangeNotificationStatusRequest : BaseRequest() {
    var userid: String? = null
    var messages: Array<String>? = null
    var status: String? = null
    var timestamp: Long = 0
    var signature: String? = null
}