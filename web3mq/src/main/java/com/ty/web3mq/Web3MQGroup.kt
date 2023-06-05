package com.ty.web3mq

import com.ty.web3mq.http.ApiConfig
import com.ty.web3mq.http.HttpManager
import com.ty.web3mq.http.request.CreateGroupRequest
import com.ty.web3mq.http.request.GetGroupListRequest
import com.ty.web3mq.http.request.GetGroupMembersRequest
import com.ty.web3mq.http.request.InvitationGroupRequest
import com.ty.web3mq.http.response.CreateGroupResponse
import com.ty.web3mq.http.response.GroupMembersResponse
import com.ty.web3mq.http.response.GroupsResponse
import com.ty.web3mq.http.response.InvitationGroupResponse
import com.ty.web3mq.interfaces.CreateGroupCallback
import com.ty.web3mq.interfaces.GetGroupListCallback
import com.ty.web3mq.interfaces.GetGroupMembersCallback
import com.ty.web3mq.interfaces.InvitationGroupCallback
import com.ty.web3mq.utils.DefaultSPHelper
import com.ty.web3mq.utils.Ed25519
import java.lang.Exception
import java.net.URLEncoder

object Web3MQGroup  {
    fun createGroup(group_name: String, callback: CreateGroupCallback) {
        try {
            val pub_key: String = DefaultSPHelper.getTempPublic()!!
            val prv_key_seed: String = DefaultSPHelper.getTempPrivate()!!
            val did_key: String = DefaultSPHelper.getDidKey()!!
            val request = CreateGroupRequest()
            request.group_name = group_name
            request.userid = DefaultSPHelper.getUserID()
            request.timestamp = System.currentTimeMillis()
            request.web3mq_signature =
                Ed25519.ed25519Sign(prv_key_seed, (request.userid + request.timestamp).toByteArray())
            HttpManager.post(
                ApiConfig.GROUP_CREATE,
                request,
                pub_key,
                did_key,
                CreateGroupResponse::class.java,
                object : HttpManager.Callback<CreateGroupResponse> {
                    override fun onResponse(response: CreateGroupResponse) {
                        if (response.code == 0) {
                            callback.onSuccess(response.data!!)
                        } else {
                            callback.onFail(
                                "error code: " + response.code
                                    .toString() + " msg:" + response.msg
                            )
                        }
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

    fun invite(groupid: String, member_ids: Array<String>, callback: InvitationGroupCallback) {
        try {
            val pub_key: String = DefaultSPHelper.getTempPublic()!!
            val prv_key_seed: String = DefaultSPHelper.getTempPrivate()!!
            val did_key: String = DefaultSPHelper.getDidKey()!!
            val request = InvitationGroupRequest()
            request.groupid = groupid
            request.userid = DefaultSPHelper.getUserID()
            request.timestamp = System.currentTimeMillis()
            request.members = member_ids
            request.web3mq_signature = Ed25519.ed25519Sign(
                prv_key_seed,
                (request.userid + request.groupid + request.timestamp).toByteArray()
            )
            HttpManager.post(
                ApiConfig.GROUP_INVITATION,
                request,
                pub_key,
                did_key,
                InvitationGroupResponse::class.java,
                object : HttpManager.Callback<InvitationGroupResponse> {
                    override fun onResponse(response: InvitationGroupResponse) {
                        if (response.code == 0) {
                            callback.onSuccess(response.data!!)
                        } else {
                            callback.onFail(
                                "error code: " + response.code
                                    .toString() + " msg:" + response.msg
                            )
                        }
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

    fun getGroupList(page: Int, size: Int, callback: GetGroupListCallback) {
        try {
            val pub_key: String = DefaultSPHelper.getTempPublic()!!
            val prv_key_seed: String = DefaultSPHelper.getTempPrivate()!!
            val did_key: String = DefaultSPHelper.getDidKey()!!
            val request = GetGroupListRequest()
            request.page = page
            request.size = size
            request.userid = DefaultSPHelper.getUserID()
            request.timestamp = System.currentTimeMillis()
            request.web3mq_signature = URLEncoder.encode(
                Ed25519.ed25519Sign(
                    prv_key_seed,
                    (request.userid + request.timestamp).toByteArray()
                )
            )
            HttpManager.get(
                ApiConfig.GET_GROUP_LIST,
                request,
                pub_key,
                did_key,
                GroupsResponse::class.java,
                object : HttpManager.Callback<GroupsResponse>{
                    override fun onResponse(response: GroupsResponse) {
                        if (response.code == 0) {
                            callback.onSuccess(response.data)
                        } else {
                            callback.onFail(
                                "error code: " + response.code
                                    .toString() + " msg:" + response.msg
                            )
                        }
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

    fun getGroupMembers(page: Int, size: Int, groupid: String, callback: GetGroupMembersCallback) {
        try {
            val pub_key: String = DefaultSPHelper.getTempPublic()!!
            val prv_key_seed: String = DefaultSPHelper.getTempPrivate()!!
            val did_key: String = DefaultSPHelper.getDidKey()!!
            val request = GetGroupMembersRequest()
            request.page = page
            request.size = size
            request.userid = DefaultSPHelper.getUserID()
            request.timestamp = System.currentTimeMillis()
            request.groupid = groupid
            request.web3mq_signature = URLEncoder.encode(
                Ed25519.ed25519Sign(
                    prv_key_seed,
                    (request.userid + request.groupid + request.timestamp).toByteArray()
                )
            )
            HttpManager.get(
                ApiConfig.GET_GROUP_MEMBERS,
                request,
                pub_key,
                did_key,
                GroupMembersResponse::class.java,
                object : HttpManager.Callback<GroupMembersResponse> {
                    override fun onResponse(response: GroupMembersResponse) {
                        if (response.code == 0) {
                            callback.onSuccess(response.data!!)
                        } else {
                            callback.onFail(
                                "error code: " + response.code
                                    .toString() + " msg:" + response.msg
                            )
                        }
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
}