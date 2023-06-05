package com.ty.web3mq.interfaces

import com.ty.web3mq.http.beans.TopicBean


interface CreateTopicCallback {
    fun onSuccess(topicBean: TopicBean?)
    fun onFail(error: String?)
}