package com.ty.web3mq

import android.os.CountDownTimer
import android.util.Base64
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.protobuf.ByteString
import com.goterl.lazysodium.LazySodiumAndroid
import com.goterl.lazysodium.SodiumAndroid
import com.goterl.lazysodium.exceptions.SodiumException
import com.goterl.lazysodium.interfaces.Sign
import com.ty.web3mq.interfaces.*
import com.ty.web3mq.utils.*
import com.ty.web3mq.websocket.MessageManager
import com.ty.web3mq.websocket.bean.*
import com.ty.web3mq.websocket.bean.sign.Participant
import com.ty.web3mq.websocket.bean.sign.Web3MQSession
import org.bouncycastle.crypto.agreement.X25519Agreement
import org.bouncycastle.crypto.digests.SHA384Digest
import org.bouncycastle.crypto.generators.HKDFBytesGenerator
import org.bouncycastle.crypto.params.HKDFParameters
import org.bouncycastle.crypto.params.X25519PrivateKeyParameters
import org.bouncycastle.crypto.params.X25519PublicKeyParameters
import org.bouncycastle.jcajce.provider.digest.SHA3
import web3mq.Message
import java.math.BigInteger
import java.net.URLEncoder
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

object Web3MQSign {
    private val gson: Gson
    private var onSignRequestMessageCallback: OnSignRequestMessageCallback? = null
    private var onConnectResponseCallback: OnConnectResponseCallback? = null
    private var onSignResponseMessageCallback: OnSignResponseMessageCallback? = null
    private val aNative: Sign.Native
    private var currentSession: Web3MQSession
    private var dAppID: String? = null
    private const val TAG = "Web3MQSign"

    //session有效期
    private val session_valid_duration = (1000 * 60 * 60 * 24).toLong()

    //request有效期
    private val request_valid_duration = (1000 * 60 * 60 * 3).toLong()

    init {
        gson = GsonBuilder().disableHtmlEscaping().create()
        val lazySodium = LazySodiumAndroid(SodiumAndroid())
        aNative = lazySodium
        currentSession = Web3MQSession()
        currentSession.selfParticipant = Participant()
        currentSession.peerParticipant = Participant()
    }

    fun init(dAppID: String, callback: BridgeConnectCallback?) {
        if (Web3MQClient.getNodeId() == null || !Web3MQClient.getSocketClient()!!.isOpen()
        ) {
            callback!!.onError("websocket not connect")
            return
        }
        this.dAppID = dAppID
        generate25519KeyPair()
        MessageManager.setBridgeConnectCallback(callback)
        currentSession.selfTopic = "bridge:" + CryptoUtils.SHA1_ENCODE(
            dAppID + "@" + Base64.encodeToString(
                CryptoUtils.hexStringToBytes(currentSession.selfParticipant!!.ed25519Pubkey),
                Base64.NO_WRAP
            )
        )
        Web3MQClient.sendBridgeConnectCommand(dAppID, currentSession.selfTopic)
        MessageManager.setBridgeMessageCallback(object : BridgeMessageCallback{
            override fun onBridgeMessage(comeFrom: String, publicKey: String, content: String?) {
                currentSession.peerParticipant!!.ed25519Pubkey = publicKey
                val hex_prv: String? = currentSession.selfParticipant!!.ed25519PrvKey
                Log.i(TAG, "hex_prv:$hex_prv")
                val json_content = String(
                    decryptionContent(
                        Ed25519.hexStringToBytes(publicKey),
                        CryptoUtils.hexStringToBytes(currentSession.selfParticipant!!.ed25519PrvKey),
                        Base64.decode(content, Base64.NO_WRAP)
                    )!!
                )
                val bridgeMessageContent: BridgeMessageContent =
                    ConvertUtil.convertJsonToBridgeMessageContent(json_content, gson)
                when (bridgeMessageContent.type) {
                    BridgeMessageContent.TYPE_CONNECT_SUCCESS_RESPONSE -> {
                        currentSession.peerTopic = comeFrom
                        val connectSuccessResponse: ConnectSuccessResponse =
                            bridgeMessageContent.content as ConnectSuccessResponse
                        DefaultSPHelper.appendSession(currentSession)
                        // TODO connect request是否要加入到session request中？
                        if (onConnectResponseCallback != null) {
                            onConnectResponseCallback!!.onApprove(
                                connectSuccessResponse.result!!.metadata!!,
                                connectSuccessResponse.getETHAddress()!!
                            )
                        }
                    }
                    BridgeMessageContent.TYPE_CONNECT_ERROR_RESPONSE -> if (onConnectResponseCallback != null) {
                        onConnectResponseCallback!!.onReject()
                    }
                    BridgeMessageContent.TYPE_SIGN_REQUEST -> {
                        val session: Web3MQSession =
                            DefaultSPHelper.getSession(currentSession.peerTopic)!!
                        val signRequest: SignRequest = bridgeMessageContent.content as SignRequest
                        DefaultSPHelper.appendSignRequest(session.peerTopic!!, signRequest.id!!, signRequest)
                        if (onSignRequestMessageCallback != null) {
                            onSignRequestMessageCallback!!.onSignRequestMessage(
                                signRequest.id!!,
                                currentSession.peerParticipant,
                                signRequest.getAddress()!!,
                                signRequest.getSignRaw()!!
                            )
                        }
                    }
                    BridgeMessageContent.TYPE_SIGN_SUCCESS_RESPONSE -> {
                        val signSuccessResponse: SignSuccessResponse =
                            bridgeMessageContent.content as SignSuccessResponse
                        DefaultSPHelper.appendSignSuccessResponse(
                            currentSession.peerTopic!!,
                            signSuccessResponse.id!!,
                            signSuccessResponse
                        )
                        if (onSignResponseMessageCallback != null) {
                            onSignResponseMessageCallback!!.onApprove(signSuccessResponse.result!!)
                        }
                    }
                    BridgeMessageContent.TYPE_SIGN_ERROR_RESPONSE -> {
                        val errorResponse: ErrorResponse =
                            bridgeMessageContent.content as ErrorResponse
                        DefaultSPHelper.appendSignErrorResponse(
                            currentSession.peerTopic!!,
                            errorResponse.id!!,
                            errorResponse
                        )
                        if (onSignResponseMessageCallback != null) {
                            onSignResponseMessageCallback!!.onReject()
                        }
                    }
                }
            }

        })
    }

    fun getSessionList(): ArrayList<Web3MQSession>? {
        return DefaultSPHelper.getSessionList()
    }

    fun switchSession(dAppID: String, session: Web3MQSession) {
        this.dAppID = dAppID
        currentSession = session
        Web3MQClient.sendBridgeConnectCommand(dAppID, currentSession.selfTopic)
    }

    fun reconnect() {
        Web3MQClient.sendBridgeConnectCommand(dAppID, currentSession.selfTopic)
    }

    fun checkPendingRequest(web3MQSession: Web3MQSession): SignRequest? {
        if (web3MQSession.signConversationMap == null) {
            return null
        }
        web3MQSession.signConversationMap!!.forEach { (key, value) ->
            if (value.successResponse == null && value.errorResponse == null) {
                return value.request
            }
        }
        return null
    }

    fun getLastSession(): Web3MQSession? {
        val sessionList: ArrayList<Web3MQSession>? = DefaultSPHelper.getSessionList()
        return if (sessionList == null || sessionList.size == 0) {
            null
        } else sessionList[0]
    }

    fun getCurrentSession(): Web3MQSession {
        return currentSession
    }

    fun initialized(): Boolean {
        return currentSession.selfTopic != null && currentSession.selfParticipant!!.ed25519Pubkey != null && currentSession.selfParticipant!!.ed25519PrvKey != null
    }

    fun setTargetTopicID(topicID: String) {
        currentSession.peerTopic = topicID
        Log.i(TAG, "setTargetTopicID:$topicID")
    }

    fun generateConnectDeepLink(icon_url: String?, website: String?, redirect: String?): String? {
        if (currentSession.selfTopic == null || currentSession.selfParticipant == null) {
            Log.e(TAG, "Web3MQSign not init")
            return null
        }
        val request = ConnectRequest()
        request.topic = URLEncoder.encode(currentSession.selfTopic)
        request.id = System.currentTimeMillis().toString() + "" + RandomUtils.random4Number()
        request.jsonrpc = "2.0"
        request.name = ""
        request.description = ""
        if (icon_url != null) {
            val icons: ArrayList<String> = ArrayList()
            icons.add(URLEncoder.encode(icon_url))
            request.icons = icons
        }
        request.redirect = URLEncoder.encode(redirect)
        request.url = URLEncoder.encode(website)
        request.method = "provider_authorization"
        request.publicKey = URLEncoder.encode(currentSession.selfParticipant!!.ed25519Pubkey)
        request.expiry =
            DateUtils.getISOOffsetTime(System.currentTimeMillis() + request_valid_duration)
        return ConvertUtil.convertConnectRequestToDeepLink(request)
    }

    fun generateSignDeepLink(): String? {
        if (currentSession.selfTopic == null || currentSession.selfParticipant == null) {
            Log.e(TAG, "Web3MQSign not init")
            return null
        }
        val deepLink = "web3mq://?action=sign"
        Log.i(TAG, "generateDeepLink:$deepLink")
        return deepLink
    }

    fun setTargetPubKey(ed25519Pubkey: String) {
        currentSession.peerParticipant!!.ed25519Pubkey = ed25519Pubkey
        Log.i(TAG, "updateTargetPubKey:$ed25519Pubkey")
    }

    //dapp TODO
    fun sendSignRequest(
        signRaw: String,
        address: String,
        needStore: Boolean,
        callback: SendBridgeMessageCallback?
    ) {
        if (currentSession.peerParticipant!!.ed25519Pubkey == null) {
            Log.e(TAG, "targetPubKey is null")
            return
        }
        val message = BridgeMessage()
        val request = SignRequest()
        request.id = System.currentTimeMillis().toString() + "" + RandomUtils.random4Number()
        request.jsonrpc = "2.0"
        request.method = "personal_sign"
        request.params = ArrayList<String>()
        request.params!!.add(signRaw)
        request.params!!.add(address)
        val json_content: String = gson.toJson(request)
        message.content = Base64.encodeToString(
            encryptionContent(
                Ed25519.hexStringToBytes(currentSession.peerParticipant!!.ed25519Pubkey)!!,
                Ed25519.hexStringToBytes(currentSession.selfParticipant!!.ed25519PrvKey)!!,
                json_content.toByteArray()
            ), Base64.NO_WRAP
        )
        message.publicKey = currentSession.selfParticipant!!.ed25519Pubkey
        sendBridgeMessage(message, needStore, callback)
        DefaultSPHelper.appendSignRequest(currentSession.peerTopic!!, request.id!!, request)
    }

    //wallet
    fun sendConnectResponse(
        id: String,
        address: String,
        approve: Boolean,
        walletInfo: BridgeMessageMetadata?,
        needStore: Boolean,
        callback: SendBridgeMessageCallback?
    ) {
        val message = BridgeMessage()
        val json_content: String
        if (approve) {
            val connectSuccessResponse = ConnectSuccessResponse()
            connectSuccessResponse.id = id
            connectSuccessResponse.jsonrpc = "2.0"
            connectSuccessResponse.method = "provider_authorization"
            connectSuccessResponse.result = AuthorizationResponseSuccessData()
            connectSuccessResponse.result!!.metadata = walletInfo
            connectSuccessResponse.result!!.sessionNamespaces = HashMap<String, Namespaces>()
            val namespaces = Namespaces()
            namespaces.chains = ArrayList<String>()
            namespaces.chains!!.add("eip155:1")
            namespaces.events = ArrayList<String>()
            namespaces.events!!.add("personal_sign")
            namespaces.methods = ArrayList<String>()
            namespaces.accounts = ArrayList<String>()
            namespaces.accounts!!.add("eip155:1:$address")
            connectSuccessResponse.result!!.sessionNamespaces!!["eip155"] = namespaces
            json_content = gson.toJson(connectSuccessResponse)
        } else {
            val errorResponse = ErrorResponse()
            errorResponse.id = id
            errorResponse.jsonrpc = "2.0"
            errorResponse.method = "provider_authorization"
            errorResponse.error = ResponseErrorData()
            errorResponse.error!!.code = 5001
            errorResponse.error!!.message = "User disapproved requested methods"
            json_content = gson.toJson(errorResponse)
        }
        Log.i(TAG, "targetPubKey:" + currentSession.peerParticipant!!.ed25519Pubkey.toString() + "")
        message.content = Base64.encodeToString(
            encryptionContent(
                Ed25519.hexStringToBytes(currentSession.peerParticipant!!.ed25519Pubkey),
                Ed25519.hexStringToBytes(currentSession.selfParticipant!!.ed25519PrvKey),
                json_content.toByteArray()
            ), Base64.NO_WRAP
        )
        message.publicKey = currentSession.selfParticipant!!.ed25519Pubkey
        Log.i(TAG, "send pubKy:" + message.publicKey)
        sendBridgeMessage(message, needStore, callback)
    }

    //wallet
    fun sendSignResponse(
        id: String,
        approve: Boolean,
        signature: String?,
        needStore: Boolean,
        callback: SendBridgeMessageCallback?
    ) {
        Log.i(TAG, "sendSignResponse approve:$approve")
        Log.i(TAG, "sendSignResponse signature:$signature")
        val message = BridgeMessage()
        val json_content: String
        if (approve) {
            val response = SignSuccessResponse()
            response.id = id
            response.jsonrpc = "2.0"
            response.method = "personal_sign"
            response.result = signature
            json_content = gson.toJson(response)
            DefaultSPHelper.appendSignSuccessResponse(currentSession.peerTopic!!, id, response)
        } else {
            val errorResponse = ErrorResponse()
            errorResponse.id = id
            errorResponse.jsonrpc = "2.0"
            errorResponse.method = "personal_sign"
            val errorData = ResponseErrorData()
            errorData.code = 5001
            errorData.message = "User disapproved requested methods"
            errorResponse.error = errorData
            json_content = gson.toJson(errorResponse)
            DefaultSPHelper.appendSignErrorResponse(currentSession.peerTopic!!, id, errorResponse)
        }
        message.content = Base64.encodeToString(
            encryptionContent(
                CryptoUtils.hexStringToBytes(currentSession.peerParticipant!!.ed25519Pubkey)!!,
                CryptoUtils.hexStringToBytes(currentSession.selfParticipant!!.ed25519PrvKey)!!,
                json_content.toByteArray()
            ), Base64.NO_WRAP
        )
        message.publicKey = currentSession.selfParticipant!!.ed25519Pubkey
        sendBridgeMessage(message, needStore, callback)
    }

    fun setOnSignRequestMessageCallback(onSignRequestMessageCallback: OnSignRequestMessageCallback?) {
        this.onSignRequestMessageCallback = onSignRequestMessageCallback
    }

    fun setOnSignResponseMessageCallback(onSignResponseMessageCallback: OnSignResponseMessageCallback?) {
        this.onSignResponseMessageCallback = onSignResponseMessageCallback
    }

    fun setOnConnectResponseCallback(callback: OnConnectResponseCallback?) {
        onConnectResponseCallback = callback
    }

    private fun sendBridgeMessage(
        message: BridgeMessage,
        needStore: Boolean,
        callback: SendBridgeMessageCallback?
    ) {
        if (Web3MQClient.getNodeId() == null || !Web3MQClient.getSocketClient()!!.isOpen
        ) {
            Log.e(TAG, "websocket not connect")
            return
        }
        Log.i(TAG, "-----sendBridgeMessage-----")
        try {
            val timestamp = System.currentTimeMillis()
            val node_id: String = Web3MQClient.getNodeId()!!
            val msg_str: String = gson.toJson(message)
            Log.i(TAG, "BridgeMessage: $msg_str")
            val msg_id = GenerateMessageID(
                currentSession.selfTopic!!,
                currentSession.peerTopic!!,
                timestamp,
                msg_str.toByteArray()
            )
            Log.i(TAG, "msg_id:$msg_id")
            val signContent =
                msg_id + currentSession.selfTopic + currentSession.peerTopic.toString() + node_id + timestamp
            Log.i(TAG, "signContent:$signContent")
            val sign: String = Ed25519.ed25519Sign(
                currentSession.selfParticipant!!.ed25519PrvKey,
                signContent.toByteArray()
            )
            val builder: Message.Web3MQMessage.Builder = Message.Web3MQMessage.newBuilder()
            builder.setNodeId(node_id)
            Log.i(TAG, "NodeId:$node_id")
            builder.setCipherSuite("NONE")
            Log.i(TAG, "CipherSuite:" + "NONE")
            builder.setPayloadType("application/json")
            Log.i(TAG, "PayloadType:" + "application/json")
            builder.setFromSign(sign)
            Log.i(TAG, "FromSign:$sign")
            builder.setTimestamp(timestamp)
            Log.i(TAG, "timestamp:$timestamp")
            builder.setMessageId(msg_id)
            Log.i(TAG, "MessageId:$msg_id")
            builder.setVersion(1)
            Log.i(TAG, "Version:" + 1)
            builder.setComeFrom(currentSession.selfTopic)
            Log.i(TAG, "ComeFrom:" + currentSession.selfTopic)
            builder.setContentTopic(currentSession.peerTopic)
            Log.i(TAG, "ContentTopic:" + currentSession.peerTopic)
            builder.setNeedStore(needStore)
            Log.i(TAG, "NeedStore:$needStore")
            builder.setPayload(ByteString.copyFrom(msg_str.toByteArray()))
            Log.i(TAG, "Payload:$msg_str")
            val base64PubKey = Base64.encodeToString(
                CryptoUtils.hexStringToBytes(currentSession.selfParticipant!!.ed25519Pubkey),
                Base64.NO_WRAP
            )
            builder.setValidatePubKey(base64PubKey)
            Log.i(TAG, "ValidatePubKey:$base64PubKey")
            builder.setMessageType("Web3MQ/bridge")
            Log.i(TAG, "MessageType:" + "Web3MQ/bridge")
            val sendMessageBytes: ByteArray = CommonUtils.appendPrefix(
                WebsocketConfig.category,
                WebsocketConfig.PbTypeMessage,
                builder.build().toByteArray()
            )
            Web3MQClient.getSocketClient()!!.send(sendMessageBytes)
            if (callback!=null) {
                MessageManager.addSendBridgeMessageCallback(msg_id, callback)
            }
            object : CountDownTimer(request_valid_duration, request_valid_duration) {
                override fun onTick(millisUntilFinished: Long) {}
                override fun onFinish() {
                    if (callback != null) {
                        callback.onTimeout()
                        MessageManager.removeSendBridgeMessageCallback(msg_id)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun GenerateMessageID(
        user_id: String,
        topic: String,
        timestamp: Long,
        payload: ByteArray
    ): String {
        val md: MessageDigest = SHA3.Digest224()
        md.update(user_id.toByteArray())
        md.update(topic.toByteArray())
        md.update(("" + timestamp).toByteArray())
        md.update(payload)
        val messageDigest = md.digest()
        val no = BigInteger(1, messageDigest)
        return no.toString(16)
    }

    /**
     *
     */
    private fun generate25519KeyPair() {
        try {
            val ls = LazySodiumAndroid(SodiumAndroid())
            val sign: Sign.Lazy = ls
            val ed25519KeyPair = sign.cryptoSignKeypair()
            val ed25519PrvKey = Arrays.copyOfRange(ed25519KeyPair.secretKey.asBytes, 0, 32)
            currentSession.selfParticipant!!.ed25519Pubkey =
                CryptoUtils.bytesToHexString(ed25519KeyPair.publicKey.asBytes)
            currentSession.selfParticipant!!.ed25519PrvKey =
                CryptoUtils.bytesToHexString(ed25519PrvKey)
            Log.i(TAG, "generate25519KeyPair prv_key:" + currentSession.selfParticipant!!.ed25519PrvKey)
        } catch (e: SodiumException) {
            e.printStackTrace()
        }
    }

    /**
     *
     * @param content
     * @return
     */
    private fun encryptionContent(
        ed25519PublicKey: ByteArray,
        ed25519PrivateKey: ByteArray,
        content: ByteArray
    ): ByteArray? {
        Log.i(TAG, "---------encrypt--------")
        Log.i(TAG, "ed25519PublicKey: " + Ed25519.bytesToHexString(ed25519PublicKey))
        Log.i(TAG, "ed25519PrivateKey: " + Ed25519.bytesToHexString(ed25519PrivateKey))
        Log.i(TAG, "content: " + String(content))
        val x25519PublicKey = ByteArray(32)
        aNative.convertPublicKeyEd25519ToCurve25519(x25519PublicKey, ed25519PublicKey)
        val x25519PrivateKey = ByteArray(32)
        aNative.convertSecretKeyEd25519ToCurve25519(x25519PrivateKey, ed25519PrivateKey)
        val x25519Agreement = X25519Agreement()
        val shareKey = ByteArray(x25519Agreement.getAgreementSize())
        x25519Agreement.init(X25519PrivateKeyParameters(x25519PrivateKey))
        x25519Agreement.calculateAgreement(X25519PublicKeyParameters(x25519PublicKey), shareKey, 0)
        Log.i(TAG, "shareKey:" + Ed25519.bytesToHexString(shareKey))
        val hkdf = HKDFBytesGenerator(SHA384Digest())
        hkdf.init(HKDFParameters(shareKey, "".toByteArray(), "".toByteArray()))
        val prk = ByteArray(32)
        hkdf.generateBytes(prk, 0, 32)
        Log.i(TAG, "SHA384 prk:" + Ed25519.bytesToHexString(prk))
        //AES-GCM
        val prk_base64 = Base64.encodeToString(prk, Base64.NO_WRAP)
        val iv_str = prk_base64.substring(0, 16)
        Log.i(TAG, "iv_str length:" + iv_str.length)
        val iv = Base64.decode(iv_str, Base64.NO_WRAP)
        Log.i(TAG, "iv length:" + iv.size)
        val key = SecretKeySpec(prk, "AES")
        val spec = GCMParameterSpec(128, iv) // 创建 GCM 参数规范
        try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding") // 创建密码器
            cipher.init(Cipher.ENCRYPT_MODE, key, spec) // 初始化密码器
            val ciphertext = cipher.doFinal(content)
            Log.i(TAG, "final content base64:" + Base64.encodeToString(ciphertext, Base64.NO_WRAP))
            return ciphertext
        } catch (e: BadPaddingException) {
            e.printStackTrace()
        } catch (e: IllegalBlockSizeException) {
            e.printStackTrace()
        } catch (e: InvalidAlgorithmParameterException) {
            e.printStackTrace()
        } catch (e: NoSuchPaddingException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: InvalidKeyException) {
            e.printStackTrace()
        }
        return null
    }

    private fun decryptionContent(
        ed25519PublicKey: ByteArray,
        ed25519PrivateKey: ByteArray,
        content: ByteArray
    ): ByteArray? {
        Log.i(TAG, "---------decrypt--------")
        Log.i(TAG, "ed25519PublicKey: " + Ed25519.bytesToHexString(ed25519PublicKey))
        Log.i(TAG, "ed25519PrivateKey: " + Ed25519.bytesToHexString(ed25519PrivateKey))
        val x25519PublicKey = ByteArray(32)
        aNative.convertPublicKeyEd25519ToCurve25519(x25519PublicKey, ed25519PublicKey)
        val x25519PrivateKey = ByteArray(32)
        aNative.convertSecretKeyEd25519ToCurve25519(x25519PrivateKey, ed25519PrivateKey)
        val x25519Agreement = X25519Agreement()
        val shareKey = ByteArray(x25519Agreement.getAgreementSize())
        x25519Agreement.init(X25519PrivateKeyParameters(x25519PrivateKey))
        x25519Agreement.calculateAgreement(X25519PublicKeyParameters(x25519PublicKey), shareKey, 0)
        Log.i(TAG, "shareKey:" + Ed25519.bytesToHexString(shareKey))
        val hkdf = HKDFBytesGenerator(SHA384Digest())
        hkdf.init(HKDFParameters(shareKey, "".toByteArray(), "".toByteArray()))
        val prk = ByteArray(32)
        hkdf.generateBytes(prk, 0, 32)
        Log.i(TAG, "SHA384 prk:" + Ed25519.bytesToHexString(prk))
        //AES-GCM
        val prk_base64 = Base64.encodeToString(prk, Base64.NO_WRAP)
        val iv_str = prk_base64.substring(0, 16)
        Log.i(TAG, "iv_str length:" + iv_str.length)
        val iv = Base64.decode(iv_str, Base64.NO_WRAP)
        Log.i(TAG, "iv length:" + iv.size)
        val key = SecretKeySpec(prk, "AES")
        val spec = GCMParameterSpec(128, iv) // 创建 GCM 参数规范
        try {
            val decipher = Cipher.getInstance("AES/GCM/NoPadding") // 创建密码器
            decipher.init(Cipher.DECRYPT_MODE, key, spec) // 初始化密码器
            val ciphertext = decipher.doFinal(content)
            Log.i(TAG, "content source:" + String(ciphertext))
            return ciphertext
        } catch (e: BadPaddingException) {
            e.printStackTrace()
        } catch (e: IllegalBlockSizeException) {
            e.printStackTrace()
        } catch (e: InvalidAlgorithmParameterException) {
            e.printStackTrace()
        } catch (e: NoSuchPaddingException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: InvalidKeyException) {
            e.printStackTrace()
        }
        return null
    }

    fun walletBuildAndSaveSession(connectRequest: ConnectRequest) {
        if (currentSession == null || currentSession.selfParticipant == null) {
            return
        }
        currentSession.peerTopic = connectRequest.topic
        if (connectRequest.icons != null && connectRequest.icons!!.size > 0) {
            currentSession.peerParticipant!!.iconUrl = connectRequest.icons!![0]
        }
        currentSession.peerParticipant!!.website = connectRequest.url
        currentSession.peerParticipant!!.redirect = connectRequest.redirect
        DefaultSPHelper.appendSession(currentSession)
    }



}