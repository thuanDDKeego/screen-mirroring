package com.abc.mirroring.cast.setup.config

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import kotlin.properties.Delegates
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Represents a single [SharedPreferences] file.
 *
 * Init by App Application:
 * ```kotlin
 *    override fun onCreate() {
 *      super.onCreate()
 *      ...
 *      Preferences.init(context)
 *      ...
 *    }
 * ```
 *
 * Usage:
 *
 * ```kotlin
 * class UserPreferences : Preferences() {
 *   var emailAccount by stringPref()
 *   var showSystemAppsPreference by booleanPref()
 * }
 * ```
 */
// Ignore unused warning. This class needs to handle all data types, regardless of whether the method is used.
// Allow unchecked casts - we can blindly trust that data we read is the same type we saved it as..
abstract class Preferences(private val name: String? = null) {

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var context: Context? = null

        /**
         * Initialize PrefDelegate with a Context reference.
         *
         * **This method needs to be called before any other usage of PrefDelegate!!**
         */
        fun init(context: Context) {
            Companion.context = context
        }
    }

    val prefs: SharedPreferences by lazy {
        if (context != null) {
            context!!.getSharedPreferences(name ?: javaClass.simpleName, Context.MODE_PRIVATE)
        } else {
            throw IllegalArgumentException("Context was not initialized. Call Preferences.init(context) before using it")
        }
    }

    val _defaults = HashMap<String, Any?>()

    private val listeners = mutableListOf<SharedPrefsListener>()

    interface SharedPrefsListener {
        fun onSharedPrefChanged(property: KProperty<*>)
    }

    fun addListener(sharedPrefsListener: SharedPrefsListener) {
        listeners.add(sharedPrefsListener)
    }

    fun removeListener(sharedPrefsListener: SharedPrefsListener) {
        listeners.remove(sharedPrefsListener)
    }

    fun clearListeners() = listeners.clear()

    sealed class StorableType {
        object String : StorableType()
        object Int : StorableType()
        object Float : StorableType()
        object Boolean : StorableType()
        object Long : StorableType()
        object StringSet : StorableType()
    }

    inner class GenericPrefDelegate<T>(
        private val prefKey: String?,
        private val defaultValue: T?,
        private val type: StorableType,
    ) : ReadWriteProperty<Preferences, T?> {
        var key: String by Delegates.notNull()

        operator fun provideDelegate(
            thisRef: Preferences,
            property: KProperty<*>
        ): GenericPrefDelegate<T> {
            key = prefKey ?: property.name
            thisRef._defaults[key] = defaultValue
            return this
        }

        override fun getValue(thisRef: Preferences, property: KProperty<*>): T? =
            when (type) {
                StorableType.String ->
                    prefs.getString(key, defaultValue as String?) as T?

                StorableType.Int ->
                    prefs.getInt(key, defaultValue as Int) as T?

                StorableType.Float ->
                    prefs.getFloat(key, defaultValue as Float) as T?

                StorableType.Boolean ->
                    prefs.getBoolean(key, defaultValue as Boolean) as T?

                StorableType.Long ->
                    prefs.getLong(prefKey ?: property.name, defaultValue as Long) as T?

                StorableType.StringSet ->
                    prefs.getStringSet(key, defaultValue as Set<String>) as T?
            }


        override fun setValue(thisRef: Preferences, property: KProperty<*>, value: T?) =
            when (type) {
                StorableType.String -> {
                    prefs.edit().putString(prefKey ?: property.name, value as String?).apply()
                    onPrefChanged(property)
                }

                StorableType.Int -> {
                    prefs.edit().putInt(prefKey ?: property.name, value as Int).apply()
                    onPrefChanged(property)
                }

                StorableType.Float -> {
                    prefs.edit().putFloat(prefKey ?: property.name, value as Float).apply()
                    onPrefChanged(property)
                }

                StorableType.Boolean -> {
                    prefs.edit().putBoolean(prefKey ?: property.name, value as Boolean).apply()
                    onPrefChanged(property)
                }

                StorableType.Long -> {
                    prefs.edit().putLong(prefKey ?: property.name, value as Long).apply()
                    onPrefChanged(property)
                }

                StorableType.StringSet -> {
                    prefs.edit().putStringSet(prefKey ?: property.name, value as Set<String>)
                        .apply()
                    onPrefChanged(property)
                }
            }


    }

    fun stringPref(prefKey: String? = null, defaultValue: String? = null) =
        GenericPrefDelegate(prefKey, defaultValue, StorableType.String)

    fun intPref(prefKey: String? = null, defaultValue: Int = 0) =
        GenericPrefDelegate(prefKey, defaultValue, StorableType.Int)

    fun floatPref(prefKey: String? = null, defaultValue: Float = 0f) =
        GenericPrefDelegate(prefKey, defaultValue, StorableType.Float)

    fun booleanPref(prefKey: String? = null, defaultValue: Boolean = false) =
        GenericPrefDelegate(prefKey, defaultValue, StorableType.Boolean)

    fun longPref(prefKey: String? = null, defaultValue: Long = 0L) =
        GenericPrefDelegate(prefKey, defaultValue, StorableType.Long)

    fun stringSetPref(prefKey: String? = null, defaultValue: Set<String> = HashSet()) =
        GenericPrefDelegate(prefKey, defaultValue, StorableType.StringSet)

    private fun onPrefChanged(property: KProperty<*>) {
        listeners.forEach { it.onSharedPrefChanged(property) }
    }

    override fun toString(): String {
        return "${this.javaClass.name}(PreferencesName=$name, _defaults=$_defaults)"
    }

}