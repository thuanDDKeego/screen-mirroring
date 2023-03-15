package com.abc.mirroring.setup.center

import android.app.Activity
import android.os.Bundle

/**
 * Helper Class for init & manage long-lifecycle objects
 * that always run in background of activity lifecycle
 */
class HakiObservation : HakiObservable {

    private val modules: MutableList<HakiObservable> = mutableListOf()

    fun register(module: HakiObservable): HakiObservation {
        modules.add(module)
        return this
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        modules.forEach { it.onActivityCreated(activity, savedInstanceState) }
    }

    override fun onActivityStarted(activity: Activity) {
        modules.forEach { it.onActivityStarted(activity) }
    }

    override fun onActivityResumed(activity: Activity) {
        modules.forEach { it.onActivityResumed(activity) }
    }

    override fun onActivityPaused(activity: Activity) {
        modules.forEach { it.onActivityPaused(activity) }
    }

    override fun onActivityStopped(activity: Activity) {
        modules.forEach { it.onActivityStopped(activity) }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        modules.forEach { it.onActivitySaveInstanceState(activity, outState) }
    }

    override fun onActivityDestroyed(activity: Activity) {
        modules.forEach { it.onActivityDestroyed(activity) }
    }
}