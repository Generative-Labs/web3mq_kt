package com.ty.web3mq.http.request

import com.google.gson.JsonObject

class UpdatePermissionsRequest : BaseRequest() {
    var userid: String? = null
    var permissions: JsonObject? = null
    var timestamp: Long = 0
    var web3mq_user_signature: String? = null
}