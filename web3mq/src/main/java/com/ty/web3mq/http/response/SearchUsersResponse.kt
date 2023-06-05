package com.ty.web3mq.http.response

import com.ty.web3mq.http.beans.UsersBean

class SearchUsersResponse(code: Int, msg: String?, data: UsersBean?) : BaseResponse<UsersBean?>(code, msg, data)