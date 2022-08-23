package com.abc.mirroring.config

import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import timber.log.Timber

abstract class PreferencesAdapterRC(name: String? = null, private val devMode: Boolean = false) :
    Preferences(name) {
    private val remoteConfig = Firebase.remoteConfig

    init {
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = if (devMode) 0 else 3600
        }
        remoteConfig.apply {
            setConfigSettingsAsync(configSettings)
            setDefaultsAsync(_defaults)
            fetchAndActivate().addOnCompleteListener {
                if (it.isSuccessful) {
                    val updated = it.result
                    Timber.d("Config params updated: $updated")
                    Timber.d("Config params updated: ${remoteConfig.all}")
                    syncRemoteConfigToPreferences()
                }
            }
        }
    }

    private fun syncRemoteConfigToPreferences() = try {
        prefs.edit().apply {
            _defaults.forEach { it ->
                val key = it.key
                when (it.value) {
                    is Boolean -> {
                        val value = remoteConfig.getBoolean(key)
                        putBoolean(key, value)
                        _defaults[key] = value
                    }
                    is String -> {
                        val value = remoteConfig.getString(key)
                        putString(key, value)
                        _defaults[key] = value
                    }
                    is Int -> {
                        val value = remoteConfig.getDouble(key).toInt()
                        putInt(key, value)
                        _defaults[key] = value
                    }
                    is Float -> {
                        val value = remoteConfig.getDouble(key).toFloat()
                        putFloat(key, value)
                        _defaults[key] = value
                    }
                    is Long -> {
                        val value = remoteConfig.getLong(key)
                        putLong(key, value)
                        _defaults[key] = value
                    }
                }
            }
            apply()
        }


        Timber.d(AppPreferences().toString())
    } catch (e: Exception) {
    }
}
