package com.ty.web3mq.interfaces

import com.ty.web3mq.http.beans.FriendRequestsBean


interface GetSentFriendRequestListCallback {
    fun onSuccess(requestsBean: FriendRequestsBean?)
    fun onFail(error: String?)
}