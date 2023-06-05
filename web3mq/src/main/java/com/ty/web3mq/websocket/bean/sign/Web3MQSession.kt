package com.ty.web3mq.websocket.bean.sign

import java.util.HashMap

class Web3MQSession {
    /// 自己的 topicId
    var selfTopic: String? = null

    /// 对方的 topicId
    var peerTopic: String? = null

    /// 自己方信息，包括 publicKey 和 App 相关信息
    var selfParticipant: Participant? = null

    /// 对方信息，包括 publicKey 和 App 相关信息
    var peerParticipant: Participant? = null

    /// 过期时间
    var expiryDate: Long = 0

    /// 功能集 Namespace 相关参考 https://github.com/ChainAgnostic/CAIPs/blob/master/CAIPs/caip-25.md
    var namespaces: Array<String>? = null
    var signConversationMap: HashMap<String, SignConversation>? = null
}