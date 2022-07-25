package com.abc.sreenmirroring.config

import android.util.Log
import timber.log.Timber

fun logCrash(logMessage: String, throwable: Throwable?) {
    try {
//        FirebaseCrashlytics.getInstance().apply {
//            log(logMessage)
//            if (throwable != null) {
//                recordException(throwable)
//            }
//        }
    } catch (e: Exception) {
        Timber.tag("FirebaseCrashlytics").d("logCrash")
    }
}

class ReleaseTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority == Log.ERROR || priority == Log.WARN) logCrash(message, t)
    }
}