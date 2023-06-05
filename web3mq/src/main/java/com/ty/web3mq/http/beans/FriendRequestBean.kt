package com.ty.web3mq.http.beans

class FriendRequestBean {
    var userid: String? = null
    var status = 0

    companion object {
        const val STATUS_NOT_AGREED = 0
        const val STATUS_AGREED = 1
    }
}