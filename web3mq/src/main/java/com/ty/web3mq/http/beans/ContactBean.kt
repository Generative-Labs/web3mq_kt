package com.ty.web3mq.http.beans

class ContactBean {
    var userid: String? = null
    var nickname: String? = null
    var avatar_url: String? = null

    companion object {
        const val STATUS_NOT_AGREED = 0
        const val STATUS_AGREED = 1
    }
}