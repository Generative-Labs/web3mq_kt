package com.ty.web3mq.websocket.bean

import com.ty.web3mq.websocket.bean.Namespaces
import com.ty.web3mq.websocket.bean.BridgeMessageMetadata
import com.ty.web3mq.websocket.bean.AuthorizationResponseSuccessData
import com.ty.web3mq.websocket.bean.ResponseErrorData

class BridgeMessageContent {
    var type: String? = null
    var content: Any? = null

    companion object {
        //    public String action;
        //    public BridgeMessageProposer proposer;
        //    public BridgeMessageWalletInfo walletInfo;
        //    public String signRaw;
        //    public String address;
        //    public String signature;
        //    public boolean approve;
        //    public String requestId;
        //    public String userInfo;
        const val TYPE_CONNECT_REQUEST = "type_connect_request"
        const val TYPE_CONNECT_SUCCESS_RESPONSE = "type_connect_response"
        const val TYPE_SIGN_REQUEST = "type_sign_request"
        const val TYPE_SIGN_SUCCESS_RESPONSE = "type_sign_response"
        const val TYPE_CONNECT_ERROR_RESPONSE = "type_connect_error_response"
        const val TYPE_SIGN_ERROR_RESPONSE = "type_sign_error_response"
    }
}