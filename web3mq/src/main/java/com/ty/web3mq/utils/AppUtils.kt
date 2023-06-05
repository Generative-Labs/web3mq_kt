package com.ty.web3mq.utils

import android.app.Application

object AppUtils {
    fun getApplicationContext(): Application? {
        try {
            val application = Class.forName("android.app.ActivityThread")
                .getMethod("currentApplication").invoke(null, null as Array<Any?>?) as Application
            if (application != null) {
                return application
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            val application = Class.forName("android.app.AppGlobals")
                .getMethod("getInitialApplication")
                .invoke(null, null as Array<Any?>?) as Application
            if (application != null) {
                return application
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}