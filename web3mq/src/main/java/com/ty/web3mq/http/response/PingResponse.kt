package com.ty.web3mq.http.response

import com.ty.web3mq.http.beans.PingBean


class PingResponse(code: Int, msg: String?, data: PingBean?) :
    BaseResponse<PingBean>(code, msg, data)