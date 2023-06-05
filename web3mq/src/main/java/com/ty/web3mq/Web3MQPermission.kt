package com.ty.web3mq

import com.google.gson.JsonObject
import com.ty.web3mq.http.ApiConfig
import com.ty.web3mq.http.HttpManager
import com.ty.web3mq.http.request.GetUserPermissionsRequest
import com.ty.web3mq.http.request.UpdatePermissionsRequest
import com.ty.web3mq.interfaces.GetUserPermissionCallback
import com.ty.web3mq.interfaces.UpdateUserPermissionCallback
import com.ty.web3mq.utils.ConvertUtil
import com.ty.web3mq.utils.DefaultSPHelper
import com.ty.web3mq.utils.Ed25519
import java.net.URLEncoder

object Web3MQPermission {
    public const val CHAT_PERMISSION_PUBLIC = "public"
    public const val CHAT_PERMISSION_FOLLOWER = "follower"
    public const val CHAT_PERMISSION_FOLLOWING = "following"
    public const val CHAT_PERMISSION_FRIEND = "friend"

    fun getUserPermission(target_userid: String, callback: GetUserPermissionCallback) {
        try {
            val pub_key: String = DefaultSPHelper.getTempPublic()!!
            val prv_key_seed: String = DefaultSPHelper.getTempPrivate()!!
            val did_key: String = DefaultSPHelper.getDidKey()!!
            val request = GetUserPermissionsRequest()
            request.target_userid = target_userid
            request.userid = DefaultSPHelper.getUserID()
            request.timestamp = System.currentTimeMillis()
            request.web3mq_user_signature = URLEncoder.encode(
                Ed25519.ed25519Sign(
                    prv_key_seed,
                    (request.userid + request.target_userid + request.timestamp).toByteArray()
                )
            )
            HttpManager.get(
                ApiConfig.GET_USER_PERMISSIONS,
                request,
                pub_key,
                did_key,
                object : HttpManager.Callback<String> {
                    override fun onResponse(response: String) {
                        callback.onSuccess(ConvertUtil.convertJsonToUserPermissions(response)!!)
                    }

                    override fun onError(error: String) {
                        callback.onFail("error: $error")
                    }
                })
        } catch (e: Exception) {
            e.printStackTrace()
            callback.onFail("ed25519 sign error")
        }
    }

    fun updateChatPermission(chatPermission: String, callback: UpdateUserPermissionCallback){
        val pub_key: String = DefaultSPHelper.getTempPublic()!!
        val prv_key_seed: String = DefaultSPHelper.getTempPrivate()!!
        val did_key: String = DefaultSPHelper.getDidKey()!!
        val request = UpdatePermissionsRequest()
        request.userid = DefaultSPHelper.getUserID()
        request.timestamp = System.currentTimeMillis()
        request.web3mq_user_signature = URLEncoder.encode(
            Ed25519.ed25519Sign(
                prv_key_seed,
                (request.userid + request.timestamp).toByteArray()
            )
        )
        val permissions = JsonObject()
        val permission = JsonObject()
        permission.addProperty("type", "enum")
        permission.addProperty("value", chatPermission)
        permissions.add("user:chat", permission)
        request.permissions = permissions
        HttpManager.post(ApiConfig.UPDATE_USER_PERMISSION, request, pub_key, did_key, object : HttpManager.Callback<String>{
            override fun onResponse(response: String) {
                callback.onSuccess()
            }

            override fun onError(error: String) {
                callback.onFail("error: $error")
            }
        })
    }
}