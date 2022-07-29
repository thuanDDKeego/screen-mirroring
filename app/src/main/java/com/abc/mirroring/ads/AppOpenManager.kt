package com.soft.slideshow.ads

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.abc.mirroring.ui.home.HomeActivity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.appopen.AppOpenAd.AppOpenAdLoadCallback
import timber.log.Timber
import java.util.*


/** Prefetches App Open Ads.  */
class AppOpenManager : ActivityLifecycleCallbacks, LifecycleObserver {
    private var appOpenAd: AppOpenAd? = null
    private var loadCallback: AppOpenAdLoadCallback? = null
    private lateinit var myApplication: Application
    private lateinit var appOpenAdId: String
    private var currentActivity: Activity? = null
    private var isShowingAd: Boolean = false
    private var loadTime: Long = 0
    private lateinit var disabledAppOpenList: MutableList<Class<*>>

    var isInitialized = false

    /** Creates and returns ad request.  */
    private val adRequest: AdRequest
        private get() = AdRequest.Builder().build()

    /** Utility method to check if ad was loaded more than n hours ago.  */
    private fun wasLoadTimeLessThanNHoursAgo(numHours: Long): Boolean {
        val dateDifference = Date().time - loadTime
        val numMilliSecondsPerHour: Long = 3600000
        return dateDifference < numMilliSecondsPerHour * numHours
    }

    /** Utility method that checks if ad exists and can be shown.  */
    private val isAdAvailable: Boolean
        get() = appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4)

    companion object {
        const val LOG_TAG = "AppOpenManager"

        @SuppressLint("StaticFieldLeak")
        private var INSTANCE: AppOpenManager? = null

        @JvmStatic
        @get:Synchronized
        val instance: AppOpenManager?
            get() {
                if (INSTANCE == null) {
                    INSTANCE = AppOpenManager()
                }
                return INSTANCE
            }
    }

    /** Constructor  */
    fun init(application: Application, adId: String) {
        isInitialized = true
        myApplication = application
        appOpenAdId = adId
        myApplication.registerActivityLifecycleCallbacks(this)
        disabledAppOpenList = ArrayList()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        fetchAd()
        Log.d(LOG_TAG, "init")
    }

    fun disableAddWithActivity(activityClass: Class<*>) {
        disabledAppOpenList.add(activityClass)
    }

    fun enableAddWithActivity(activityClass: Class<*>) {
        disabledAppOpenList.remove(activityClass)
    }

    /** LifecycleObserver methods  */
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        for (activity in disabledAppOpenList) {
            if (activity.name == currentActivity?.javaClass?.name) {
                return
            }
        }
        Timber.d("activity name ${currentActivity?.javaClass?.name}")
        showAdIfAvailable()
    }

    /** Request an ad  */
    fun fetchAd(onLoadedCallback: (() -> Unit)? = null) {
        // Have unused ad, no need to fetch another.
        if (isAdAvailable) {
            return
        }
        loadCallback = object : AppOpenAdLoadCallback() {
            /**
             * Called when an app open ad has loaded.
             *
             * @param ad the loaded app open ad.
             */
            override fun onAdLoaded(ad: AppOpenAd) {
                appOpenAd = ad
                loadTime = Date().time
                onLoadedCallback?.invoke()
            }

            /**
             * Called when an app open ad has failed to load.
             *
             * @param loadAdError the error.
             */
            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                // Handle the error.
                onLoadedCallback?.invoke()
            }
        }
        val request: AdRequest = AdRequest.Builder().build()
        AppOpenAd.load(
            myApplication, appOpenAdId, request,
            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, loadCallback as AppOpenAdLoadCallback
        )
    }

    fun showAdAtSplash(activity: Activity) {
        val fullScreenContentCallback: FullScreenContentCallback =
            object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    // Set the reference to null so isAdAvailable() returns false.
                    appOpenAd = null
                    isShowingAd = false
                    fetchAd()
                    activity.startActivity(Intent(activity, HomeActivity::class.java))
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    activity.startActivity(Intent(activity, HomeActivity::class.java))
                }

                override fun onAdShowedFullScreenContent() {
                    isShowingAd = true
                }
            }
        if (!isShowingAd && isAdAvailable) {
            appOpenAd?.fullScreenContentCallback = fullScreenContentCallback
            if (currentActivity != null) {
                appOpenAd?.show(currentActivity!!)
            }
        } else {
            fetchAd {
                appOpenAd?.fullScreenContentCallback = fullScreenContentCallback
                if (currentActivity != null) {
                    appOpenAd?.show(currentActivity!!)
                }
            }
        }
    }

    /** Shows the ad if one isn't already showing.  */
    private fun showAdIfAvailable() {
        // Only show ad if there is not already an app open ad currently showing
        // and an ad is available.
        Log.d(LOG_TAG, "showAdIfAvailable $isShowingAd -- $isAdAvailable -- $currentActivity")
        if (!isShowingAd && isAdAvailable) {
            Log.d(LOG_TAG, "Will show ad $currentActivity")
            val fullScreenContentCallback: FullScreenContentCallback =
                object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        // Set the reference to null so isAdAvailable() returns false.
                        appOpenAd = null
                        isShowingAd = false
                        fetchAd()
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {}
                    override fun onAdShowedFullScreenContent() {
                        isShowingAd = true
                    }
                }
            appOpenAd?.fullScreenContentCallback = fullScreenContentCallback
            if (currentActivity != null) {
                appOpenAd?.show(currentActivity!!)
            }
        } else {
            Log.d(LOG_TAG, "Can not show ad.")
            fetchAd()
        }
    }

    /** ActivityLifecycleCallback methods  */
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
    }

    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityPaused(activity: Activity) {
    }

    override fun onActivityStopped(activity: Activity) {
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityDestroyed(activity: Activity) {
        currentActivity = null
    }
}