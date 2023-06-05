package com.ty.web3mq

import com.ty.web3mq.http.ApiConfig
import com.ty.web3mq.http.HttpManager
import com.ty.web3mq.http.request.ChatRequest
import com.ty.web3mq.http.request.UpdateChatRequest
import com.ty.web3mq.http.response.BaseResponse
import com.ty.web3mq.http.response.ChatResponse
import com.ty.web3mq.http.response.CommonResponse
import com.ty.web3mq.interfaces.GetChatsCallback
import com.ty.web3mq.interfaces.UpdateMyChatCallback
import com.ty.web3mq.utils.DefaultSPHelper
import com.ty.web3mq.utils.Ed25519
import java.lang.Exception
import java.net.URLEncoder

object Web3MQChats {
    fun getChats(page: Int, size: Int, callback: GetChatsCallback) {
        try {
            val pub_key: String = DefaultSPHelper.getTempPublic()!!
            val prv_key_seed: String = DefaultSPHelper.getTempPrivate()!!
            val did_key: String = DefaultSPHelper.getDidKey()!!
            val request = ChatRequest()
            request.timestamp = System.currentTimeMillis()
            request.userid = DefaultSPHelper.getUserID()
            request.page = page
            request.size = size
            request.web3mq_signature = URLEncoder.encode(
                Ed25519.ed25519Sign(
                    prv_key_seed,
                    (request.userid + request.timestamp).toByteArray()
                )
            )
            HttpManager.get(
                ApiConfig.GET_CHAT_LIST,
                request,
                pub_key,
                did_key,
                ChatResponse::class.java,
                object : HttpManager.Callback<ChatResponse> {
                    override fun onResponse(response: ChatResponse) {
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
                        callback.onFail("request error: $error")
                    }
                })
        } catch (e: Exception) {
            callback.onFail("ed25519 sign error")
            e.printStackTrace()
        }
    }

    fun updateMyChat(
        timestamp: Long,
        chatid: String,
        chat_type: String,
        callback: UpdateMyChatCallback?
    ) {
        try {
            val pub_key: String = DefaultSPHelper.getTempPublic()!!
            val prv_key_seed: String = DefaultSPHelper.getTempPrivate()!!
            val did_key: String = DefaultSPHelper.getDidKey()!!
            val request = UpdateChatRequest()
            request.userid = DefaultSPHelper.getUserID()
            request.timestamp = timestamp
            request.chatid = chatid
            request.chat_type = chat_type
            request.web3mq_signature =
                Ed25519.ed25519Sign(prv_key_seed, (request.userid + request.timestamp).toByteArray())
            HttpManager.post(
                ApiConfig.UPDATE_MY_CHAT,
                request,
                pub_key,
                did_key,
                object : HttpManager.Callback<String> {
                    override fun onResponse(response: String) {
                        callback?.onSuccess()
                    }

                    override fun onError(error: String) {
                        callback?.onFail(error)
                    }
                })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}