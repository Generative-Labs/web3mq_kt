package com.ty.web3mq.interfaces

import com.ty.web3mq.http.beans.TopicBean


interface GetMyCreateTopicCallback {
    fun onSuccess(topicList: ArrayList<TopicBean?>?)
    fun onFail(error: String?)
}