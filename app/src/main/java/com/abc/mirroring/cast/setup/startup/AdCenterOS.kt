package com.abc.mirroring.cast.setup.startup

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.startup.Initializer
import com.google.android.gms.ads.MobileAds
import dev.sofi.ads.AdCenter
import dev.sofi.ads.AppOpen
import dev.sofi.ads.Banner
import dev.sofi.ads.ExitDialog
import dev.sofi.ads.Interstitial
import dev.sofi.ads.Native
import kotlinx.coroutines.flow.MutableStateFlow
import net.sofigo.cast.tv.BuildConfig
import net.sofigo.cast.tv.R
import com.abc.mirroring.cast.setup.config.AppPreferences
import com.abc.mirroring.cast.setup.config.Preferences

class AdCenterOS : Initializer<AdCenter> {
    override fun create(context: Context): AdCenter {
        MobileAds.initialize(context)
        Preferences.init(context)

        return AdCenter.initialize(
            context as Application,
            MutableStateFlow(if (BuildConfig.DEBUG || AppPreferences.isPremiumSubscribed == true) false else true)
        ).apply {
            exitDialog = ExitDialog(context.getString(R.string.ad_native_advanced_general), this.enable)
            banner = Banner(context.getString(R.string.ad_banner_general), MutableStateFlow(false))
            interstitial = Interstitial(context.getString(R.string.ad_interstitial_general), this.enable)
            appOpen = AppOpen(context.getString(R.string.ad_app_open_general), this.enable)
            native = Native(context.getString(R.string.ad_native_advanced_general), enable = this.enable)
        }.also {
            Log.i("sofi_AdCenterOS", "AdCenter created")
            context.registerActivityLifecycleCallbacks(it)
            ProcessLifecycleOwner.get().lifecycle.addObserver(it)
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}