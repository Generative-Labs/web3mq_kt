package com.ty.web3mq.http.request

class PublishTopicMessageRequest : BaseRequest() {
    var userid: String? = null
    var topicid: String? = null
    var title: String? = null
    var content: String? = null
    var timestamp: Long = 0
    var web3mq_signature: String? = null
}