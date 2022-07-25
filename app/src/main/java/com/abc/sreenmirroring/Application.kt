package com.abc.sreenmirroring

import android.app.Application
import com.abc.sreenmirroring.config.Preferences
import com.abc.sreenmirroring.config.ReleaseTree
import com.elvishew.xlog.LogLevel
import com.elvishew.xlog.XLog
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
<<<<<<< HEAD

@HiltAndroidApp
class Application: Application() {
=======


@HiltAndroidApp
class Application : Application() {

>>>>>>> develop
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
<<<<<<< HEAD
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(ReleaseTree())
        }
        Preferences.init(this)
        initLogger()
    }

    private fun initLogger() {
        if (BuildConfig.DEBUG) {
            XLog.init(LogLevel.ALL)
        } else {
            XLog.init(LogLevel.ERROR)
=======
            Timber.plant(object :Timber.DebugTree(){
                /**
                 * Override [createStackElementTag] to include a add a "method name" to the tag.
                 */
                override fun createStackElementTag(element: StackTraceElement): String {
                    return String.format(
                        "%s:%s",
                        super.createStackElementTag(element),
                        element.methodName
                    )
                }
            })
>>>>>>> develop
        }
    }
}