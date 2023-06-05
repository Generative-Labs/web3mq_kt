package com.ty.web3mq.http.response

import com.ty.web3mq.http.beans.GroupsBean

class GroupsResponse(code: Int, msg: String?, data: GroupsBean?) : BaseResponse<GroupsBean?>(code, msg, data)