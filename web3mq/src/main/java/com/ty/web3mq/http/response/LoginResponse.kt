package com.ty.web3mq.http.response

import com.ty.web3mq.http.beans.RegisterBean

class LoginResponse (code: Int, msg: String?, data: RegisterBean?): BaseResponse<RegisterBean?>(code, msg, data)