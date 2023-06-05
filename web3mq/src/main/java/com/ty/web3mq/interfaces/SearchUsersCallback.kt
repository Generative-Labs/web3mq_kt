package com.ty.web3mq.interfaces

import com.ty.web3mq.http.beans.UsersBean


interface SearchUsersCallback {
    fun onSuccess(usersBean: UsersBean?)
    fun onFail(error: String?)
}