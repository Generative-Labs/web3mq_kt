package com.ty.web3mq.utils

import android.util.Log
import org.web3j.crypto.Sign.SignatureData
import org.web3j.utils.Numeric
import com.ty.web3mq.utils.CryptoUtils
import org.bouncycastle.jcajce.provider.digest.SHA3
import org.web3j.crypto.Credentials
import org.web3j.crypto.Sign
import java.lang.StringBuilder
import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import kotlin.experimental.and

//import org.web3j.crypto.Credentials;
//import org.web3j.crypto.Sign;
//import org.web3j.utils.Numeric;
object CryptoUtils {
    fun signMessage(pri_key: String?, message: String): String {
        val credentials = Credentials.create(pri_key)
        val messageBytes = message.toByteArray(StandardCharsets.UTF_8)
        val signature = Sign.signPrefixedMessage(messageBytes, credentials.ecKeyPair)
        val retval = ByteArray(65)
        System.arraycopy(signature.r, 0, retval, 0, 32)
        System.arraycopy(signature.s, 0, retval, 32, 32)
        System.arraycopy(signature.v, 0, retval, 64, 1)
        return Numeric.toHexString(retval)
    }

    fun SHA3_ENCODE(input: String): String {
        Log.i("CryptoUtils", "SHA3 String:$input")
        val md: MessageDigest = SHA3.Digest224()
        // digest() method is called
        // to calculate message digest of the input string
        // returned as array of byte
        val messageDigest = md.digest(input.toByteArray())

        // Convert byte array into signum representation
        val no = BigInteger(1, messageDigest)

        // Convert message digest into hex value
        var hashtext = no.toString(16)

        // Add preceding 0s to make it 32 bit
        while (hashtext.length < 56) {
            hashtext = "0$hashtext"
        }
        return hashtext
    }

    fun SHA256_ENCODE(input: String): String? {
        var md: MessageDigest? = null
        val bt = input.toByteArray()
        return try {
            md = MessageDigest.getInstance("SHA-256") // 将此换成SHA-1、SHA-512、SHA-384等参数
            md.update(bt)
            bytesToHexString(md.digest())
        } catch (e: NoSuchAlgorithmException) {
            null
        }
    }

    fun SHA1_ENCODE(input: String): String? {
        return SHA1_ENCODE(input.toByteArray())
    }

    fun SHA1_ENCODE(input: ByteArray?): String? {
        var md: MessageDigest? = null
        return try {
            md = MessageDigest.getInstance("SHA-1") // 将此换成SHA-1、SHA-512、SHA-384等参数
            md.update(input)
            bytesToHexString(md.digest())
        } catch (e: NoSuchAlgorithmException) {
            null
        }
    }

    fun bytesToHexString(src: ByteArray): String {
//        val builder = StringBuilder()
//        if (src == null || src.size <= 0) {
//            return null
//        }
//        var hv: String
//        for (i in src.indices) {
//            // 以十六进制（基数 16）无符号整数形式返回一个整数参数的字符串表示形式，并转换为大写
//            hv = Integer.toHexString((src[i] and 0xFF.toByte()).toInt()).uppercase(Locale.getDefault())
//            if (hv.length < 2) {
//                builder.append(0)
//            }
//            builder.append(hv)
//        }
        return src.toHex()
    }

    fun hexStringToBytes(hexString: String?): ByteArray {
//        var hexString = hexString ?: return null
//        hexString = hexString.lowercase(Locale.getDefault())
//        val byteArray = ByteArray(hexString.length shr 1)
//        var index = 0
//        for (i in 0 until hexString.length) {
//            if (index > hexString.length - 1) {
//                return byteArray
//            }
//            val highDit = (hexString[index].digitToIntOrNull(16) ?: -1 and 0xFF).toInt()
//            val lowDit = (hexString[index + 1].digitToIntOrNull(16) ?: -1 and 0xFF).toInt()
//            byteArray[i] = (highDit shl 4 or lowDit).toByte()
//            index += 2
//        }
        return hexString!!.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
    }

    fun ByteArray.toHex(): String {
        return joinToString("") { byte -> "%02x".format(byte) }
    }
}