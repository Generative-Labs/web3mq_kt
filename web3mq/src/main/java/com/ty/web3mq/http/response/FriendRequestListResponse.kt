package com.ty.web3mq.http.response

import com.ty.web3mq.http.beans.FollowersBean
import com.ty.web3mq.http.beans.FriendRequestsBean

class FriendRequestListResponse(code: Int, msg: String?, data: FriendRequestsBean?)  : BaseResponse<FriendRequestsBean?>(code, msg, data)