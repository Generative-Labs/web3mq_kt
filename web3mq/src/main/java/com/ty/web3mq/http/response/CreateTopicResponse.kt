package com.ty.web3mq.http.response

import com.ty.web3mq.http.beans.GroupBean
import com.ty.web3mq.http.beans.TopicBean

class CreateTopicResponse(code: Int, msg: String?, data: TopicBean?)  : BaseResponse<TopicBean?>(code, msg, data)