package com.ty.web3mq

import android.content.Context
import android.util.Base64
import android.util.Log
import com.ty.web3mq.http.ApiConfig
import com.ty.web3mq.http.HttpManager
import com.ty.web3mq.http.response.PingResponse
import com.ty.web3mq.interfaces.ConnectCallback
import com.ty.web3mq.interfaces.OnConnectCommandCallback
import com.ty.web3mq.interfaces.OnWebsocketClosedCallback
import com.ty.web3mq.utils.CommonUtils
import com.ty.web3mq.utils.DefaultSPHelper
import com.ty.web3mq.utils.Ed25519
import com.ty.web3mq.websocket.MessageManager
import com.ty.web3mq.websocket.Web3MQSocketClient
import org.java_websocket.enums.ReadyState
import web3mq.Bridge.Web3MQBridgeConnectCommand
import web3mq.Heartbeat.ConnectCommand
import java.net.URI

object Web3MQClient{
    private val TAG = "Web3MQClient"
    private var socketClient: Web3MQSocketClient? = null
    private var api_Key: String? = null
    private var node_id: String? = null

    fun init(context: Context, api_key: String) {
        DefaultSPHelper.init(context)
        this.api_Key = api_key
//        HttpManager.getInstance().initialize(context)
        initWebSocket()
    }

    private fun initWebSocket() {
        val uri = URI.create(WebsocketConfig.WS_URL_DEV)
        Log.i(TAG, "ws_url:" + WebsocketConfig.WS_URL_DEV)
        socketClient = Web3MQSocketClient(uri)
    }

    fun switchUri(uri: String) {
        val new_uri = URI.create(uri)
        socketClient!!.switchUri(new_uri)
    }

    fun setOnWebsocketClosedCallback(callback: OnWebsocketClosedCallback?) {
        socketClient!!.setOnWebsocketClosedCallback(callback)
    }

    fun reconnect() {
        try {
            socketClient!!.reconnectBlocking()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    fun startConnect(connectCallback: ConnectCallback?) {
        node_id = DefaultSPHelper.getNodeID()
        if (node_id != null) {
            connectWebSocket(connectCallback, node_id)
            return
        }
        HttpManager.get(ApiConfig.PING, null, null, null,
            PingResponse::class.java, object : HttpManager.Callback<PingResponse>{
                override fun onResponse(response: PingResponse) {
                    node_id = response.data?.NodeID
                    DefaultSPHelper.saveNodeID(node_id)
                    Log.i("getNodeId", "NodeID:$node_id")
                    connectWebSocket(connectCallback, node_id)
                }

                override fun onError(error: String) {
                    connectCallback?.onFail("ping error")
                }
            })
    }

    fun sendConnectCommand(callback: OnConnectCommandCallback?) {
        if (socketClient!!.isClosed || socketClient!!.isClosing) {
            Log.e(TAG, "websocket closed")
            return
        }
        val userid: String = DefaultSPHelper.getUserID()?: return
        val prv_key_seed: String = DefaultSPHelper.getTempPrivate()?: return
        val pub_key: String = DefaultSPHelper.getTempPublic()?: return
        MessageManager.setOnConnectCommandCallback(callback)
        val builder = ConnectCommand.newBuilder()
        builder.nodeId = node_id
        builder.userId = userid
        val timestamp = System.currentTimeMillis()
        builder.timestamp = timestamp
        builder.validatePubKey = Base64.encodeToString(Ed25519.hexStringToBytes(pub_key), Base64.NO_WRAP)
        val sign_content = node_id + userid + timestamp
//        try {
            builder.msgSign = Ed25519.ed25519Sign(prv_key_seed, sign_content.toByteArray())
//        } catch (e: Exception) {
//            Log.e(TAG, "ed25519 Sign Error")
//            e.printStackTrace()
//        }
        val connectBytes: ByteArray = CommonUtils.appendPrefix(
            WebsocketConfig.category,
            WebsocketConfig.PbTypeConnectReqCommand,
            builder.build().toByteArray()
        )
        socketClient!!.send(connectBytes)
        Log.i(TAG, "sendConnectCommand")
    }

    private fun connectWebSocket(connectCallback: ConnectCallback?, node_id: String?) {
        if (node_id != null) {
            if (socketClient == null) {
                return
            }
            socketClient!!.initConnectionParam(node_id)
            if (!socketClient!!.isOpen) {
                if (socketClient!!.readyState.equals(ReadyState.NOT_YET_CONNECTED)) {
                    try {
                        socketClient!!.setConnectCallback(connectCallback)
                        socketClient!!.connect()
                    } catch (e: IllegalStateException) {
                    }
                } else if (socketClient!!.readyState.equals(ReadyState.CLOSING) || socketClient!!.readyState.equals(
                        ReadyState.CLOSED
                    )
                ) {
                    socketClient!!.reconnect()
                }
            } else {
                Log.e(TAG, "WebSocket is already connected")
                connectCallback?.alreadyConnected()
            }
        } else {
            connectCallback?.onFail("connect websocket error")
            Log.e(TAG, "node id is null or init error")
        }
    }

    fun sendBridgeConnectCommand(dAppID: String?, topic_id: String?) {
        val builder = Web3MQBridgeConnectCommand.newBuilder()
        builder.nodeID = node_id
        builder.dAppID = dAppID
        builder.topicID = topic_id
        val connectBridgeBytes = CommonUtils.appendPrefix(
            WebsocketConfig.category,
            WebsocketConfig.PbTypeWeb3MQBridgeConnectCommand,
            builder.build().toByteArray()
        )
        getSocketClient()!!.send(connectBridgeBytes)
    }

    fun getNodeId(): String? {
        return node_id
    }

    fun getSocketClient(): Web3MQSocketClient? {
        return socketClient
    }

    fun close() {
        if (socketClient != null) {
            try {
                socketClient!!.closeBlocking()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    fun getApiKey(): String? {
        return api_Key
    }
}