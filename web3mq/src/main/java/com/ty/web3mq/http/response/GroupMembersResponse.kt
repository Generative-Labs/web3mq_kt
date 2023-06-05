package com.ty.web3mq.http.response

import com.ty.web3mq.http.beans.GroupMembersBean

class GroupMembersResponse(code: Int, msg: String?, data: GroupMembersBean?)  : BaseResponse<GroupMembersBean?>(code, msg, data)