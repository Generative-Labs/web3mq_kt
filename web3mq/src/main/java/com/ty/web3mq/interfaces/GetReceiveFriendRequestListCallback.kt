package com.ty.web3mq.interfaces

import com.ty.web3mq.http.beans.FriendRequestsBean


interface GetReceiveFriendRequestListCallback {
    fun onSuccess(friendRequestBeans: FriendRequestsBean?)
    fun onFail(error: String?)
}