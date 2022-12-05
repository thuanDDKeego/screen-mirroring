package com.abc.mirroring.cast

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.migration.CustomInject
import timber.log.Timber

@CustomInject
@HiltAndroidApp
class CastTvApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Timber.i("CastTvApplication created")
    }
}