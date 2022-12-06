package com.abc.mirroring.cast.setup.startup

import android.content.Context
import androidx.startup.Initializer
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics

class FirebaseOS : Initializer<FirebaseAnalytics> {

    override fun create(context: Context): FirebaseAnalytics {
        FirebaseCrashlytics.getInstance()
        return FirebaseAnalytics.getInstance(context)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}