package com.abc.mirroring

import AdType
import android.app.Application
import com.abc.mirroring.config.Preferences
import com.abc.mirroring.config.ReleaseTree
import com.abc.sreenmirroring.ui.splash.SplashActivity
import com.elvishew.xlog.LogLevel
import com.elvishew.xlog.XLog
import com.google.android.gms.ads.AdActivity
import com.soft.slideshow.ads.AppOpenManager
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

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
        AppOpenManager.instance?.init(this, this.getString(AdType.APP_OPEN.adsId))
        AppOpenManager.instance?.disableAddWithActivity(AdActivity::class.java)
        AppOpenManager.instance?.disableAddWithActivity(SplashActivity::class.java)
    }

    private fun initLogger() {
        if (BuildConfig.DEBUG) {
            XLog.init(LogLevel.ALL)
        } else {
            XLog.init(LogLevel.ERROR)
        }
    }
}