package com.abc.mirroring.cast.setup.startup

import android.app.Application
import android.content.Context
import androidx.startup.Initializer
import com.abc.mirroring.setup.BannerObservable
import com.abc.mirroring.setup.center.HakiObservation

class HakiOS : Initializer<HakiObservation> {
    override fun create(context: Context): HakiObservation {
        return HakiObservation()
            .apply {
                register(BannerObservable)
            }.also {
                (context as Application).registerActivityLifecycleCallbacks(it)
            }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}