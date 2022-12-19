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
import com.abc.mirroring.R
import com.abc.mirroring.config.AppPreferences
import com.abc.mirroring.config.Preferences
import dev.sofi.ads.Rewarded
import dev.sofi.ads.RewardedInterstitial

class AdCenterOS : Initializer<AdCenter> {
    override fun create(context: Context): AdCenter {
        MobileAds.initialize(context)
        Preferences.init(context)

        return AdCenter.initialize(
            context as Application,
//            MutableStateFlow(if (BuildConfig.DEBUG || AppPreferences.isPremiumSubscribed == true) false else true)
            MutableStateFlow(AppPreferences().isPremiumSubscribed != true)
        ).apply {
            exitDialog = ExitDialog(context.getString(R.string.ad_native_advanced_general), this.enable)
            banner = Banner(context.getString(R.string.ad_banner_general), MutableStateFlow(false))
            interstitial = Interstitial(context.getString(R.string.ad_interstitial_general), this.enable)
            rewardedInterstitial = RewardedInterstitial(context.getString(R.string.ad_rewarded_interstitial_general), this.enable)
            rewarded = Rewarded(context.getString(R.string.ad_rewarded_browser_mirror), this.enable)
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