package com.ty.web3mq.http.response

import com.ty.web3mq.http.beans.ChatsBean

class ChatResponse(code: Int, msg: String?, data: ChatsBean?) : BaseResponse<ChatsBean?>(code, msg, data)