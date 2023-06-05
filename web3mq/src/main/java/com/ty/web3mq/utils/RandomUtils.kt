package com.ty.web3mq.utils

import com.ty.web3mq.utils.RandomUtils
import java.lang.StringBuilder
import java.util.*

object RandomUtils {
    fun randomNonce(): String {
        val random = Random()
        val bytes = ByteArray(16)
        random.nextBytes(bytes)
        return bytesToHex(bytes)
    }

    fun randomBoolean(): Boolean {
        val random = Random()
        return random.nextBoolean()
    }

    fun random4Number(): Int {
        val random = Random()
        return random.nextInt(9000) + 1000
    }

    private fun bytesToHex(bytes: ByteArray): String {
        val hexString = StringBuilder(bytes.size)
        for (i in bytes.indices) {
            val hex = Integer.toHexString(0xff and bytes[i].toInt())
            if (hex.length == 1) {
                hexString.append("0")
            }
            hexString.append(hex)
        }
        return hexString.toString()
    }
}