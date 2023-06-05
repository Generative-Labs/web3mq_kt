package com.ty.web3mq.http.request

import com.ty.web3mq.http.request.BaseRequest

class UserRegisterRequest : BaseRequest() {
    var userid: String? = null
    var did_type: String? = null
    var did_value: String? = null
    var timestamp: Long = 0
    var did_signature: String? = null
    var signature_content: String? = null
    var pubkey_type: String? = null
    var pubkey_value: String? = null
    var nickname: String? = null
    var avatar_url: String? = null
    var avatar_base64: String? = null
    var testnet_access_key: String? = null
}