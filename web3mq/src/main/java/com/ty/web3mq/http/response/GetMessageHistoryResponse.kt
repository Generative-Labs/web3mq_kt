package com.ty.web3mq.http.response

import com.ty.web3mq.http.beans.FollowersBean
import com.ty.web3mq.http.beans.MessagesBean

class GetMessageHistoryResponse(code: Int, msg: String?, data: MessagesBean?) : BaseResponse<MessagesBean?>(code, msg, data)