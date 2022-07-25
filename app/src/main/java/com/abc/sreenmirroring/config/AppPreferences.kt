package com.abc.sreenmirroring.config

class AppPreferences : Preferences(name = "app_preferences") {
    var isTheFirstTimeUseApp by booleanPref(defaultValue = true)
    var pinCode by stringPref(defaultValue = "0000")
    var isTurnOnPinCode by booleanPref(defaultValue = false)
}