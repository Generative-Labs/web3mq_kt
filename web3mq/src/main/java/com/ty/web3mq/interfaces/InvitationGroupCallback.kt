package com.ty.web3mq.interfaces

import com.ty.web3mq.http.beans.GroupBean


interface InvitationGroupCallback {
    fun onSuccess(invitationGroupBean: GroupBean)
    fun onFail(error: String)
}