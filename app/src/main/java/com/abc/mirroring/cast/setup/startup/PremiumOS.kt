package com.abc.mirroring.cast.setup.startup

import android.content.Context
import androidx.startup.Initializer
import net.sofigo.cast.tv.section.billing.BillingConnection
import com.abc.mirroring.cast.setup.config.AppPreferences

class PremiumOS: Initializer<Unit> {
    override fun create(context: Context) {
        BillingConnection().getTimeSubscribe(context) { time ->
                AppPreferences.isPremiumSubscribed = time > 0L
        }
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> = mutableListOf()
}