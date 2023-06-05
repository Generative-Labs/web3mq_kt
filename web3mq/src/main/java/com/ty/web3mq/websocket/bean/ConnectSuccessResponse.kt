package com.ty.web3mq.websocket.bean

class ConnectSuccessResponse {
    var id: String? = null
    var jsonrpc: String? = null
    var method: String? = null
    var result: AuthorizationResponseSuccessData? = null

    //eip155:42161:0x0910e12C68d02B561a34569E1367c9AAb42bd810
    open fun getETHAddress(): String? {
        if (result != null && result!!.sessionNamespaces != null) {
            val namespaces = result!!.sessionNamespaces!!["eip155"]
            if (namespaces != null && namespaces.accounts != null && namespaces.accounts!!.size > 0) {
                val account = namespaces.accounts!![0]!!
                //eip155:42161:0x0910e12C68d02B561a34569E1367c9AAb42bd810
                val account_ = account.split(":".toRegex()).toTypedArray()
                if (account_.size == 3) {
                    return account_[2]
                }
            }
        }
        return null
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