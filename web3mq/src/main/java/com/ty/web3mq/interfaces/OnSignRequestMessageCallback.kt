package com.ty.web3mq.interfaces

import com.ty.web3mq.websocket.bean.sign.Participant


interface OnSignRequestMessageCallback {
    fun onSignRequestMessage(
        id: String,
        participant: Participant?,
        address: String,
        sign_raw: String
    )
}