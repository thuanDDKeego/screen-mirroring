package com.abc.sreenmirroring.config

class AppPreferences: Preferences(name = "app_preferences") {
    var isTheFirstTimeUseApp by booleanPref(defaultValue = false)
}