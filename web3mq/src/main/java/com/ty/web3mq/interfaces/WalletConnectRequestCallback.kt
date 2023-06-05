package com.ty.web3mq.interfaces

import com.ty.web3mq.websocket.bean.BridgeMessageMetadata

interface WalletConnectRequestCallback {
    fun onConnectWalletSuccess(walletInfo: BridgeMessageMetadata?)
}