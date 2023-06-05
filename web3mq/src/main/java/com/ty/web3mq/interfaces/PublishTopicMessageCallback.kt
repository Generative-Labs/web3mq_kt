package com.ty.web3mq.interfaces

interface PublishTopicMessageCallback {
    fun onSuccess()
    fun onFail(error: String?)
}