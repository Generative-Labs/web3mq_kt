package com.ty.web3mq.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.ty.web3mq.websocket.bean.ErrorResponse
import com.ty.web3mq.websocket.bean.SignRequest
import com.ty.web3mq.websocket.bean.SignSuccessResponse
import com.ty.web3mq.websocket.bean.sign.SignConversation
import com.ty.web3mq.websocket.bean.sign.Web3MQSession
import java.lang.reflect.Type

object DefaultSPHelper {
    private const val TAG = "DefaultSPHelper"
    private const val PREFERENCE_NAME = "web3_mq"
    private var mPreferences: SharedPreferences? = null
    private var gson: Gson? = null
    fun init(context: Context){
        mPreferences = context.getSharedPreferences(
            PREFERENCE_NAME,
            Context.MODE_PRIVATE
        )
        gson = GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create()
    }

    fun saveNodeID(node_id: String?) {
        put(Constant.SP_NODE_ID, node_id)
    }

    fun getNodeID(): String? {
        return getString(Constant.SP_NODE_ID, null)
    }

    fun saveMainPrivate(prv: String?) {
        put(Constant.SP_ED25519_MAIN_PRV, prv)
    }

    fun saveMainPublic(pub: String?) {
        put(Constant.SP_ED25519_MAIN_PUB, pub)
    }

    fun saveTempPrivate(prv: String?) {
        put(Constant.SP_ED25519_TEMP_PRV, prv)
    }

    fun saveTempPublic(pub: String?) {
        put(Constant.SP_ED25519_TEMP_PUB, pub)
    }

    fun saveUserID(userID: String?) {
        put(Constant.SP_USER_ID, userID)
    }

    fun saveDidKey(didKey: String?) {
        put(Constant.SP_DID_KEY, didKey)
    }

    fun getMainPrivate(): String? {
        return getString(Constant.SP_ED25519_MAIN_PRV)
    }

    fun getMainPublic(): String? {
        return getString(Constant.SP_ED25519_MAIN_PUB)
    }

    fun getTempPrivate(): String? {
        return getString(Constant.SP_ED25519_TEMP_PRV)
    }

    fun getTempPublic(): String? {
        return getString(Constant.SP_ED25519_TEMP_PUB)
    }

    fun getUserID(): String? {
        return getString(Constant.SP_USER_ID)
    }

    fun getDidKey(): String? {
        return getString(Constant.SP_DID_KEY)
    }


//    public MessagesBean getMessages(String chatId){
//        return (MessagesBean) getObject(chatId,MessagesBean.class);
//    }

    //    public MessagesBean getMessages(String chatId){
    //        return (MessagesBean) getObject(chatId,MessagesBean.class);
    //    }
    fun getObject(key: String?, type: Type?): Any? {
        val json_str = getString(key)
        return if (json_str != null) {
            gson!!.fromJson<Any>(json_str, type)
        } else {
            null
        }
    }

    /**
     * 将String信息存入Preferences
     */
    fun put(key: String?, value: String?): Boolean {
        // 存入数据
        val editor = mPreferences!!.edit()
        editor.putString(key, value)
        return editor.commit()
    }


    /**
     * 获取SharePreference中的String类型值
     */
    fun getString(key: String?): String? {
        // 获取数据
        return mPreferences!!.getString(key, "")
    }

    /**
     * 获取SharePreference中的String类型值
     */
    fun getString(key: String?, defValue: String?): String? {
        // 获取数据
        return mPreferences!!.getString(key, defValue)
    }


    /**
     * 将boolean信息存入Preferences
     */
    fun put(key: String?, value: Boolean): Boolean {
        // 存入数据
        val editor = mPreferences!!.edit()
        editor.putBoolean(key, value)
        return editor.commit()
    }

    /**
     * 获取SharePreference中的值
     */
    fun getBoolean(key: String?, defValue: Boolean): Boolean {
        // 获取数据
        return mPreferences!!.getBoolean(key, defValue)
    }

    /**
     * 将int信息存入Preferences
     */
    fun put(key: String?, value: Int): Boolean {
        // 存入数据
        val editor = mPreferences!!.edit()
        editor.putInt(key, value)
        return editor.commit()
    }

    fun put(key: String?, value: Any?): Boolean {
        // 存入数据
        val json = gson!!.toJson(value)
        val editor = mPreferences!!.edit()
        editor.putString(key, json)
        return editor.commit()
    }

    /**
     * 获取SharePreference中的int类型值
     */
    fun getInt(key: String?, defValue: Int): Int {
        // 获取数据
        return mPreferences!!.getInt(key, defValue)
    }

    fun put(key: String?, value: Long): Boolean {
        // 存入数据
        val editor = mPreferences!!.edit()
        editor.putLong(key, value)
        return editor.commit()
    }

    /**
     * 获取SharePreference中的int类型值
     */
    fun getLong(key: String?, defValue: Long?): Long? {
        // 获取数据
        return mPreferences!!.getLong(key, defValue!!)
    }

    /**
     * 将float信息存入Preferences
     */
    fun put(key: String?, value: Float): Boolean {
        // 存入数据
        val editor = mPreferences!!.edit()
        editor.putFloat(key, value)
        return editor.commit()
    }

    /**
     * 获取SharePreference中的值
     */
    fun getFloat(key: String?, defValue: Float): Float {
        // 获取数据
        return mPreferences!!.getFloat(key, defValue)
    }

    fun put(key: String?, value: ArrayList<String?>?): Boolean {
        val editor = mPreferences!!.edit()
        val json = gson!!.toJson(value)
        editor.putString(key, json)
        return editor.commit()
    }


    /**
     * delete in preferences value
     */
    fun remove(key: String?): Boolean {
        // 存入数据
        val editor = mPreferences!!.edit()
        editor.remove(key)
        return editor.commit()
    }

    fun appendSession(session: Web3MQSession) {
        var sessionList: ArrayList<Web3MQSession>? = getSessionList()
        if (sessionList == null) {
            sessionList = ArrayList<Web3MQSession>()
        }
        sessionList.add(session)
        saveSessionList(sessionList)
    }

    fun removeSession(sessionID: String) {
        val sessionList: ArrayList<Web3MQSession> = getSessionList() ?: return
        for (i in sessionList.indices) {
            val session: Web3MQSession = sessionList[i]
            if (session.peerTopic.equals(sessionID)) {
                sessionList.removeAt(i)
                if (sessionList.size > 0) {
                    saveSessionList(sessionList)
                }
                break
            }
        }
    }

    fun getSession(sessionID: String?): Web3MQSession? {
        val sessionList: ArrayList<Web3MQSession> = getSessionList() ?: return null
        for (session in sessionList) {
            if (session.peerTopic.equals(sessionID)) {
                return session
            }
        }
        return null
    }

    fun updateSession(sessionID: String, newSession: Web3MQSession) {
//        Log.i(TAG,"updateSession sessionID:"+sessionID+" newSession:"+gson.toJson(newSession));
        val sessionList: ArrayList<Web3MQSession> = getSessionList() ?: return
        for (i in sessionList.indices) {
            val session: Web3MQSession = sessionList[i]
            if (session.peerTopic.equals(sessionID)) {
                sessionList[i] = newSession
                break
            }
        }
        saveSessionList(sessionList)
    }

    private fun saveSessionList(sessionList: ArrayList<Web3MQSession>?) {
        put("sessionList", gson!!.toJson(sessionList))
    }

    fun getSessionList(): ArrayList<Web3MQSession>? {
        val json = mPreferences!!.getString("sessionList", null)
        return if (json != null) {
            val type = object : TypeToken<ArrayList<Web3MQSession>?>() {}.type
            gson!!.fromJson<ArrayList<Web3MQSession>>(json, type)
        } else {
            null
        }
    }

    fun appendSignRequest(sessionID: String, id: String, signRequest: SignRequest) {
//        Log.i(TAG,"appendSignRequest sessionID:"+sessionID+" id:"+id);
        val web3MQSession = getSession(sessionID) ?: return
        if (web3MQSession.signConversationMap == null) {
            web3MQSession.signConversationMap = HashMap()
        }
        val conversation = SignConversation(id, signRequest, null, null)
        web3MQSession.signConversationMap!![id] = conversation
        updateSession(sessionID, web3MQSession)
    }


    fun appendSignSuccessResponse(
        sessionID: String,
        id: String,
        successResponse: SignSuccessResponse
    ) {
        val web3MQSession = getSession(sessionID) ?: return
        if (web3MQSession.signConversationMap == null) {
            return
        }
        val conversation: SignConversation = web3MQSession.signConversationMap!!.get(id) ?: return
        conversation.successResponse = successResponse
        web3MQSession.signConversationMap!!.put(id, conversation)
        updateSession(sessionID, web3MQSession)
    }

    fun appendSignErrorResponse(sessionID: String, id: String, errorResponse: ErrorResponse?) {
        val web3MQSession = getSession(sessionID) ?: return
        if (web3MQSession.signConversationMap == null) {
            return
        }
        val conversation: SignConversation = web3MQSession.signConversationMap!!.get(id) ?: return
        conversation.errorResponse = errorResponse
        web3MQSession.signConversationMap!!.put(id, conversation)
        updateSession(sessionID, web3MQSession)
    }

    fun showSessionInfo() {
        Log.i(TAG, "----------showSessionInfo--------")
        val sessionList: ArrayList<Web3MQSession>? = getSessionList()
        if (sessionList == null) {
            Log.i(TAG, "session list is null")
        } else {
            for (session in sessionList) {
                Log.i(TAG, "session : " + gson!!.toJson(session) + "")
            }
        }
    }

    /**
     * Mark in the editor to remove all data from the preferences.
     */
    fun clear(): Boolean {
        // 存入数据
        val editor = mPreferences!!.edit()
        editor.clear()
        return editor.commit()
    }
}