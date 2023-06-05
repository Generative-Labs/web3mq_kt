package com.ty.web3mq.http

import android.content.Context
import android.text.TextUtils
import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.ty.web3mq.http.request.BaseRequest
import java.lang.StringBuilder

/**
 *
 */
object HttpManager {
    private var mGson: Gson? = null
    private const val TAG = "HttpManager"
    init {
        mGson = GsonBuilder()
            .disableHtmlEscaping()
            .create()
        FuelManager.instance.apply {
            // 设置日志级别为 INFO
            addRequestInterceptor { next ->
                { request ->
                    val response = next(request)
                    Log.i(TAG,"${request.method} URL: ${request.url}")
                    Log.i(TAG,"request parameters: ${request.parameters}")
                    response
                }
            }
        }
    }

    fun <T> post(
        url: String, request: BaseRequest?, pub_key: String?, didKey: String?, clazz: Class<T>,
        callback: Callback<T>
    ) {
        val fuelRequest = Fuel.post(url)
        if(pub_key !=null){
            fuelRequest.header(ApiConfig.Headers.PUB_KEY, pub_key)
        }
        if(didKey != null){
            fuelRequest.header(ApiConfig.Headers.DID_KEY, didKey)
        }
        fuelRequest.jsonBody(mGson!!.toJson(request))
            .response{result -> val (bytes, error) = result
                if (bytes != null) {
                    Log.i(TAG,"[response] ${String(bytes)}")
                    callback.onResponse(fromJson(String(bytes), clazz))
                }else{
                    callback.onError(error.toString())
                }}
    }

    fun post(
        url: String, request: BaseRequest?, pub_key: String?, didKey: String?,callback: Callback<String>
    ) {
        val fuelRequest = Fuel.post(url)
        if(pub_key !=null){
            fuelRequest.header(ApiConfig.Headers.PUB_KEY, pub_key)
        }
        if(didKey != null){
            fuelRequest.header(ApiConfig.Headers.DID_KEY, didKey)
        }
        fuelRequest.jsonBody(mGson!!.toJson(request))
            .response{result -> val (bytes, error) = result
                if (bytes != null) {
                    Log.i(TAG,"[response] ${String(bytes)}")
                    callback.onResponse(String(bytes))
                }else{
                    callback.onError(error.toString())
                }}
    }

    fun<T> get(
        url: String, request: BaseRequest?, pub_key: String?, didKey: String?, clazz: Class<T>,
        callback: Callback<T>
    ) {
        val final_url = StringBuilder(url)
        if (request != null) {
            val empMapType = object : TypeToken<Map<String?, String?>?>() {}.type
            val map: Map<String, String> = mGson!!.fromJson(
                mGson!!.toJson(request), empMapType
            )
            final_url.append("?")
            for (key in map.keys) {
                final_url.append(key).append("=").append(map[key]).append("&")
            }
            if (final_url.length > 0) {
                final_url.deleteCharAt(final_url.length - 1)
            }
        }
        val fuelRequest = Fuel.get(final_url.toString())
        if(pub_key !=null){
            fuelRequest.header(ApiConfig.Headers.PUB_KEY, pub_key)
        }
        if(didKey != null){
            fuelRequest.header(ApiConfig.Headers.DID_KEY, didKey)
        }
        fuelRequest.response{ result ->
                val (bytes, error) = result
                if (bytes != null) {
                    Log.i(TAG,"[response] ${String(bytes)}")
                    callback.onResponse(fromJson(String(bytes), clazz))
                }else{
                    callback.onError(error.toString())
                }
            }
    }

    fun get(
        url: String, request: BaseRequest?, pub_key: String?, didKey: String?,callback: Callback<String>
    ) {
        val final_url = StringBuilder(url)
        if (request != null) {
            val empMapType = object : TypeToken<Map<String?, String?>?>() {}.type
            val map: Map<String, String> = mGson!!.fromJson(
                mGson!!.toJson(request), empMapType
            )
            final_url.append("?")
            for (key in map.keys) {
                final_url.append(key).append("=").append(map[key]).append("&")
            }
            if (final_url.length > 0) {
                final_url.deleteCharAt(final_url.length - 1)
            }
        }
        val fuelRequest = Fuel.get(final_url.toString())
        if(pub_key !=null){
            fuelRequest.header(ApiConfig.Headers.PUB_KEY, pub_key)
        }
        if(didKey != null){
            fuelRequest.header(ApiConfig.Headers.DID_KEY, didKey)
        }
        fuelRequest.response{ result ->
            val (bytes, error) = result
            if (bytes != null) {
                Log.i(TAG,"[response] ${String(bytes)}")
                callback.onResponse(String(bytes))
            }else{
                callback.onError(error.toString())
            }
        }
    }

    fun <T> fromJson(json: String, type: Class<T>): T {
        val gson = Gson()
        return gson.fromJson(json, type)
    }

    interface Callback<T> {
        fun onResponse(response: T)
        fun onError(error: String)
    }
}