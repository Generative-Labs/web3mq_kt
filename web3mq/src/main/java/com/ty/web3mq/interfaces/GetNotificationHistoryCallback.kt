package com.ty.web3mq.interfaces

import com.ty.web3mq.http.beans.NotificationsBean


interface GetNotificationHistoryCallback {
    fun onSuccess(notificationsBean: NotificationsBean)
    fun onFail(error: String)
}