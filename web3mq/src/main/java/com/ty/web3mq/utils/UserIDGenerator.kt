package com.ty.web3mq.utils

import android.util.Base64
import com.ty.web3mq.utils.CryptoUtils.SHA3_ENCODE
import com.ty.web3mq.utils.CryptoUtils

object UserIDGenerator {
    fun generateUserID(wallet_type: String, wallet_address: String): String {
        return "user:" + Base64.encodeToString(
            SHA3_ENCODE("$wallet_type:$wallet_address").toByteArray(),
            Base64.NO_WRAP
        )
    }
}