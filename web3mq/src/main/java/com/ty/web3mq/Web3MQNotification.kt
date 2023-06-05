package com.ty.web3mq

import com.ty.web3mq.http.ApiConfig
import com.ty.web3mq.http.HttpManager
import com.ty.web3mq.http.request.GetNotificationHistoryRequest
import com.ty.web3mq.http.response.GetNotificationHistoryResponse
import com.ty.web3mq.interfaces.GetNotificationHistoryCallback
import com.ty.web3mq.interfaces.NotificationMessageCallback
import com.ty.web3mq.utils.DefaultSPHelper
import com.ty.web3mq.utils.Ed25519
import com.ty.web3mq.websocket.MessageManager
import java.lang.Exception
import java.net.URLEncoder

object Web3MQNotification {
    fun setOnNotificationMessageEvent(notificationMessageCallback: NotificationMessageCallback?) {
        MessageManager.setOnNotificationMessageEvent(notificationMessageCallback)
    }

    fun removeNotificationMessageEvent() {
        MessageManager.removeNotificationMessageEvent()
    }
    fun getNotificationHistory(page: Int, size: Int, callback: GetNotificationHistoryCallback) {
        try {
            val pub_key: String = DefaultSPHelper.getTempPublic()!!
            val prv_key_seed: String = DefaultSPHelper.getTempPrivate()!!
            val did_key: String = DefaultSPHelper.getDidKey()!!
            val request = GetNotificationHistoryRequest()
            request.userid = DefaultSPHelper.getUserID()
            //            request.notice_type = notice_type;
            request.page = page
            request.size = size
            request.timestamp = System.currentTimeMillis()
            request.web3mq_signature = URLEncoder.encode(
                Ed25519.ed25519Sign(
                    prv_key_seed,
                    (request.userid + request.timestamp).toByteArray()
                )
            )
            HttpManager.get(
                ApiConfig.GET_NOTIFICATION_HISTORY,
                request,
                pub_key,
                did_key,
                GetNotificationHistoryResponse::class.java,
                object : HttpManager.Callback<GetNotificationHistoryResponse> {
                    override fun onResponse(response: GetNotificationHistoryResponse) {
                        if (response.code == 0) {
                            callback.onSuccess(response.data!!)
                        } else {
                            callback.onFail(
                                "error code: " + response.code
                                    .toString() + " msg:" + response.msg
                            )
                        }
                    }

                    override fun onError(error: String) {
                        callback.onFail("error: $error")
                    }
                })
        } catch (e: Exception) {
            e.printStackTrace()
            callback.onFail("ed25519 sign error")
        }
    }
}