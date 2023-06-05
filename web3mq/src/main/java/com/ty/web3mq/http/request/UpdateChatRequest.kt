package com.ty.web3mq.http.request

import com.ty.web3mq.http.request.BaseRequest

class UpdateChatRequest : BaseRequest() {
    var userid: String? = null
    var web3mq_signature: String? = null
    var timestamp: Long = 0
    var chatid: String? = null
    var chat_type: String? = null
}