package com.ty.web3mq.utils

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.ty.web3mq.http.beans.FollowerBean
import com.ty.web3mq.http.beans.FollowersBean
import com.ty.web3mq.http.beans.UserPermissionsBean
import com.ty.web3mq.websocket.bean.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.lang.StringBuilder
import java.net.URLDecoder
import java.util.ArrayList

object ConvertUtil {
    private const val TAG = "ConvertUtil"
    fun convertJsonToFollowersBean(json: String?): FollowersBean? {
        var bean: FollowersBean? = null
        try {
            val jsonObject = JSONObject(json)
            val data: JSONObject = jsonObject.getJSONObject("data")
            bean = FollowersBean()
            bean.total_count = data.getInt("total_count")
            val user_list: ArrayList<FollowerBean> = ArrayList<FollowerBean>()
            val jsonArray: JSONArray = data.getJSONArray("user_list")
            for (i in 0 until jsonArray.length()) {
                val obj: JSONObject = jsonArray.getJSONObject(i)
                val followerBean = FollowerBean()
                followerBean.follow_status = obj.getString("follow_status")
                followerBean.avatar_url = obj.getString("avatar_url")
                followerBean.nickname = obj.getString("nickname")
                followerBean.userid = obj.getString("userid")
                followerBean.wallet_address = obj.getString("wallet_address")
                followerBean.wallet_type = obj.getString("wallet_type")
                followerBean.permissions = obj.getJSONObject("permissions").toString()
                user_list.add(followerBean)
            }
            bean.user_list = user_list
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return bean
    }

    fun convertJsonToUserPermissions(json: String?): UserPermissionsBean? {
        var bean: UserPermissionsBean? = null
        try {
            val jsonObject = JSONObject(json)
            val data: JSONObject = jsonObject.getJSONObject("data")
            bean = UserPermissionsBean()
            bean.target_userid = data.getString("target_userid")
            bean.chat_permission =
                data.getJSONObject("permissions").getJSONObject("user:chat").getString("value")
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return bean
    }

    fun convertJsonToBridgeMessageContent(json_content: String?, gson: Gson): BridgeMessageContent {
        val bridgeMessageContent = BridgeMessageContent()
        val jsonParser = JsonParser()
        val element: JsonElement = jsonParser.parse(json_content)
        if (element.getAsJsonObject().has("params")) {
            if (element.getAsJsonObject().get("method").getAsString() == "provider_authorization") {
                // connect request
                bridgeMessageContent.type = BridgeMessageContent.TYPE_CONNECT_REQUEST
                bridgeMessageContent.content =
                    gson.fromJson<Any>(json_content, ConnectRequest::class.java)
            } else if (element.getAsJsonObject().get("method").getAsString() == "personal_sign") {
                // sign request
                bridgeMessageContent.type = BridgeMessageContent.TYPE_SIGN_REQUEST
                bridgeMessageContent.content =
                    gson.fromJson<Any>(json_content, SignRequest::class.java)
            }
        } else {
            //response
            if (element.getAsJsonObject().has("result")) {
                //success response
                if (element.getAsJsonObject().get("method")
                        .getAsString() == "provider_authorization"
                ) {
                    //connect response
                    bridgeMessageContent.type = BridgeMessageContent.TYPE_CONNECT_SUCCESS_RESPONSE
                    bridgeMessageContent.content =
                        gson.fromJson<Any>(json_content, ConnectSuccessResponse::class.java)
                } else if (element.getAsJsonObject().get("method")
                        .getAsString() == "personal_sign"
                ) {
                    //sign response
                    bridgeMessageContent.type = BridgeMessageContent.TYPE_SIGN_SUCCESS_RESPONSE
                    bridgeMessageContent.content =
                        gson.fromJson<Any>(json_content, SignSuccessResponse::class.java)
                }
            } else if (element.getAsJsonObject().has("error")) {
                //error response
                if (element.getAsJsonObject().get("method")
                        .getAsString() == "provider_authorization"
                ) {
                    bridgeMessageContent.type = BridgeMessageContent.TYPE_CONNECT_ERROR_RESPONSE
                } else if (element.getAsJsonObject().get("method")
                        .getAsString() == "personal_sign"
                ) {
                    bridgeMessageContent.type = BridgeMessageContent.TYPE_SIGN_ERROR_RESPONSE
                }
                bridgeMessageContent.content =
                    gson.fromJson<Any>(json_content, ErrorResponse::class.java)
            }
        }
        return bridgeMessageContent
    }

    fun convertDeepLinkToConnectRequest(deepLink: String): ConnectRequest {
        val request = ConnectRequest()
        val strs = deepLink.replace("web3mq://?", "").split("&".toRegex()).toTypedArray()
        for (str in strs) {
            Log.i(TAG, "str:$str")
            val `as` = str.split("=".toRegex()).toTypedArray()
            if (`as`.size < 2) {
                continue
            }
            val key = `as`[0]
            val value = `as`[1]
            request.icons = ArrayList<String>()
            if (key == "topic") {
                request.topic = URLDecoder.decode(value)
            }
            if (key == "request[id]") {
                request.id = URLDecoder.decode(value)
            }
            if (key == "request[jsonrpc]") {
                request.jsonrpc = URLDecoder.decode(value)
            }
            if (key == "request[method]") {
                request.method = URLDecoder.decode(value)
            }
            if (key == "proposer[appMetadata][description]") {
                request.description = URLDecoder.decode(value)
            }
            if (key == "proposer[publicKey]") {
                request.publicKey = URLDecoder.decode(value)
            }
            if (key == "proposer[metadata][icons][]") {
                request.icons!!.add(URLDecoder.decode(value))
            }
            if (key == "proposer[metadata][redirect]") {
                request.redirect = URLDecoder.decode(value)
            }
            if (key == "proposer[appMetadata][name]") {
                request.name = URLDecoder.decode(value)
            }
            if (key == "proposer[appMetadata][url]") {
                request.url = URLDecoder.decode(value)
            }
            if (key == "request[params][sessionProperties][expiry]") {
                request.expiry = URLDecoder.decode(value)
            }
        }


//        request.topic = uri.getQueryParameter("topic");
//        request.id = uri.getQueryParameter(Uri.decode("request[id]").replace("[","%5B").replace("]", "%5D"));
//        Log.i(TAG,"request id:"+request.id);
//        request.jsonrpc = uri.getQueryParameter(Uri.decode("request[jsonrpc]"));
//        request.method = uri.getQueryParameter(Uri.decode("request[method]"));
//        request.description = uri.getQueryParameter(Uri.decode("proposer[appMetadata][description]"));
//        request.publicKey = uri.getQueryParameter(Uri.decode("proposer[publicKey]"));
//        request.icons = uri.getQueryParameters(Uri.decode("proposer[metadata][icons]"));
//        request.redirect = uri.getQueryParameter(Uri.decode("proposer[metadata][redirect]"));
//        request.name = uri.getQueryParameter(Uri.decode("proposer[appMetadata][name]"));
//        request.url = uri.getQueryParameter(Uri.decode("proposer[appMetadata][url]"));
        return request
    }

    fun convertConnectRequestToDeepLink(request: ConnectRequest): String {
        val url = "web3mq://?request[id]=" + request.id.toString() +
                "&topic=" + request.topic.toString() +
                "&request[jsonrpc]=" + request.jsonrpc.toString() +
                "&request[method]=" + request.method.toString() +
                "&proposer[appMetadata][description]" + request.description.toString() +
                "&proposer[publicKey]=" + request.publicKey.toString() +
                "&proposer[metadata][redirect]=" + request.redirect.toString() +
                "&proposer[appMetadata][name]=" + request.name.toString() +
                "&proposer[appMetadata][url]=" + request.url.toString() +
                "&request[params][sessionProperties][expiry]=" + request.expiry
        val deepLink = StringBuilder(url)
        if (request.icons != null) {
            for (icon in request.icons!!) {
                deepLink.append("&").append("proposer[appMetadata][icons][]=").append(icon)
            }
        }
        return deepLink.toString()
    }
}