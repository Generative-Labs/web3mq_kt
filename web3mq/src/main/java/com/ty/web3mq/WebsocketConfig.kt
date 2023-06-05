package com.ty.web3mq

object WebsocketConfig {
    const val WS_PROTOCOL = "wss"
    const val WS_DEV_HOST_URL = "dev-ap-jp-1.web3mq.com"
    const val WS_TEST_NET_HOST_URL = "testnet-ap-jp-1.web3mq.com"
    const val WS_URL_DEV = WS_PROTOCOL + "://" + WS_DEV_HOST_URL + "/messages"
    const val WS_URL_TEST_NET = WS_PROTOCOL + "://" + WS_TEST_NET_HOST_URL + "/messages"

    // ping
    const val PbTypePingCommand = 128.toByte()
    const val PbTypePongCommand = 129.toByte()

    // connect to node
    const val PbTypeConnectReqCommand: Byte = 2
    const val PbTypeConnectRespCommand: Byte = 3

    // normally message
    const val PbTypeMessage: Byte = 16
    const val PbTypeMessageStatusResp: Byte = 21

    // notification
    const val PbTypeNotificationListResp: Byte = 20

    // connect to bridge
    const val PbTypeWeb3MQBridgeConnectCommand: Byte = 100
    const val PbTypeWeb3MQBridgeConnectResp: Byte = 101

    const val category = 10
}