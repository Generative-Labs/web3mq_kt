package com.ty.web3mq.http.request

import com.ty.web3mq.http.request.BaseRequest

class UserLoginRequest : BaseRequest() {
    var userid: String? = null
    var did_type: String? = null
    var did_value: String? = null
    var timestamp: Long = 0
    var login_signature: String? = null
    var signature_content: String? = null
    var main_pubkey: String? = null
    var pubkey_type: String? = null
    var pubkey_value: String? = null
    var pubkey_expired_timestamp: Long = 0
}