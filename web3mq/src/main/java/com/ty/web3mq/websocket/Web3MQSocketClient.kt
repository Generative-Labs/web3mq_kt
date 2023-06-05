package com.ty.web3mq.websocket

import android.os.Handler
import android.util.Log
import com.ty.web3mq.WebsocketConfig
import com.ty.web3mq.interfaces.ConnectCallback
import com.ty.web3mq.interfaces.OnWebsocketClosedCallback
import com.ty.web3mq.utils.CommonUtils
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import pb.KeepAlive
import java.net.URI
import java.nio.ByteBuffer
import java.util.*

class Web3MQSocketClient(var serverUri: URI?) : WebSocketClient(serverUri) {
    private var node_id: String? = null
    private var callback: ConnectCallback? = null
    private var onWebsocketClosedCallback: OnWebsocketClosedCallback? = null
    private val mHandler = Handler()

    fun switchUri(uri: URI) {
        serverUri = uri
    }

    fun initConnectionParam(node_id: String?) {
        this.node_id = node_id
        this.setConnectionLostTimeout(0)
    }

    override fun onOpen(handshakedata: ServerHandshake?) {
        if (callback != null) {
            mHandler.post { callback!!.onSuccess() }
        }
        Log.i(TAG, "WebSocketClient onOpen")
        val timer = Timer(true)
        val timerTask: TimerTask = object : TimerTask() {
            override fun run() {
                ping()
            }
        }
        timer.schedule(timerTask, 0, 60000)
    }

    private fun ping() {
        if (this.isClosed() || this.isClosing()) {
            Log.e(TAG, "websocket closed")
            return
        }
        Log.i(TAG, "send ping")
        val builder: KeepAlive.WebsocketPingCommand.Builder =
            KeepAlive.WebsocketPingCommand.newBuilder()
        builder.setNodeId(node_id)
        val timestamp = System.currentTimeMillis()
        builder.setTimestamp(timestamp)
        val connectBytes: ByteArray = CommonUtils.appendPrefix(
            WebsocketConfig.category,
            WebsocketConfig.PbTypePingCommand,
            builder.build().toByteArray()
        )
        this.send(connectBytes)
    }

    override fun onMessage(message: String) {
        Log.i(TAG, "WebSocketClient onMessage $message")
    }

    override fun onMessage(bytes: ByteBuffer?) {
        MessageManager.onMessage(bytes!!)
    }

    override fun onClose(code: Int, reason: String, remote: Boolean) {
        Log.e(
            TAG,
            "WebSocketClient onClose code:$code reason:$reason remote:$remote"
        )
        if (onWebsocketClosedCallback != null) {
            onWebsocketClosedCallback!!.onClose()
        }
    }

    override fun onError(ex: Exception) {
        Log.e(TAG, "WebSocketClient onError " + ex.localizedMessage)
    }

    fun setConnectCallback(connectCallback: ConnectCallback?) {
        callback = connectCallback
    }

    fun setOnWebsocketClosedCallback(callback: OnWebsocketClosedCallback?) {
        onWebsocketClosedCallback = callback
    }

    companion object {
        private const val TAG = "Web3MQClient"
    }
}