package com.ty.web3mq.utils

import android.util.Base64
import android.util.Log
import net.i2p.crypto.eddsa.EdDSAEngine
import net.i2p.crypto.eddsa.EdDSAPrivateKey
import net.i2p.crypto.eddsa.EdDSAPublicKey
import net.i2p.crypto.eddsa.KeyPairGenerator
import net.i2p.crypto.eddsa.spec.EdDSANamedCurveSpec
import net.i2p.crypto.eddsa.spec.EdDSANamedCurveTable
import net.i2p.crypto.eddsa.spec.EdDSAPrivateKeySpec
import net.i2p.crypto.eddsa.spec.EdDSAPublicKeySpec
import org.bouncycastle.jcajce.provider.digest.SHA3

import java.lang.Exception
import java.lang.StringBuilder
import java.security.*
import java.util.*
import kotlin.experimental.and

object Ed25519 {
    private const val TAG = "Ed25519"
    fun ed25519GenerateKeyPair(): KeyPair {
        val edDsaKpg = KeyPairGenerator()
        return edDsaKpg.generateKeyPair()
    }

    fun generateKeyPair(): Array<String> {
        val edDsaKpg = KeyPairGenerator()
        val keyPair: KeyPair = edDsaKpg.generateKeyPair()
        val pv: EdDSAPrivateKey = keyPair.private as EdDSAPrivateKey
        val privateKey = bytesToHexString(pv.getSeed())!!
        val publicKey = bytesToHexString(pv.getAbyte())!!
        return arrayOf(privateKey, publicKey)
    }

    fun generatePublicKey(prv_key_hex: String): String {
        val spec: EdDSANamedCurveSpec =
            EdDSANamedCurveTable.getByName(EdDSANamedCurveTable.ED_25519)
        val privKey = EdDSAPrivateKeySpec(hexStringToBytes(prv_key_hex), spec)
        val prv_key = EdDSAPrivateKey(privKey)
        val publicKey = bytesToHexString(prv_key.getAbyte())
        Log.i(TAG, "length:" + publicKey!!.length)
        return publicKey
    }

    fun ed25519GenerateKeyPair(seed: String): KeyPair {
        val spec: EdDSANamedCurveSpec =
            EdDSANamedCurveTable.getByName(EdDSANamedCurveTable.ED_25519)
        val privKey = EdDSAPrivateKeySpec(toSeedBytes(seed), spec)
        val pubKey = EdDSAPublicKeySpec(privKey.getA(), spec)
        return KeyPair(EdDSAPublicKey(pubKey), EdDSAPrivateKey(privKey))
    }

    private fun toSeedBytes(seed: String): ByteArray {
        val md: MessageDigest = SHA3.Digest224()
        val seedBytes = ByteArray(32)
        val messageDigest = md.digest(seed.toByteArray())
        System.arraycopy(messageDigest, 0, seedBytes, 32 - messageDigest.size, messageDigest.size)
        return seedBytes
    }

    fun getEd25519PrivateKeyBySeed(seed: String): PrivateKey {
        val spec: EdDSANamedCurveSpec =
            EdDSANamedCurveTable.getByName(EdDSANamedCurveTable.ED_25519)
        return EdDSAPrivateKey(EdDSAPrivateKeySpec(toSeedBytes(seed), spec))
    }

    fun getEd25519PrivateKey(prv_key_hex: String?): PrivateKey {
        val spec: EdDSANamedCurveSpec =
            EdDSANamedCurveTable.getByName(EdDSANamedCurveTable.ED_25519)
        return EdDSAPrivateKey(EdDSAPrivateKeySpec(hexStringToBytes(prv_key_hex), spec))
    }

//    @Throws(Exception::class)
    fun ed25519Sign(privateKeyHex: String?, data: ByteArray?): String {
        val edEng = EdDSAEngine()
        edEng.initSign(getEd25519PrivateKey(privateKeyHex))
        edEng.setParameter(EdDSAEngine.ONE_SHOT_MODE)
        edEng.update(data)
        val enEdata: ByteArray = edEng.sign()
        return Base64.encodeToString(enEdata, Base64.NO_WRAP)
    }

    fun ed25519VerifySign(publicKey: String?, data: String, signData: String?): Boolean? {
        var isSuccess: Boolean? = null
        try {
            val edEng = EdDSAEngine()
            val spec: EdDSANamedCurveSpec =
                EdDSANamedCurveTable.getByName(EdDSANamedCurveTable.ED_25519)
            val publicKeyByte = hexStringToBytes(publicKey)
            val pk: PublicKey = EdDSAPublicKey(EdDSAPublicKeySpec(publicKeyByte, spec))
            edEng.initVerify(pk)
            edEng.setParameter(EdDSAEngine.ONE_SHOT_MODE)
            edEng.update(data.toByteArray())
            isSuccess = edEng.verify(Base64.decode(signData, Base64.NO_WRAP))
        } catch (e: SignatureException) {
            e.printStackTrace()
        } catch (e: InvalidAlgorithmParameterException) {
            e.printStackTrace()
        } catch (e: InvalidKeyException) {
            e.printStackTrace()
        }
        return isSuccess
    }

    @Throws(Exception::class)
    fun ed25519SeedSign(privateKeySeed: String, data: ByteArray?): String {
        val edEng = EdDSAEngine()
        edEng.initSign(getEd25519PrivateKeyBySeed(privateKeySeed))
        edEng.setParameter(EdDSAEngine.ONE_SHOT_MODE)
        edEng.update(data)
        val enEdata: ByteArray = edEng.sign()
        return Base64.encodeToString(enEdata, Base64.NO_WRAP)
    }

    fun bytesToHexString(src: ByteArray?): String {
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
        return src!!.joinToString("") { "%02X".format(it) }
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
}