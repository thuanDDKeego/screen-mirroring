package com.abc.sreenmirroring

import android.app.Application
import com.abc.sreenmirroring.config.Preferences
import com.abc.sreenmirroring.config.ReleaseTree
import com.elvishew.xlog.LogLevel
import com.elvishew.xlog.XLog
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class Application: Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(ReleaseTree())
        }
        Preferences.init(this)
        initLogger()
    }

    private fun initLogger() {
        if (BuildConfig.DEBUG) {
            XLog.init(LogLevel.ALL)
        } else {
            XLog.init(LogLevel.ERROR)
        }
    }
}