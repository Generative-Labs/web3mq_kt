package com.ty.web3mq.http.response

import com.ty.web3mq.http.beans.ContactsBean

class ContactsResponse(code: Int, msg: String?, data: ContactsBean?) : BaseResponse<ContactsBean?>(code, msg, data)