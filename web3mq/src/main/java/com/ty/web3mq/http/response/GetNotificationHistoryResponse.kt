package com.ty.web3mq.http.response

import com.ty.web3mq.http.beans.FollowersBean
import com.ty.web3mq.http.beans.NotificationsBean

class GetNotificationHistoryResponse(code: Int, msg: String?, data: NotificationsBean?)  : BaseResponse<NotificationsBean?>(code, msg, data)