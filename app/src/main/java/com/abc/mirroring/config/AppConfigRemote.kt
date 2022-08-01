package com.documentmanager.viewer.config

import com.abc.mirroring.BuildConfig

class AppConfigRemote :
    PreferencesAdapterRC(name = "app_config_remote", devMode = BuildConfig.DEBUG) {
    var isUsingAdsOpenApp by booleanPref(defaultValue = true)

}