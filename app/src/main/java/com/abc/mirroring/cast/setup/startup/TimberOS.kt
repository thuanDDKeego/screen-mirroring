package com.abc.mirroring.cast.setup.startup

import android.content.Context
import android.util.Log
import androidx.startup.Initializer
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import net.sofigo.cast.tv.BuildConfig
import timber.log.Timber

/**
 * OS means "on startup"
 * Class này sẽ được load trước khi App khởi động
 */
class TimberOS : Initializer<Unit> {

    override fun create(context: Context) {
        if (BuildConfig.DEBUG) {
            Timber.plant(
                object : Timber.DebugTree() {
                    override fun log(
                        priority: Int, tag: String?, message: String, t: Throwable?
                    ) {
                        super.log(priority, "sofi_$tag", "$tag >> $message", t)
                    }
                },
            )
            Timber.d("Timber is initialized.")

        } else {
            Timber.plant(ReleaseTree())
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = listOf(FirebaseOS::class.java)
}


class ReleaseTree : Timber.Tree() {

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority == Log.ERROR) {
            Firebase.crashlytics.recordException(t ?: Exception(message))
        }
//        if (priority == Log.INFO) {
//            Firebase.analytics.logEvent(message) {}
//        }
    }
}