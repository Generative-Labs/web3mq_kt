package com.ty.web3mq.websocket

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.gson.Gson
import com.google.protobuf.InvalidProtocolBufferException
import com.ty.web3mq.WebsocketConfig
import com.ty.web3mq.http.beans.NotificationBean
import com.ty.web3mq.http.beans.NotificationPayload
import com.ty.web3mq.interfaces.*
import com.ty.web3mq.websocket.bean.BridgeMessage
import com.ty.web3mq.websocket.bean.MessageBean
import web3mq.Bridge
import web3mq.Heartbeat
import web3mq.Message
import java.nio.ByteBuffer
import java.util.ArrayList
import java.util.HashMap

object MessageManager {
    private val TAG = "MessageManager"
    private var notificationMessageCallback: NotificationMessageCallback? = null
    private var connectCallback: ConnectCallback? = null
    private var bridgeConnectCallback: BridgeConnectCallback? = null
    private var bridgeMessageCallback: BridgeMessageCallback? = null
    private var chatsMessageCallback: ChatsMessageCallback? = null
    private val DMMessageCallbackHashMap: HashMap<String, MessageCallback> =
        HashMap<String, MessageCallback>()
    private val groupMessageCallbackHashMap: HashMap<String, MessageCallback> =
        HashMap<String, MessageCallback>()
    private val sendBridgeMessageCallbackHashMap: HashMap<String, SendBridgeMessageCallback> =
        HashMap<String, SendBridgeMessageCallback>()
    private val handler = Handler(Looper.getMainLooper())
    private val gson: Gson = Gson()
    private var connectCommandCallback: OnConnectCommandCallback? = null
    private val MESSAGE_TYPE_BRIDGE = "Web3MQ/bridge"
    fun onMessage(bytes: ByteBuffer) {
        val length = bytes.array().size
        Log.i(TAG, "WebSocketClient onMessage bytes length$length")
        val categoryType = bytes.array()[0].toInt()
        val pbType = bytes.array()[1]
        Log.i(TAG, "categoryType: $categoryType")
        Log.i(TAG, "pbType: $pbType")
        if (pbType == WebsocketConfig.PbTypePongCommand) {
            Log.i(TAG, "pong response")
        }
        val data = ByteArray(length - 2)
        System.arraycopy(bytes.array(), 2, data, 0, length - 2)
        when (pbType) {
            WebsocketConfig.PbTypeConnectRespCommand -> {
                Log.i(TAG, "ConnectResp")
                if (connectCommandCallback != null) {
                    try {
                        val command: Heartbeat.ConnectCommand = Heartbeat.ConnectCommand.parseFrom(data)
                        Log.i(TAG, "NodeId: " + command.getNodeId())
                        Log.i(TAG, "UserId: " + command.getUserId())
                        Log.i(TAG, "Timestamp: " + command.getTimestamp())
                        Log.i(TAG, "MsgSign: " + command.getMsgSign())
                    } catch (e: InvalidProtocolBufferException) {
                        e.printStackTrace()
                    }
                    handler.post { connectCommandCallback!!.onConnectCommandResponse() }
                }
            }
            WebsocketConfig.PbTypeNotificationListResp -> {
                Log.i(TAG, "NotificationListResp")
                if (notificationMessageCallback != null) {
                    try {
                        val response: Message.Web3MQMessageListResponse =
                            Message.Web3MQMessageListResponse.parseFrom(data)
                        val messageList: List<Message.MessageItem> = response.getDataList()
                        val notificationList: ArrayList<NotificationBean> =
                            ArrayList<NotificationBean>()
                        for (message in messageList) {
                            val notification = NotificationBean()
                            notification.from = message.getComeFrom()
                            notification.messageid = message.getMessageId()
                            notification.payload = gson.fromJson(
                                message.getPayload().toStringUtf8(),
                                NotificationPayload::class.java
                            )
                            notification.timestamp = message.getTimestamp()
                            notification.cipher_suite = message.getCipherSuite()
                            notification.from_sign = message.getFromSign()
                            notification.topic = message.getContentTopic()
                            notification.payload_type = message.getPayloadType()
                            notificationList.add(notification)
                        }
                        //TODO

                        handler.post {
                            notificationMessageCallback!!.onNotificationMessage(
                                notificationList
                            )
                        }
                    } catch (e: InvalidProtocolBufferException) {
                        e.printStackTrace()
                        Log.e(TAG, "NotificationListResp parse error")
                    }
                }
            }
            WebsocketConfig.PbTypeMessage -> {
                Log.i(TAG, "Message callback")
                try {
                    val message: Message.Web3MQMessage = Message.Web3MQMessage.parseFrom(data)
                    Log.i(TAG, "MessageType:" + message.getMessageType())
                    Log.i(TAG, "MessageId:" + message.getMessageId())
                    Log.i(TAG, "MessageType:" + message.getMessageType())
                    Log.i(TAG, "ComeFrom:" + message.getComeFrom())
                    Log.i(TAG, "Payload:" + message.getPayload().toStringUtf8())
                    Log.i(TAG, "PayloadType:" + message.getPayloadType())
                    Log.i(TAG, "ContentTopic:" + message.getContentTopic())
                    if (MESSAGE_TYPE_BRIDGE == message.getMessageType()) {
                        if (bridgeMessageCallback != null) {
                            val bridgeMessage: BridgeMessage = gson.fromJson(
                                message.getPayload().toStringUtf8(),
                                BridgeMessage::class.java
                            )
                            handler.post {
                                bridgeMessageCallback!!.onBridgeMessage(
                                    message.getComeFrom(),
                                    bridgeMessage.publicKey!!,
                                    bridgeMessage.content
                                )
                            }
                        }
                    } else {
                        if (chatsMessageCallback != null) {
                            handler.post { chatsMessageCallback!!.onMessage(message) }
                        }
                    }
                    for (come_from in DMMessageCallbackHashMap.keys) {
                        if (come_from == message.getComeFrom()) {
                            val messageBean = MessageBean()
                            messageBean.from = message.getComeFrom()
                            messageBean.payload = message.getPayload().toStringUtf8()
                            messageBean.timestamp = message.getTimestamp()
                            val callback: MessageCallback? = DMMessageCallbackHashMap[come_from]
                            if(callback!=null){
                                handler.post { callback.onMessage(messageBean) }
                            }

                        }
                    }
                    for (group_id in groupMessageCallbackHashMap.keys) {
                        if (group_id == message.getContentTopic()) {
                            val messageBean = MessageBean()
                            messageBean.from = message.getComeFrom()
                            messageBean.payload = message.getPayload().toStringUtf8()
                            messageBean.timestamp = message.getTimestamp()
                            val callback: MessageCallback? = groupMessageCallbackHashMap[group_id]
                            if(callback!=null) {
                                handler.post { callback.onMessage(messageBean) }
                            }
                        }
                    }
                } catch (e: InvalidProtocolBufferException) {
                    e.printStackTrace()
                }
            }
            WebsocketConfig.PbTypeMessageStatusResp -> {
                Log.i(TAG, "MessageStatusResp")
                try {
                    val response: Message.Web3MQMessageStatusResp =
                        Message.Web3MQMessageStatusResp.parseFrom(data)
                    Log.i(TAG, "ComeFrom: " + response.getComeFrom())
                    Log.i(TAG, "MessageId: " + response.getMessageId())
                    Log.i(TAG, "Timestamp: " + response.getTimestamp())
                    Log.i(TAG, "ContentTopic: " + response.getContentTopic())
                    Log.i(TAG, "messageStatus: " + response.getMessageStatus())
                    val callback: SendBridgeMessageCallback? =
                        sendBridgeMessageCallbackHashMap[response.getMessageId()]
                    if (callback != null) {
                        if (response.getMessageStatus() == "received") {
                            callback.onReceived()
                        } else {
                            callback.onFail()
                        }
                        sendBridgeMessageCallbackHashMap.remove(response.getMessageId())
                    }
                } catch (e: InvalidProtocolBufferException) {
                    e.printStackTrace()
                    Log.e(TAG, "parse error")
                }
            }
            WebsocketConfig.PbTypeWeb3MQBridgeConnectResp -> {
                Log.i(TAG, "BridgeConnectResp")
                if (bridgeConnectCallback != null) {
                    try {
                        val connectCommand: Bridge.Web3MQBridgeConnectCommand =
                            Bridge.Web3MQBridgeConnectCommand.parseFrom(data)
                        Log.i(TAG, "Node ID:" + connectCommand.getNodeID())
                        Log.i(TAG, "DApp ID:" + connectCommand.getDAppID())
                        Log.i(TAG, "Topic ID:" + connectCommand.getTopicID())
                        handler.post { bridgeConnectCallback!!.onConnectCallback() }
                    } catch (e: InvalidProtocolBufferException) {
                        e.printStackTrace()
                        Log.e(TAG, "parse error")
                    }
                }
            }
        }
    }

    fun setOnConnectCommandCallback(connectCommandCallback: OnConnectCommandCallback?) {
        this.connectCommandCallback = connectCommandCallback
    }

    fun setBridgeMessageCallback(callback: BridgeMessageCallback?) {
        bridgeMessageCallback = callback
    }

    fun removeBridgeMessageCallback() {
        bridgeMessageCallback = null
    }

    fun setBridgeConnectCallback(callback: BridgeConnectCallback?) {
        bridgeConnectCallback = callback
    }

    fun removeBridgeConnectCallback() {
        bridgeConnectCallback = null
    }

    fun setOnNotificationMessageEvent(notificationMessageCallback: NotificationMessageCallback?) {
        this.notificationMessageCallback = notificationMessageCallback
    }

    fun removeNotificationMessageEvent() {
        notificationMessageCallback = null
    }

    fun setConnectCallback(connectCallback: ConnectCallback?) {
        this.connectCallback = connectCallback
    }

    fun addDMMessageCallback(from: String, callback: MessageCallback) {
        DMMessageCallbackHashMap[from] = callback
    }

    fun removeDMMessageCallback(from: String) {
        DMMessageCallbackHashMap.remove(from)
    }

    fun addGroupMessageCallback(group_id: String, callback: MessageCallback) {
        groupMessageCallbackHashMap[group_id] = callback
    }

    fun removeGroupMessageCallback(group_id: String) {
        groupMessageCallbackHashMap.remove(group_id)
    }

    fun addSendBridgeMessageCallback(messageID: String, callback: SendBridgeMessageCallback) {
        sendBridgeMessageCallbackHashMap[messageID] = callback
    }

    fun removeSendBridgeMessageCallback(messageID: String) {
        sendBridgeMessageCallbackHashMap.remove(messageID)
    }

    //    public void addTopicMessageCallback(String topic_id, MessageCallback callback){
    //        topicMessageCallbackHashMap.put(topic_id,callback);
    //    }
    //
    //    public void removeTopicMessageCallback(String Topic_id){
    //        topicMessageCallbackHashMap.remove(Topic_id);
    //    }
    fun setChatsMessageCallback(callback: ChatsMessageCallback?) {
        chatsMessageCallback = callback
    }

    fun removeChatsMessageCallback() {
        chatsMessageCallback = null
    }
}