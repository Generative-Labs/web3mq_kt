package com.ty.web3mq.http.beans

class MessageStatus {
    var status: String? = null
    var timestamp: Long = 0

    companion object {
        const val STATUS_READ = "read"
        const val STATUS_DELIVERED = "delivered"
        const val STATUS_RECEIVED = "received"
    }
}