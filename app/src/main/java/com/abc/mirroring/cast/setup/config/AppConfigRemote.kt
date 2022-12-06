package com.abc.mirroring.cast.setup.config

import android.annotation.SuppressLint
import com.abc.mirroring.BuildConfig
import com.google.gson.GsonBuilder

@SuppressLint("StaticFieldLeak")
object AppConfigRemote : PreferencesAdapterRC(name = "app_config_remote", devMode = BuildConfig.DEBUG) {
    // on/off ads remote config
    var connect_sdk_devices by stringPref(defaultValue = "[]")
    var ui_home_version by intPref(defaultValue = 1)
    var player_control_counter by intPref(defaultValue = 5)
    var premium_enable by booleanPref(defaultValue = false)

    private val gson = GsonBuilder().create()

    fun getDevices() = gson.fromJson(connect_sdk_devices, Array<String>::class.java).toList()
}