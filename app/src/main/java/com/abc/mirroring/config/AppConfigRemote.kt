package com.abc.mirroring.config

import com.abc.mirroring.BuildConfig

class AppConfigRemote() :
    PreferencesAdapterRC(name = "app_config_remote", devMode = BuildConfig.DEBUG) {
    var isUsingAdsOpenApp by booleanPref(defaultValue = true)
    var timeBetweenTwoAdsOpenAppShow by intPref(defaultValue = 10000)

    var isUsingAdmobGoMirrorDevice by booleanPref(defaultValue = false)
    var isUsingAdmobHomeOnboarding by booleanPref(defaultValue = false)
    var isUsingAdmobBackFromTutorial by booleanPref(defaultValue = false)
    var isUsingAdmobNative by booleanPref(defaultValue = false)

    var isUsingAdmobBrowserMirrorReward by booleanPref(defaultValue = false)
}
