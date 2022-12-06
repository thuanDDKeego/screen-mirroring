package com.abc.mirroring

import AdType
import android.app.Application
import androidx.annotation.NonNull
import com.abc.mirroring.ads.AppOpenManager
import com.abc.mirroring.config.AppPreferences
import com.abc.mirroring.config.Preferences
import com.abc.mirroring.config.ReleaseTree
import com.abc.mirroring.ui.splash.SplashActivity
import com.android.billingclient.api.*
import com.elvishew.xlog.LogLevel
import com.elvishew.xlog.XLog
import com.google.android.gms.ads.AdActivity
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.migration.CustomInject
import timber.log.Timber
import java.util.*

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
