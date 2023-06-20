package com.ty.web3mq
import com.ty.web3mq.http.ApiConfig
import com.ty.web3mq.http.HttpManager
import com.ty.web3mq.http.request.AddFriendsRequest
import com.ty.web3mq.http.request.FollowRequest
import com.ty.web3mq.http.request.GetFollowerRequest
import com.ty.web3mq.http.response.CommonResponse
import com.ty.web3mq.http.response.FollowResponse
import com.ty.web3mq.interfaces.*
import com.ty.web3mq.utils.CommonUtils
import com.ty.web3mq.utils.DefaultSPHelper
import com.ty.web3mq.utils.Ed25519
import java.net.URLEncoder

object Web3MQFollower  {
    const val ACTION_FOLLOW = "follow"
    const val ACTION_CANCEL = "cancel"
    fun getFollowSignContent(wallet_type: String, wallet_address: String, nonce: String): String {
        val str_date: String = CommonUtils.date
        return """Web3MQ wants you to sign in with your $wallet_type account:
$wallet_address

For follow signature

Nonce: $nonce
Issued At: $str_date"""
    }

    fun follow(
        target_userid: String,
        action: String,
        did_signature: String,
        sign_content: String,
        timeStamp: Long,
        callback: FollowCallback
    ) {
        try {
            val pub_key: String = DefaultSPHelper.getTempPublic()!!
            val prv_key_seed: String = DefaultSPHelper.getTempPrivate()!!
            val did_key: String = DefaultSPHelper.getDidKey()!!
            val request = FollowRequest()
            request.userid = DefaultSPHelper.getUserID()
            request.target_userid = target_userid
            request.timestamp = timeStamp
            request.action = action
            request.did_type = did_key.split(":".toRegex()).toTypedArray()[0]
            request.did_signature = did_signature
            request.did_pubkey = did_key.split(":".toRegex()).toTypedArray()[1]
            request.sign_content = sign_content
            HttpManager.post(
                ApiConfig.POST_FOLLOW,
                request,
                pub_key,
                did_key,
                FollowResponse::class.java,
                object : HttpManager.Callback<FollowResponse> {
                    override fun onResponse(response: FollowResponse) {
                        if (response.code == 0) {
                            Web3MQChats.updateMyChat(timeStamp, target_userid, "user", object :UpdateMyChatCallback{
                                    override fun onSuccess(){
                                        callback.onSuccess()
                                    }

                                    override fun onFail(error: String){
                                        callback.onFail("update chat error")
                                    }
                                })

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

    fun getMyFollowers(page: Int, size: Int, callback: GetMyFollowersCallback) {
        try {
            val pub_key: String = DefaultSPHelper.getTempPublic()!!
            val prv_key_seed: String = DefaultSPHelper.getTempPrivate()!!
            val did_key: String = DefaultSPHelper.getDidKey()!!
            val request = GetFollowerRequest()
            request.page = page
            request.size = size
            request.userid = DefaultSPHelper.getUserID()
            request.timestamp = System.currentTimeMillis()
            request.web3mq_user_signature = URLEncoder.encode(
                Ed25519.ed25519Sign(
                    prv_key_seed,
                    (request.userid + request.timestamp).toByteArray()
                )
            )
            HttpManager.get(
                ApiConfig.GET_MY_FOLLOWERS,
                request,
                pub_key,
                did_key,
                object : HttpManager.Callback<String> {
                    override fun onResponse(response: String) {
                        callback.onSuccess(response)
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

    fun getMyFollowing(page: Int, size: Int, callback: GetMyFollowingCallback) {
        try {
            val pub_key: String = DefaultSPHelper.getTempPublic()!!
            val prv_key_seed: String = DefaultSPHelper.getTempPrivate()!!
            val did_key: String = DefaultSPHelper.getDidKey()!!
            val request = GetFollowerRequest()
            request.page = page
            request.size = size
            request.userid = DefaultSPHelper.getUserID()
            request.timestamp = System.currentTimeMillis()
            request.web3mq_user_signature = URLEncoder.encode(
                Ed25519.ed25519Sign(
                    prv_key_seed,
                    (request.userid + request.timestamp).toByteArray()
                )
            )
            HttpManager.get(
                ApiConfig.GET_MY_FOLLOWING,
                request,
                pub_key,
                did_key,
                object : HttpManager.Callback<String> {
                    override fun onResponse(response: String) {
                        callback.onSuccess(response)
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

    fun getFollowerAndFollowing(page: Int, size: Int, callback: GetMyFollowingCallback) {
        try {
            val pub_key: String = DefaultSPHelper.getTempPublic()!!
            val prv_key_seed: String = DefaultSPHelper.getTempPrivate()!!
            val did_key: String = DefaultSPHelper.getDidKey()!!
            val request = GetFollowerRequest()
            request.page = page
            request.size = size
            request.userid = DefaultSPHelper.getUserID()
            request.timestamp = System.currentTimeMillis()
            request.web3mq_user_signature = URLEncoder.encode(
                Ed25519.ed25519Sign(
                    prv_key_seed,
                    (request.userid + request.timestamp).toByteArray()
                )
            )
            HttpManager.get(
                ApiConfig.GET_FOLLOWERS_AND_FOLLOWING,
                request,
                pub_key,
                did_key,
                object : HttpManager.Callback<String>{
                    override fun onResponse(response: String) {
                        callback.onSuccess(response)
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

    fun sendFriendRequest(
        target_userid: String,
        timeStamp: Long,
        content: String,
        callback: SendFriendRequestCallback
    ) {
        try {
            val pub_key: String = DefaultSPHelper.getTempPublic()!!
            val prv_key_seed: String = DefaultSPHelper.getTempPrivate()!!
            val did_key: String = DefaultSPHelper.getDidKey()!!
            val request = AddFriendsRequest()
            request.userid = DefaultSPHelper.getUserID()
            request.target_userid = target_userid
            request.timestamp = timeStamp
            request.content = content
            request.web3mq_signature = Ed25519.ed25519Sign(
                prv_key_seed,
                (request.userid + request.target_userid.toString() + content + timeStamp).toByteArray()
            )
            HttpManager.post(
                ApiConfig.ADD_FRIENDS,
                request,
                pub_key,
                did_key,
                CommonResponse::class.java,
                object : HttpManager.Callback<CommonResponse>{
                    override fun onResponse(response: CommonResponse) {
                        if (response.code == 0) {
                            callback.onSuccess()
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