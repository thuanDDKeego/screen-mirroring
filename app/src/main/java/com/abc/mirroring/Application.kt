package com.abc.mirroring

import android.app.Application
import com.abc.mirroring.config.Preferences
import com.abc.mirroring.config.ReleaseTree
import com.elvishew.xlog.LogLevel
import com.elvishew.xlog.XLog
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.migration.CustomInject
import timber.log.Timber

@CustomInject
@HiltAndroidApp
class Application : Application() {

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
