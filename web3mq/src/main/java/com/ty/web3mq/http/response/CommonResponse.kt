package com.ty.web3mq.http.response

import com.ty.web3mq.http.beans.ChatsBean

class CommonResponse(code: Int, msg: String?, data: String?) : BaseResponse<String?>(code, msg, data)