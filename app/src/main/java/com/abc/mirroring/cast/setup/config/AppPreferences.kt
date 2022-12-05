package com.abc.mirroring.cast.setup.config

import android.annotation.SuppressLint

@SuppressLint("StaticFieldLeak")
object AppPreferences : Preferences(name = "app_preferences") {
     var isPremiumSubscribed by booleanPref(defaultValue = false)
     var isRated by booleanPref(defaultValue = false)
     var appOpenCounter by intPref(defaultValue = 0)
}