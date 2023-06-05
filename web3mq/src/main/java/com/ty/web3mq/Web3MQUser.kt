package com.ty.web3mq

import android.text.TextUtils
import android.util.Base64
import android.util.Log
import com.ty.web3mq.http.ApiConfig
import com.ty.web3mq.http.HttpManager
import com.ty.web3mq.http.request.*
import com.ty.web3mq.http.response.*
import com.ty.web3mq.interfaces.*
import com.ty.web3mq.utils.*
import org.bouncycastle.jcajce.provider.digest.SHA3
import java.net.URLEncoder
import java.security.MessageDigest
import java.util.*

object Web3MQUser {
    private const val TAG = "Web3MQUser"
    private var salt = ""
    private var expiredTime = (24 * 60 * 60 * 1000).toLong()
    fun setSalt(salt: String) {
        this.salt = salt
    }

    fun getKeyGenerateSignContent(
        wallet_address: String,
        magic_str: String
    ): String {
        return """
               Signing this message will allow this app to decrypt messages in the Web3MQ protocol for the following address: $wallet_address. This won’t cost you anything.
               
               If your Web3MQ wallet-associated password and this signature is exposed to any malicious app, this would result in exposure of Web3MQ account access and encryption keys, and the attacker would be able to read your messages.
               
               In the event of such an incident, don’t panic. You can call Web3MQ’s key revoke API and service to revoke access to the exposed encryption key and generate a new one!
               
               Nonce: $magic_str
               """.trimIndent()
    }

    fun getRegisterSignContent(
        wallet_name: String,
        wallet_address: String,
        your_domain_url: String,
        nonce_content: String
    ): String {
        val str_date: String = CommonUtils.date
        return """Web3MQ wants you to sign in with your $wallet_name account: 
$wallet_address 
For Web3MQ registration 
URI: $your_domain_url 
Version: 1 
Nonce: $nonce_content 
Issued At: $str_date"""
    }

    @Deprecated("")
    fun registerSign(
        wallet_prv_key: String?,
        wallet_type_name: String,
        wallet_address: String,
        your_domain_url: String,
        nonce_content: String
    ): Array<String> {
        val str_date: String = CommonUtils.date
        val signature_content = """Web3MQ wants you to sign in with your $wallet_type_name account: 
$wallet_address 
For Web3MQ registration 
URI: $your_domain_url 
Version: 1 
Nonce: $nonce_content 
Issued At: $str_date"""
        return arrayOf(
            signature_content,
            CryptoUtils.signMessage(wallet_prv_key, signature_content)
        )
    }

    fun resetPwdSign(
        wallet_prv_key: String?,
        wallet_type_name: String,
        wallet_address: String,
        your_domain_url: String,
        nonce_content: String
    ): Array<String> {
        val str_date: String = CommonUtils.date
        val signature_content = """Web3MQ wants you to sign in with your $wallet_type_name account: 
$wallet_address 
For Web3MQ reset password 
URI: $your_domain_url 
Version: 1 
Nonce: $nonce_content 
Issued At: $str_date"""
        return arrayOf(
            signature_content,
            CryptoUtils.signMessage(wallet_prv_key, signature_content)
        )
    }

    /**
     * 注册流程：
     * 1.生成magicString sha3_224(user_id+wallet_type+wallet_address+password)
     * 2.对magicString 签名
     * 3.使用这个签名作为seed生成prv_key
     * 4.
     */
    fun generateMagicString(
        wallet_type: String,
        wallet_address: String,
        password: String
    ): String {
        val keyIndex = 1
        val md: MessageDigest =
            SHA3.Digest224()
        val magicString =
            "web3mq" + wallet_type + ":" + wallet_address + keyIndex + password + "web3mq"
        val messageDigest = md.digest(magicString.toByteArray())
        return Base64.encodeToString(messageDigest, Base64.NO_WRAP)
    }

    //    public String[] generateKeyPair(String keyGenerateSign){
    //        KeyPair keyPair = Ed25519.ed25519GenerateKeyPair(keyGenerateSign);
    //        EdDSAPrivateKey pv = (EdDSAPrivateKey) keyPair.getPrivate();
    //        String prv_key = Ed25519.bytesToHexString(keyPair.getPrivate().getEncoded());
    //        String pub_key = Ed25519.bytesToHexString( keyPair.getPrivate());
    //        return new String[]{prv_key, pub_key};
    //    }
    fun generateUserID(
        wallet_type: String,
        wallet_address: String
    ): String {
        return UserIDGenerator.generateUserID(wallet_type, wallet_address)
    }

    fun signUp(
        user_id: String,
        wallet_type: String,
        wallet_address: String,
        mainPrivateKeyHex: String,
        registerSignatureContent: String,
        registerSign: String,
        timestamp: Long,
        callback: SignupCallback
    ) {
        val pub_key: String = Ed25519.generatePublicKey(mainPrivateKeyHex)
        val did_value = wallet_address.lowercase(Locale.getDefault())
        val pubkey_type = "ed25519"
        val testnet_access_key: String = Web3MQClient.getApiKey()!!
        val request = UserRegisterRequest()
        request.userid = user_id
        request.did_type = wallet_type
        request.did_value = did_value
        request.timestamp = timestamp
        request.did_signature = registerSign
        request.signature_content = registerSignatureContent
        request.pubkey_type = pubkey_type
        request.pubkey_value = pub_key
        request.testnet_access_key = testnet_access_key
        HttpManager.post(
            ApiConfig.USER_REGISTER,
            request,null,null,
            RegisterResponse::class.java,
            object : HttpManager.Callback<RegisterResponse>{
                override fun onResponse(response: RegisterResponse) {
                    val code: Int = response.code
                    Log.i("register", "code:" + code + " msg:" + response.msg)
                    if (code == 0) {
                        callback.onSuccess()
                    } else {
                        callback.onFail("error code:$code")
                    }
                }

                override fun onError(error: String) {
                    Log.i("register", "error:$error")
                    callback.onFail(error)
                }
            })
    }

    fun login(
        user_id: String,
        wallet_type: String,
        wallet_address: String,
        mainPrivateKey: String?,
        main_pubkey: String,
        callback: LoginCallback
    ) {
        try {
            val request = UserLoginRequest()
            request.userid = user_id
            request.did_type = wallet_type
            request.did_value = wallet_address
            request.timestamp = System.currentTimeMillis()
            request.pubkey_type = "ed25519"
            val keyPair: Array<String> = Ed25519.generateKeyPair()
            request.pubkey_value = keyPair[1]
            val temporary_prv = keyPair[0]
            val temporary_pub: String = request.pubkey_value!!
            request.pubkey_expired_timestamp = request.timestamp + expiredTime
            request.signature_content =
                CryptoUtils.SHA3_ENCODE(user_id + request.pubkey_value + request.pubkey_expired_timestamp + request.timestamp)
            request.main_pubkey = main_pubkey
            request.login_signature =
                Ed25519.ed25519Sign(mainPrivateKey, request.signature_content!!.toByteArray())
            Log.i(TAG, "login_signature:" + request.login_signature)
            HttpManager.post(
                ApiConfig.USER_LOGIN,
                request,
                null,
                null,
                LoginResponse::class.java,
                object : HttpManager.Callback<LoginResponse> {
                    override fun onResponse(response: LoginResponse) {
                        val code: Int = response.code
                        if (code == 0) {
                            DefaultSPHelper.saveMainPrivate(mainPrivateKey)
                            DefaultSPHelper.saveMainPublic(main_pubkey)
                            DefaultSPHelper.saveTempPrivate(temporary_prv)
                            DefaultSPHelper.saveTempPublic(temporary_pub)
                            DefaultSPHelper.saveUserID(user_id)
                            val did_key = "$wallet_type:$wallet_address"
                            DefaultSPHelper.saveDidKey(did_key)
                            callback.onSuccess()
                        } else {
                            callback.onFail("error code:$code")
                        }
                    }

                    override fun onError(error: String) {
                        callback.onFail(error)
                    }
                })
        } catch (e: Exception) {
            e.printStackTrace()
            callback.onFail("ed25519 sign error")
        }
    }

    fun resetPwd(
        user_id: String,
        wallet_type: String,
        wallet_address: String,
        mainPrivateKeyHex: String,
        resetPwdSignatureContent: String,
        resetPwdSign: String,
        timestamp: Long,
        callback: ResetPwdCallback
    ) {
        val pub_key: String = Ed25519.generatePublicKey(mainPrivateKeyHex)
        val did_value = wallet_address.lowercase(Locale.getDefault())
        val pubkey_type = "ed25519"
        val testnet_access_key: String = Web3MQClient.getApiKey()!!
        val request = UserRegisterRequest()
        request.userid = user_id
        request.did_type = wallet_type
        request.did_value = did_value
        request.timestamp = timestamp
        request.did_signature = resetPwdSign
        request.signature_content = resetPwdSignatureContent
        request.pubkey_type = pubkey_type
        request.pubkey_value = pub_key
        request.testnet_access_key = testnet_access_key
        HttpManager.post(
            ApiConfig.USER_RESET_PWD,
            request,
            null,
            null,
            RegisterResponse::class.java,
            object : HttpManager.Callback<RegisterResponse> {
                override fun onResponse(response: RegisterResponse) {
                    val code: Int = response.code
                    Log.i("register", "code:" + code + " msg:" + response.msg)
                    if (code == 0) {
                        callback.onSuccess()
                    } else {
                        callback.onFail("error code:$code")
                    }
                }

                override fun onError(error: String) {
                    Log.i("register", "error:$error")
                    callback.onFail(error)
                }
            })
    }

    fun setPubKeyExpiredTime(expiredTime: Long) {
        this.expiredTime = expiredTime
    }

    fun hasLogged(): Boolean {
        val mainPrivate: String? = DefaultSPHelper.getTempPrivate()
        return !TextUtils.isEmpty(mainPrivate)
    }

    fun getMyProfile(callback: GetMyProfileCallback) {
        try {
            val pub_key: String = DefaultSPHelper.getTempPublic()!!
            val prv_key_seed: String = DefaultSPHelper.getTempPrivate()!!
            val did_key: String = DefaultSPHelper.getDidKey()!!
            val request = GetMyProfileRequest()
            request.userid = DefaultSPHelper.getUserID()
            request.timestamp = System.currentTimeMillis()
            request.web3mq_signature = URLEncoder.encode(
                Ed25519.ed25519Sign(
                    prv_key_seed,
                    (request.userid + request.timestamp).toByteArray()
                )
            )
            HttpManager.get(
                ApiConfig.GET_MY_PROFILE,
                request,
                pub_key,
                did_key,
                ProfileResponse::class.java,
                object : HttpManager.Callback<ProfileResponse> {
                    override fun onResponse(response: ProfileResponse) {
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

    fun getPublicProfile(target_userid: String, callback: GetPublicProfileCallback) {
        val pub_key: String = DefaultSPHelper.getTempPublic()!!
        val prv_key_seed: String = DefaultSPHelper.getTempPrivate()!!
        val did_key: String = DefaultSPHelper.getDidKey()!!
        val request = GetPublicProfileRequest()
        request.my_userid = DefaultSPHelper.getUserID()
        request.timestamp = System.currentTimeMillis()
        request.did_type = "web3mq"
        request.did_value = target_userid
        HttpManager.get(
            ApiConfig.GET_USER_PUBLIC_PROFILE,
            request,
            pub_key,
            did_key,
            ProfileResponse::class.java,
            object : HttpManager.Callback<ProfileResponse>{
                override fun onResponse(response: ProfileResponse) {
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
    }

    fun postMyProfile(nickname: String, avatar_url: String, callback: PostMyProfileCallback) {
        try {
            val pub_key: String = DefaultSPHelper.getTempPublic()!!
            val prv_key_seed: String = DefaultSPHelper.getTempPrivate()!!
            val did_key: String = DefaultSPHelper.getDidKey()!!
            val request = PostMyProfileRequest()
            request.userid = DefaultSPHelper.getUserID()
            request.timestamp = System.currentTimeMillis()
            request.nickname = nickname
            request.avatar_url = avatar_url
            request.web3mq_signature =
                Ed25519.ed25519Sign(prv_key_seed, (request.userid + request.timestamp).toByteArray())
            HttpManager.post(
                ApiConfig.POST_MY_PROFILE,
                request,
                pub_key,
                did_key,
                ProfileResponse::class.java,
                object : HttpManager.Callback<ProfileResponse> {
                    override fun onResponse(response: ProfileResponse) {
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

    val myUserId: String?
        get() = DefaultSPHelper.getUserID()

    fun getUserInfo(did_type: String, did_value: String, callback: GetUserinfoCallback) {
        try {
            val request = GetUserInfoRequest()
            request.timestamp = System.currentTimeMillis()
            request.did_type = did_type
            request.did_value = did_value
            HttpManager.post(
                ApiConfig.GET_USER_INFO,
                request,
                null,
                null,
                UserInfoResponse::class.java,
                object : HttpManager.Callback<UserInfoResponse>{
                    override fun onResponse(response: UserInfoResponse) {
                        if (response.code == 0) {
                            callback.onSuccess(response.data!!)
                        } else if (response.code == 404) {
                            callback.onUserNotRegister()
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

    fun searchUsers(keyword: String, callback: SearchUsersCallback) {
        try {
            val pub_key: String = DefaultSPHelper.getTempPublic()!!
            val prv_key_seed: String = DefaultSPHelper.getTempPrivate()!!
            val did_key: String = DefaultSPHelper.getDidKey()!!
            val request = SearchUsersRequest()
            request.userid = DefaultSPHelper.getUserID()
            request.timestamp = System.currentTimeMillis()
            request.keyword = keyword
            request.web3mq_signature = URLEncoder.encode(
                Ed25519.ed25519Sign(
                    prv_key_seed,
                    (request.userid + request.keyword + request.timestamp).toByteArray()
                )
            )
            HttpManager.get(
                ApiConfig.SEARCH_USERS,
                request,
                pub_key,
                did_key,
                SearchUsersResponse::class.java,
                object : HttpManager.Callback<SearchUsersResponse> {
                    override fun onResponse(response: SearchUsersResponse) {
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

}