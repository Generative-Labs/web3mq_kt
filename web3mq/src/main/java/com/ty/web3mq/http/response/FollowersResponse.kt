package com.ty.web3mq.http.response

import com.ty.web3mq.http.beans.FollowersBean

class FollowersResponse(code: Int, msg: String?, data: FollowersBean?) : BaseResponse<FollowersBean?>(code, msg, data)