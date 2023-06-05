package com.ty.web3mq.interfaces

import com.ty.web3mq.http.beans.ContactsBean


interface GetContactsCallback {
    fun onSuccess(contactsBean: ContactsBean?)
    fun onFail(error: String?)
}