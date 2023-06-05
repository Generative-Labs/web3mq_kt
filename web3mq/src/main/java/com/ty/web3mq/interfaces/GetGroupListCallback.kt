package com.ty.web3mq.interfaces

import com.ty.web3mq.http.beans.GroupsBean


interface GetGroupListCallback {
    fun onSuccess(groups: GroupsBean?)
    fun onFail(error: String?)
}