package com.ty.web3mq.websocket.bean

class SignRequest {
    var id: String? = null
    var jsonrpc: String? = null
    var method: String? = null

    //[message,address,password]
    var params: ArrayList<String>? = null
    fun getAddress(): String? {
        return if (params != null && params!!.size > 1) {
            params!![1]
        } else null
    }

    fun getSignRaw(): String? {
        return if (params != null && params!!.size > 0) {
            params!![0]
        } else null
    }

    fun getPassword(): String? {
        return if (params != null && params!!.size > 2) {
            params!![2]
        } else null
    }
    //    public BridgeMessageProposer proposer;
    //    public BridgeMessageWalletInfo walletInfo;
    //    public String signRaw;
    //    public String address;
    //    public String signature;
    //    public boolean approve;
    //    public String requestId;
    //    public String userInfo;
}