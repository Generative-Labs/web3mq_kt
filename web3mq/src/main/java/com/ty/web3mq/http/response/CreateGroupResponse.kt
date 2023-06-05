package com.ty.web3mq.http.response

import com.ty.web3mq.http.beans.ContactsBean
import com.ty.web3mq.http.beans.GroupBean


class CreateGroupResponse(code: Int, msg: String?, data: GroupBean?) : BaseResponse<GroupBean?>(code, msg, data)