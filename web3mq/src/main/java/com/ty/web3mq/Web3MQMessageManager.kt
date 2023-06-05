package com.ty.web3mq

import android.util.Base64
import android.util.Log
import com.google.protobuf.ByteString
import com.ty.web3mq.http.ApiConfig
import com.ty.web3mq.http.HttpManager
import com.ty.web3mq.http.request.ChangeMessageStatusRequest
import com.ty.web3mq.http.request.GetMessageHistoryRequest
import com.ty.web3mq.http.response.CommonResponse
import com.ty.web3mq.http.response.GetMessageHistoryResponse
import com.ty.web3mq.interfaces.ChangeMessageStatusRequestCallback
import com.ty.web3mq.interfaces.ChatsMessageCallback
import com.ty.web3mq.interfaces.GetMessageHistoryCallback
import com.ty.web3mq.interfaces.MessageCallback
import com.ty.web3mq.utils.CommonUtils
import com.ty.web3mq.utils.DefaultSPHelper
import com.ty.web3mq.utils.Ed25519
import com.ty.web3mq.websocket.MessageManager
import org.bouncycastle.jcajce.provider.digest.SHA3
import web3mq.Message
import java.lang.Exception
import java.math.BigInteger
import java.net.URLEncoder
import java.security.MessageDigest

object Web3MQMessageManager{
    const val TAG = "Web3MQMessageManager"
    fun changeMessageStatusRequest(
        message_ids: Array<String>,
        topic: String,
        status: String,
        callback: ChangeMessageStatusRequestCallback
    ) {
        try {
            val pub_key: String = DefaultSPHelper.getTempPublic()!!
            val prv_key_seed: String = DefaultSPHelper.getTempPrivate()!!
            val did_key: String = DefaultSPHelper.getDidKey()!!
            val request = ChangeMessageStatusRequest()
            request.userid = DefaultSPHelper.getUserID()
            request.messages = message_ids
            request.topic = topic
            request.status = status
            request.timestamp = System.currentTimeMillis()
            request.web3mq_signature = Ed25519.ed25519Sign(
                prv_key_seed,
                (request.userid + request.status + request.timestamp).toByteArray()
            )
            HttpManager.post(
                ApiConfig.CHANGE_MESSAGE_STATUS,
                request,
                pub_key,
                did_key,
                CommonResponse::class.java,
                object : HttpManager.Callback<CommonResponse> {
                    override fun onResponse(response: CommonResponse) {
                        if (response.code == 0) {
                            callback.onSuccess()
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

    fun getMessageHistory(
        page: Int,
        size: Int,
        topic_id: String,
        callback: GetMessageHistoryCallback
    ) {
        try {
            val pub_key: String = DefaultSPHelper.getTempPublic()!!
            val prv_key_seed: String = DefaultSPHelper.getTempPrivate()!!
            val did_key: String = DefaultSPHelper.getDidKey()!!
            val request = GetMessageHistoryRequest()
            request.userid = DefaultSPHelper.getUserID()
            request.topic = topic_id
            request.page = page
            request.size = size
            request.timestamp = System.currentTimeMillis()
            request.web3mq_signature = URLEncoder.encode(
                Ed25519.ed25519Sign(
                    prv_key_seed,
                    (request.userid + request.topic + request.timestamp).toByteArray()
                )
            )
            HttpManager.get(
                ApiConfig.GET_MESSAGE_HISTORY,
                request,
                pub_key,
                did_key,
                GetMessageHistoryResponse::class.java,
                object : HttpManager.Callback<GetMessageHistoryResponse> {
                    override fun onResponse(response: GetMessageHistoryResponse) {
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

    fun sendMessage(msg: String, topic_id: String, needStore: Boolean) {
        if (Web3MQClient.getNodeId() == null || !Web3MQClient.getSocketClient()!!.isOpen
        ) {
            Log.e(TAG, "websocket not connect")
            return
        }
        Log.i(TAG, "-----sendMessage-----")
        try {
            val pub_key: String = DefaultSPHelper.getTempPublic()!!
            val prv_key_seed: String = DefaultSPHelper.getTempPrivate()!!
            val user_id: String = DefaultSPHelper.getUserID()!!
            val timestamp = System.currentTimeMillis()
            val node_id: String = Web3MQClient.getNodeId()!!
            val msg_id = GenerateMessageID(user_id, topic_id, timestamp, msg.toByteArray())
            val signContent = msg_id + user_id + topic_id + node_id + timestamp
            val sign: String = Ed25519.ed25519Sign(prv_key_seed, signContent.toByteArray())
            val builder: Message.Web3MQMessage.Builder = Message.Web3MQMessage.newBuilder()
            builder.setNodeId(node_id)
            Log.i(TAG, "node_id:$node_id")
            builder.setCipherSuite("NONE")
            builder.setPayloadType("text/plain; charset=utf-8")
            builder.setFromSign(sign)
            Log.i(TAG, "sign:$sign")
            builder.setTimestamp(timestamp)
            Log.i(TAG, "timestamp:$timestamp")
            builder.setMessageId(msg_id)
            Log.i(TAG, "msg_id:$msg_id")
            builder.setVersion(1)
            builder.setComeFrom(user_id)
            Log.i(TAG, "comfrom:$user_id")
            builder.setContentTopic(topic_id)
            Log.i(TAG, "topic_id:$topic_id")
            builder.setNeedStore(needStore)
            Log.i(TAG, "needStore:$needStore")
            builder.setPayload(ByteString.copyFrom(msg.toByteArray()))
            Log.i(TAG, "payload:$msg")
            builder.setValidatePubKey(
                Base64.encodeToString(
                    Ed25519.hexStringToBytes(pub_key),
                    Base64.NO_WRAP
                )
            )
            val sendMessageBytes: ByteArray = CommonUtils.appendPrefix(
                WebsocketConfig.category,
                WebsocketConfig.PbTypeMessage,
                builder.build().toByteArray()
            )
            Web3MQClient.getSocketClient()!!.send(sendMessageBytes)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun GenerateMessageID(
        user_id: String,
        topic: String,
        timestamp: Long,
        payload: ByteArray
    ): String {
        val md: MessageDigest = SHA3.Digest224()
        md.update(user_id.toByteArray())
        md.update(topic.toByteArray())
        md.update(("" + timestamp).toByteArray())
        md.update(payload)
        val messageDigest = md.digest()
        val no = BigInteger(1, messageDigest)
        return no.toString(16)
    }

    fun addDMCallback(from: String, callback: MessageCallback) {
        MessageManager.addDMMessageCallback(from, callback)
    }

    fun removeDMCallback(from: String) {
        MessageManager.removeDMMessageCallback(from)
    }

    fun addGroupMessageCallback(group_id: String, callback: MessageCallback) {
        MessageManager.addGroupMessageCallback(group_id, callback)
    }

    fun removeGroupMessageCallback(group_id: String) {
        MessageManager.removeGroupMessageCallback(group_id)
    }

    fun setChatsMessageCallback(callback: ChatsMessageCallback) {
        MessageManager.setChatsMessageCallback(callback)
    }

    fun removeChatsMessageCallback() {
        MessageManager.removeChatsMessageCallback()
    }
}