package com.ty.web3mq.websocket.bean

import com.ty.web3mq.websocket.bean.Namespaces
import com.ty.web3mq.websocket.bean.BridgeMessageMetadata
import com.ty.web3mq.websocket.bean.AuthorizationResponseSuccessData
import com.ty.web3mq.websocket.bean.ResponseErrorData

class SignSuccessResponse {
    var id: String? = null
    var jsonrpc: String? = null
    var method: String? = null
    var result: String? = null //    public String action;
    //    public BridgeMessageProposer proposer;
    //    public BridgeMessageWalletInfo walletInfo;
    //    public String signRaw;
    //    public String address;
    //    public String signature;
    //    public boolean approve;
    //    public String requestId;
    //    public String userInfo;
}