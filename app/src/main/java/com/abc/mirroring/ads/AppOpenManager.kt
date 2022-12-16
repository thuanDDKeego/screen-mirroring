package com.abc.mirroring.ads

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.abc.mirroring.config.AppConfigRemote
import com.abc.mirroring.config.AppPreferences
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.appopen.AppOpenAd.AppOpenAdLoadCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*


/** Prefetches App Open Ads.  */
class AppOpenManager : ActivityLifecycleCallbacks,
    LifecycleObserver {
    private var appOpenAd: AppOpenAd? = null
    private var loadCallback: AppOpenAdLoadCallback? = null
    private lateinit var myApplication: Application
    private lateinit var appOpenAdId: String
    var currentActivity: Activity? = null
    private var isShowingAd: Boolean = false
    private var loadTime: Long = 0
    private lateinit var disabledAppOpenList: MutableList<Class<*>>

    var isInitialized = false

    fun resetAdOpenAd() {
        appOpenAd = null
    }

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
    fun fetchAd(timeoutAdRequest: Long? = null, onLoadedCallback: ((AppOpenAd?) -> Unit)? = null) {
        // Have unused ad, no need to fetch another.
        if (isAdAvailable) {
            return
        }
        var isTimeout = false
        var job: Job? = null
        if (timeoutAdRequest != null) {
            job = CoroutineScope(Dispatchers.Default).launch {
                delay(timeoutAdRequest)
                onLoadedCallback?.invoke(null)
                isTimeout = true
                Timber.d("app open ad timeout invoked callback")
            }
        }
        loadCallback = object : AppOpenAdLoadCallback() {
            /**
             * Called when an app open ad has loaded.
             *
             * @param ad the loaded app open ad.
             */
            override fun onAdLoaded(ad: AppOpenAd) {
                appOpenAd = ad
                if (!isTimeout) {
                    job?.cancel()
                    loadTime = Date().time
                    onLoadedCallback?.invoke(ad)
                }
            }

            /**
             * Called when an app open ad has failed to load.
             *
             * @param loadAdError the error.
             */
            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                // Handle the error.
                if (!isTimeout) {
                    job?.cancel()
                    onLoadedCallback?.invoke(null)
                }
            }
        }
        val request: AdRequest = AdRequest.Builder().build()
        AppOpenAd.load(
            myApplication, appOpenAdId, request,
            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, loadCallback as AppOpenAdLoadCallback
        )
    }

    fun showAdAtSplash(activity: Activity, callback: (() -> Unit)) {
        val fullScreenContentCallback: FullScreenContentCallback =
            object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    // Set the reference to null so isAdAvailable() returns false.
                    appOpenAd = null
                    isShowingAd = false
                    fetchAd()
                    callback.invoke()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    callback.invoke()
                }

                override fun onAdShowedFullScreenContent() {
                    isShowingAd = true
                }
            }
        if (!isShowingAd && isAdAvailable) {
            appOpenAd?.fullScreenContentCallback = fullScreenContentCallback
            if (currentActivity != null) {
                AppPreferences().lastTimeAdOpenApp = System.currentTimeMillis()
                appOpenAd?.show(currentActivity!!)
            }
        } else {
            callback.invoke()
        }
    }

    /** Shows the ad if one isn't already showing.  */
    private fun showAdIfAvailable() {
        // Only show ad if there is not already an app open ad currently showing
        // and an ad is available.
        Log.d(LOG_TAG, "showAdIfAvailable $isShowingAd -- $isAdAvailable -- $currentActivity")
        val currentTime = System.currentTimeMillis()
        val timeFromTheLast = currentTime - AppPreferences().lastTimeAdOpenApp!!
        if (!isShowingAd && isAdAvailable && timeFromTheLast >= AppConfigRemote().timeBetweenTwoAdsOpenAppShow!! && AppPreferences().isPremiumSubscribed == false) {
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
                AppPreferences().lastTimeAdOpenApp = currentTime
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
        Timber.d("currentActivity: ${currentActivity?.javaClass?.name}")
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
        Timber.d("currentActivity: ${currentActivity?.javaClass?.name}")

    }
}