package com.ty.web3mq.http.response

open class BaseResponse<T>(var code : Int, var msg: String?, var data: T? )