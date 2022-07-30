package com.abc.mirroring.config

class AppPreferences : Preferences(name = "app_preferences") {
    var isTheFirstTimeUseApp by booleanPref(defaultValue = true)
    var lastTimeAdOpenApp by longPref(defaultValue = 0L)
    var pinCode by stringPref(defaultValue = "0000")
    var isTurnOnPinCode by booleanPref(defaultValue = false)
}