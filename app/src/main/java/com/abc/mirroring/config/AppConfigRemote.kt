package com.abc.mirroring.config

import com.abc.mirroring.BuildConfig

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
}
