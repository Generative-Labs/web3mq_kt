package com.ty.web3mq.http.request

class ResetPwdRequest : BaseRequest() {
    var userid: String? = null
    var did_type: String? = null
    var did_value: String? = null
    var timestamp: Long = 0
    var did_signature: String? = null
    var signature_content: String? = null
    var pubkey_type: String? = null
    var pubkey_value: String? = null
    var testnet_access_key: String? = null
}