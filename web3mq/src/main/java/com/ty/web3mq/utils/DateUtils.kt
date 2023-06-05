package com.ty.web3mq.utils

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

object DateUtils {
    fun getISOOffsetTime(timeStamp: Long): String {
        // 转换为OffsetDateTime对象
        val instant = Instant.ofEpochSecond(timeStamp)
        val offsetDateTime = instant.atOffset(ZoneOffset.UTC)

        // 转换为指定格式的字符串
        val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
        return offsetDateTime.format(formatter)
    }

    fun getTimeStampFromISOOffsetTime(IOSOffset: String?): Long {
        // 解析为OffsetDateTime对象
        val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
        val offsetDateTime = OffsetDateTime.parse(IOSOffset, formatter)

        // 转换为时间戳（包含毫秒）
        val instant = offsetDateTime.toInstant()
        return instant.toEpochMilli()
    }

    val uUID: String
        get() = UUID.randomUUID().toString().replace("-", "").uppercase(Locale.getDefault())
    val dateTime: String
        get() {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS Z")
            return sdf.format(Date())
        }

    fun getTimeString(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm")
        return sdf.format(Date(timestamp))
    }

    fun getTimeStringM(timestamp: Long): String {
        val sdf = SimpleDateFormat("MM-dd HH:mm")
        return sdf.format(Date(timestamp))
    }

    fun getTimeStringH(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm")
        return sdf.format(Date(timestamp))
    }

    fun getTimeStringNotification(timestamp: Long): String {
        val gapTime = System.currentTimeMillis() - timestamp
        return if (gapTime < 60 * 1000) {
            (gapTime / 1000).toString() + "s ago"
        } else if (gapTime < 60 * 60 * 1000) {
            (gapTime / (60 * 1000)).toString() + "m ago"
        } else if (gapTime < 60 * 60 * 24 * 1000) {
            (gapTime / (60 * 60 * 1000)).toString() + "hours ago"
        } else if (gapTime < 60 * 60 * 24 * 7 * 1000) {
            (gapTime / (60 * 60 * 24 * 1000)).toString() + "days ago"
        } else if (gapTime < 60L * 60 * 24 * 30 * 1000) {
            (gapTime / (60 * 60 * 24 * 7 * 1000)).toString() + "weeks ago"
        } else if (gapTime < 60L * 60 * 24 * 365 * 1000) {
            (gapTime / (60L * 60 * 24 * 30 * 1000)).toString() + "mouths ago"
        } else {
            (gapTime / (60L * 60 * 24 * 365 * 1000)).toString() + "year ago"
        }
    }
}