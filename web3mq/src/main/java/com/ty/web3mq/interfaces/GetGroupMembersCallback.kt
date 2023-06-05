package com.ty.web3mq.interfaces

import com.ty.web3mq.http.beans.GroupMembersBean


interface GetGroupMembersCallback {
    fun onSuccess(groupMembersBean: GroupMembersBean)
    fun onFail(error: String)
}