package com.ty.web3mq.interfaces

import com.ty.web3mq.http.beans.NotificationBean


interface NotificationMessageCallback {
    fun onNotificationMessage(response: ArrayList<NotificationBean>)
}