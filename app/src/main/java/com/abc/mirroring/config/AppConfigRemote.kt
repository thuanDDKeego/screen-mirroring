package com.abc.mirroring.config

import com.abc.mirroring.BuildConfig
import com.google.gson.GsonBuilder

class AppConfigRemote() :
    PreferencesAdapterRC(name = "app_config_remote", devMode = BuildConfig.DEBUG) {
    var isUsingAdsOpenApp by booleanPref(defaultValue = true)
    var timeBetweenTwoAdsOpenAppShow by intPref(defaultValue = 180000)

    // on/off config remote
    var turnOnHomeTopNative by booleanPref(defaultValue = true)
    var turnOnHomeBrowserReward by booleanPref(defaultValue = true)
    var turnOnGoToMirrorDeviceInterstitial by booleanPref(defaultValue = true)
    var turnOnTopNativeDeviceMirror by booleanPref(defaultValue = true)
    var turnOnBottomTutorialNative by booleanPref(defaultValue = true)
    var turnOnInlineFAQNative by booleanPref(defaultValue = true)
    var turnOnTopDevicesNative by booleanPref(defaultValue = true)
    var turnOnBackFromTutorialInterstitial by booleanPref(defaultValue = true)

    //ad timeout
    var adsTimeout by intPref(defaultValue = 0)

    //theme
    var isHalloweenTheme by booleanPref(defaultValue = false)

    //enable premium feature
    var enable_premium by booleanPref(defaultValue = false)

    private var connect_sdk_devices by stringPref(defaultValue = "[]")
    var ui_home_version by intPref(defaultValue = 1)
    var player_control_counter by intPref(defaultValue = 5)

    private val gson = GsonBuilder().create()

    fun getDevices() = gson.fromJson(connect_sdk_devices, Array<String>::class.java).toList()

}
