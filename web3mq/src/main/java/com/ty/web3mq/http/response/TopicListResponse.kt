package com.ty.web3mq.http.response

import com.ty.web3mq.http.beans.TopicBean

class TopicListResponse (code: Int, msg: String?, data: java.util.ArrayList<TopicBean?>?): BaseResponse<java.util.ArrayList<TopicBean?>?>(code, msg, data)