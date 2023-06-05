package com.ty.web3mq.http.response

import com.ty.web3mq.http.beans.FollowBean

class FollowResponse(code: Int, msg: String?, data: FollowBean?) : BaseResponse<FollowBean?>(code, msg, data)