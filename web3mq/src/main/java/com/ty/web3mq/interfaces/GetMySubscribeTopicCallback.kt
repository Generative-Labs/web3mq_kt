package com.ty.web3mq.interfaces

import com.ty.web3mq.http.beans.TopicBean


interface GetMySubscribeTopicCallback {
    fun onSuccess(topicList: ArrayList<TopicBean?>?)
    fun onFail(error: String?)
}