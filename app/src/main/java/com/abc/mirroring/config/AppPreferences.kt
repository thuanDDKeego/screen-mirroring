package com.abc.mirroring.config

import java.util.*

class AppPreferences : Preferences(name = "app_preferences") {
    var isPremiumSubscribed by booleanPref(defaultValue = false)
    var purchaseDate by longPref(defaultValue = 0L)
    var isTheFirstTimeUseApp by booleanPref(defaultValue = true)
    var lastTimeAdOpenApp by longPref(defaultValue = 0L)
    var pinCode by stringPref(defaultValue = "0000")
    var isTurnOnPinCode by booleanPref(defaultValue = false)
    var languageSelected by stringPref(defaultValue = Locale.getDefault().language.toString())
    var isRated  by booleanPref(defaultValue = false)
    var countTimeOpenApp by intPref(defaultValue = 0)
    var countAdsClosed by intPref(defaultValue = 0)
    var countSatisfied by intPref(defaultValue = 0)

    var screenMirroringCountUsages by intPref(defaultValue = 0)
    var browserMirroringCountUsages by intPref(defaultValue = 0)
    var paintToolCountUsages by intPref(defaultValue = 0)

    var isInsertedDefaultM3U by booleanPref(defaultValue = false)
}