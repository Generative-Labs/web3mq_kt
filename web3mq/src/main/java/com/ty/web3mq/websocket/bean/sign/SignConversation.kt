package com.ty.web3mq.websocket.bean.sign

import com.ty.web3mq.websocket.bean.ErrorResponse
import com.ty.web3mq.websocket.bean.SignRequest
import com.ty.web3mq.websocket.bean.SignSuccessResponse


data class SignConversation(val id: String, val request: SignRequest, var successResponse: SignSuccessResponse?, var errorResponse: ErrorResponse?)