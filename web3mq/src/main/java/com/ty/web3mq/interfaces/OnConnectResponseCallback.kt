package com.ty.web3mq.interfaces

import com.ty.web3mq.websocket.bean.BridgeMessageMetadata


interface OnConnectResponseCallback {
    fun onApprove(walletInfo: BridgeMessageMetadata, address: String)
    fun onReject()
}