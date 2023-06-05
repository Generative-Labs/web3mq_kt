package com.ty.web3mq.http.beans

class NotificationBean {
    var cipher_suite: String? = null
    var from: String? = null
    var topic: String? = null
    var from_sign: String? = null
    var messageid: String? = null
    var payload_type: String? = null
    var timestamp: Long = 0
    var payload: NotificationPayload? = null
    var version = 0

    companion object {
        const val TYPE_FRIEND_REQUEST = "system.friend_request"
        const val TYPE_AGREE_FRIEND_REQUEST = "system.agree_friend_request"
        const val TYPE_GROUP_INVITATION = "system.group_invitation"
        const val TYPE_SUBSCRIPTION = "subscription"
        const val ACTION_FOLLOW = "follow"
        const val ACTION_CANCEL = "cancel"
    }
}