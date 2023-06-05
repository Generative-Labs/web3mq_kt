package com.ty.web3mq.interfaces

import com.ty.web3mq.http.beans.UserPermissionsBean


interface GetUserPermissionCallback {
    fun onSuccess(userPermissions: UserPermissionsBean)
    fun onFail(error: String)
}