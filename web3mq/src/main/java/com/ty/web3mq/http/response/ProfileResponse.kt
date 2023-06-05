package com.ty.web3mq.http.response

import com.ty.web3mq.http.beans.ProfileBean

class ProfileResponse (code: Int, msg: String?, data: ProfileBean?): BaseResponse<ProfileBean?>(code, msg, data)