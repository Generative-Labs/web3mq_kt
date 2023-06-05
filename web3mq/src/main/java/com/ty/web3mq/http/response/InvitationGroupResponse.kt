package com.ty.web3mq.http.response

import com.ty.web3mq.http.beans.GroupBean

class InvitationGroupResponse (code: Int, msg: String?, data: GroupBean?): BaseResponse<GroupBean?>(code, msg, data)