package com.ty.web3mq.http.response

import com.ty.web3mq.http.beans.UserInfo

class UserInfoResponse (code: Int, msg: String?, data: UserInfo?): BaseResponse<UserInfo?>(code, msg, data)