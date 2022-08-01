package com.abc.mirroring.config

import java.util.*

class AppPreferences : Preferences(name = "app_preferences") {
    var isTheFirstTimeUseApp by booleanPref(defaultValue = true)
    var lastTimeAdOpenApp by longPref(defaultValue = 0L)
    var pinCode by stringPref(defaultValue = "0000")
    var isTurnOnPinCode by booleanPref(defaultValue = false)
    var languageSelected by stringPref(defaultValue = Locale.getDefault().language.toString())
}