package com.abc.mirroring.ads

import AdType
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.abc.mirroring.BuildConfig
import com.abc.mirroring.R
import com.abc.mirroring.helper.toPx
import com.applovin.adview.AppLovinFullscreenActivity
import com.applovin.mediation.*
import com.applovin.mediation.ads.MaxAdView
import com.applovin.mediation.ads.MaxInterstitialAd
import com.applovin.mediation.ads.MaxRewardedAd
import com.applovin.mediation.nativeAds.MaxNativeAdListener
import com.applovin.mediation.nativeAds.MaxNativeAdLoader
import com.applovin.mediation.nativeAds.MaxNativeAdView
import com.applovin.sdk.AppLovinSdk
import com.applovin.sdk.AppLovinSdkUtils
import com.applovin.sdk.AppLovinWebViewActivity
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import timber.log.Timber
import java.util.concurrent.TimeUnit
import kotlin.math.pow

/**
 * Hướng dẫn.
 * thêm dòng dưới vào class application
 *
 * ApplovinUtils.getInstance().initialize(this)
 *
 * khi nào sử dung thì gọi.
 * ApplovinUtils.getInstance()...
 */

class ApplovinUtils : MaxAdListener {

    private object Holder {
        @SuppressLint("StaticFieldLeak")
        val INSTANCE = ApplovinUtils()
    }

    companion object {
        @JvmStatic
        fun getInstance(): ApplovinUtils {
            return Holder.INSTANCE
        }
    }

    private var mAdLoadCallback: AdLoadCallback? = null
    private var mAdDisplayCallback: AdDisplayCallback? = null
    private var isInit = false
    private var maxInterstitialAd: MaxInterstitialAd? = null
    private var retryAttempt = 0.0
    private var listenerRevenue: MaxAdRevenueListener? = null


    private var isLoading = false

    abstract class AdLoadCallback {
        abstract fun onLoaded()
        abstract fun onLoadFailed()
    }

    abstract class AdDisplayCallback {
        abstract fun onDisplayed()
    }

    private lateinit var mContext: Context
    fun initialize(context: Context) {
        mContext = context
        Timber.d("Applovin init")
        if (isInit.not()) {
            AppLovinSdk.getInstance(mContext).mediationProvider = "max"
            AppLovinSdk.getInstance(mContext).initializeSdk {
                Timber.d("Applovin init Success")
                isInit = true
            }

            listenerRevenue = MaxAdRevenueListener { impressionData ->
                impressionData?.let {
                    Firebase.analytics.logEvent(FirebaseAnalytics.Event.AD_IMPRESSION) {
                        param(FirebaseAnalytics.Param.AD_PLATFORM, "appLovin")
                        param(FirebaseAnalytics.Param.AD_UNIT_NAME, impressionData.adUnitId)
                        param(FirebaseAnalytics.Param.AD_FORMAT, impressionData.format.label)
                        param(FirebaseAnalytics.Param.AD_SOURCE, impressionData.networkName)
                        param(FirebaseAnalytics.Param.VALUE, impressionData.revenue)
                        param(FirebaseAnalytics.Param.CURRENCY, "USD")
                        // All Applovin revenue is sent in USD
                    }
                }
                Timber.d("MaxAdRevenueListener -> " +
                        "\n\t impressionData.AD_PLATFORM: appLovin" +
                        "\n\t impressionData.AD_UNIT_NAME: ${impressionData.adUnitId}" +
                        "\n\t impressionData.AD_FORMAT: ${impressionData.format.label}" +
                        "\n\t impressionData.AD_SOURCE: ${impressionData.networkName}" +
                        "\n\t impressionData.VALUE: ${impressionData.revenue}" +
                        "\n\t impressionData.CURRENCY: USD"
                )
            }

            if (BuildConfig.DEBUG) {
                AppLovinSdk.getInstance(context).settings.setVerboseLogging(true)
            }
            AppOpenManager.instance?.disableAddWithActivity(AppLovinFullscreenActivity::class.java)
            AppOpenManager.instance?.disableAddWithActivity(AppLovinWebViewActivity::class.java)
        }
    }

    fun openMediationDebugger(activity: Activity) {
        AppLovinSdk.getInstance(activity).showMediationDebugger()
    }

    fun removeAdLoadCallback() {
        mAdLoadCallback = null
    }

    fun removeAdDisplayCallback() {
        mAdDisplayCallback = null
    }

    fun removeListener() {
        removeAdLoadCallback()
        removeAdDisplayCallback()
    }

    /**
     *
     */
    private fun loadInterstitialAd(
        activity: Activity,
        adUnitInterId: AdType,
        adLoadCallback: AdLoadCallback?,
    ) {
//        if (adUnitInterId) {
//            isLoadFailed = true
//            adLoadCallback?.onLoadFailed()
//        }
        if (isInit.not()) return
        if (maxInterstitialAd?.isReady == true) return
        isLoading = true
        mAdLoadCallback = adLoadCallback
        maxInterstitialAd = MaxInterstitialAd(activity.getString(adUnitInterId.adsId), activity)
        listenerRevenue?.let { maxInterstitialAd?.setRevenueListener(it) }
        maxInterstitialAd?.setListener(this)
        maxInterstitialAd?.loadAd()
    }

    private fun showInterstitialAd(adDisplayCallback: AdDisplayCallback?) {
        mAdDisplayCallback = adDisplayCallback
        if (maxInterstitialAd?.isReady == true) {
            maxInterstitialAd?.showAd()
        }
    }

    /**
     * Tự động load và hiển thị InterstitialAd
     */
    fun loadAndShowInterstitialAd(
        activity: Activity,
        adUnitInterId: AdType,
        adDisplayCallback: AdDisplayCallback?,
    ) {

        if (maxInterstitialAd?.isReady == true) {
            removeAdLoadCallback()
//            adDisplayCallback?.onDisplayed()
            showInterstitialAd(adDisplayCallback)
        } else {
            loadInterstitialAd(activity, adUnitInterId, adLoadCallback = object : AdLoadCallback() {
                override fun onLoaded() {
                    removeAdLoadCallback()
//                    adDisplayCallback?.onDisplayed()
                    showInterstitialAd(adDisplayCallback)
                }

                override fun onLoadFailed() {
                    adDisplayCallback?.onDisplayed()
                    removeListener()
                }

            })
        }
    }

    private var nativeAdContainerView: ViewGroup? = null
    private var nativeAdLoader: MaxNativeAdLoader? = null
    private var loadedNativeAd: MaxAd? = null
    private var nativeAd: MaxAd? = null

    private inner class NativeAdListener : MaxNativeAdListener() {
        override fun onNativeAdLoaded(nativeAdView: MaxNativeAdView?, nativeAdMax: MaxAd) {
            // Clean up any pre-existing native ad to prevent memory leaks.
            if (loadedNativeAd != null) {
                nativeAdLoader?.destroy(loadedNativeAd)
            }

            nativeAd = nativeAdMax
            // Save ad for cleanup.
            loadedNativeAd = nativeAd
            nativeAdContainerView?.removeAllViews()
            nativeAdContainerView?.addView(nativeAdView)
        }

        override fun onNativeAdLoadFailed(adUnitId: String, error: MaxError) {
            // Native ad load failed.
            // AppLovin recommends retrying with exponentially higher delays up to a maximum delay.
        }

        override fun onNativeAdClicked(nativeAd: MaxAd) {}
    }

    /**
     * Add navitve ad to view
     */
    fun loadAndShowNativeAd(
        activity: AppCompatActivity,
        adType: AdType,
        containerView: FrameLayout,
    ) {
        nativeAdContainerView = containerView
        containerView.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT)
            .apply { gravity = Gravity.CENTER and Gravity.CENTER_VERTICAL }
        nativeAdLoader = MaxNativeAdLoader(activity.getString(adType.adsId), activity)
        nativeAdLoader?.setRevenueListener(listenerRevenue)
        nativeAdLoader?.setNativeAdListener(NativeAdListener())
        nativeAdLoader?.loadAd()

        activity.lifecycle.addObserver(object : DefaultLifecycleObserver {

            override fun onDestroy(owner: LifecycleOwner) {
                super.onDestroy(owner)
                Timber.d("OnDestroy native")
                if (nativeAd != null) {
                    nativeAdLoader?.destroy(nativeAd)
                }
                nativeAdLoader?.destroy()
            }
        })

    }

    private var adViewMREC: MaxAdView? = null
    private var listenerAdViewMRECAndBanner = object : MaxAdViewAdListener {
        override fun onAdLoaded(ad: MaxAd?) {}
        override fun onAdDisplayed(ad: MaxAd?) {}
        override fun onAdHidden(ad: MaxAd?) {}
        override fun onAdClicked(ad: MaxAd?) {}

        override fun onAdLoadFailed(adUnitId: String?, error: MaxError?) {
            Timber.d("onAdLoadFailed: adUnitId:" + adUnitId + " Error:" + error?.message)
        }

        override fun onAdDisplayFailed(ad: MaxAd?, error: MaxError?) {
            Timber.d("onAdDisplayFailed: Error:" + error?.message)
        }

        override fun onAdExpanded(ad: MaxAd?) {}
        override fun onAdCollapsed(ad: MaxAd?) {}
    }

    /**
     * Add Banner MREC medium add to view
     */
    fun loadAndShowMREC(activity: AppCompatActivity, adType: AdType, containerView: FrameLayout) {
        containerView.removeAllViews()
        containerView.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT)
            .apply { gravity = Gravity.CENTER and Gravity.CENTER_VERTICAL }
        adViewMREC = MaxAdView(activity.getString(adType.adsId), MaxAdFormat.MREC, activity)
        adViewMREC?.id = ViewCompat.generateViewId()
        adViewMREC?.setListener(listenerAdViewMRECAndBanner)
        adViewMREC?.setRevenueListener(listenerRevenue)
        val widthPx = 300.toPx
        val heightPx = 250.toPx
        adViewMREC?.layoutParams = FrameLayout.LayoutParams(widthPx, heightPx).apply { gravity = Gravity.CENTER and Gravity.CENTER_VERTICAL }
        adViewMREC?.setBackgroundColor(ContextCompat.getColor(activity, R.color.grayA01))
        adViewMREC?.startAutoRefresh()
        containerView.addView(adViewMREC)
        adViewMREC?.loadAd()
    }

    private var adViewBanner: MaxAdView? = null

    /**
     * Add Banner small add to view
     */
    fun loadAndShowBanner(activity: AppCompatActivity, adType: AdType, containerView: FrameLayout) {
        containerView.removeAllViews()
        containerView.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT)
            .apply { gravity = Gravity.CENTER and Gravity.CENTER_VERTICAL }
        adViewBanner = MaxAdView(activity.getString(adType.adsId), activity)
        adViewBanner?.id = ViewCompat.generateViewId()
        adViewBanner?.setListener(listenerAdViewMRECAndBanner)
        adViewBanner?.setRevenueListener(listenerRevenue)
        val isTablet = AppLovinSdkUtils.isTablet(activity)
        val heightPx = AppLovinSdkUtils.dpToPx(activity, if (isTablet) 90 else 50)
        adViewBanner?.layoutParams =
            FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, heightPx).apply { gravity = Gravity.CENTER and Gravity.CENTER_VERTICAL }
        adViewBanner?.setBackgroundColor(ContextCompat.getColor(activity, R.color.grayA01))
        adViewBanner?.startAutoRefresh()
        containerView.addView(adViewBanner)
        adViewBanner?.loadAd()
    }

    private var rewardedAd: MaxRewardedAd? = null
    private var retryAttemptRewardedAd = 0

    /**
     * tự động load và show quảng cáo RewardedAd
     */
    fun loadAndShowMaxRewardedAd(
        activity: AppCompatActivity,
        adType: AdType,
        userGetRewarded: () -> Unit?,
        userNoRewarded: () -> Unit?,
    ) {
        var userRewarded = false
        var autoShow = false
        val listener = object : MaxRewardedAdListener {
            // MAX Ad Listener
            override fun onAdLoaded(maxAd: MaxAd) {
                // Rewarded ad is ready to be shown. rewardedAd.isReady() will now return 'true'
                // Reset retry attempt
                retryAttemptRewardedAd = 0
                Timber.d("onAdLoaded autoShow $autoShow")
                if (autoShow) {
                    autoShow = false
                    showMaxRewardedAd()
                }

            }

            override fun onAdLoadFailed(adUnitId: String?, error: MaxError?) {
                // Rewarded ad failed to load
                // We recommend retrying with exponentially higher delays up to a maximum delay (in this case 64 seconds)

//                retryAttemptRewardedAd++
//                val delayMillis = TimeUnit.SECONDS.toMillis(2.0.pow(6.coerceAtMost(
//                    retryAttemptRewardedAd)).toLong())
//                Handler(Looper.getMainLooper()).postDelayed({ rewardedAd?.loadAd() },
//                    delayMillis)
                Timber.d("onAdLoadFailed: adUnitId:" + adUnitId + " error:" + error?.message)
                userNoRewarded.invoke()
            }

            override fun onAdDisplayFailed(ad: MaxAd?, error: MaxError?) {
                // Rewarded ad failed to display. We recommend loading the next ad
//                rewardedAd?.loadAd()
                Timber.d("onAdLoadFailed: " + " error:" + error?.message)
            }

            override fun onAdDisplayed(maxAd: MaxAd) {}

            override fun onAdClicked(maxAd: MaxAd) {}

            override fun onAdHidden(maxAd: MaxAd) {
                // rewarded ad is hidden. Pre-load the next ad
//                rewardedAd?.loadAd()
                if (userRewarded) {
                    userGetRewarded.invoke()
                } else userNoRewarded.invoke()
            }

            override fun onRewardedVideoStarted(maxAd: MaxAd) {}

            override fun onRewardedVideoCompleted(maxAd: MaxAd) {}

            override fun onUserRewarded(maxAd: MaxAd, maxReward: MaxReward) {
                // Rewarded ad was displayed and user should receive the reward
                userRewarded = true

            }
        }
//        if (rewardedAd?.isReady == false) {
        Timber.d("loadAndShowMaxRewardedAd isReady false")
        autoShow = true
        rewardedAd = MaxRewardedAd.getInstance(activity.getString(adType.adsId), activity)
        rewardedAd?.setRevenueListener(listenerRevenue)
        rewardedAd?.setListener(listener)
        rewardedAd?.loadAd()
//        } else {
//            Timber.d("loadAndShowMaxRewardedAd isReady true")
////            rewardedAd?.setRevenueListener(listenerRevenue)
////            rewardedAd?.setListener(listener)
//            rewardedAd?.showAd()
//        }

    }

    private fun showMaxRewardedAd() {
        Timber.d("showMaxRewardedAd:")
        if (rewardedAd?.isReady == true) {
            rewardedAd?.showAd()
        }
    }


    override fun onAdLoaded(ad: MaxAd?) {
        mAdLoadCallback?.onLoaded()
        isLoading = false

        retryAttempt = 0.0
    }

    override fun onAdDisplayed(ad: MaxAd?) {
        Timber.d("onAdDisplayed")
//        mAdDisplayCallback?.onImpression()
        isLoading = false
    }

    override fun onAdHidden(ad: MaxAd?) {
        Timber.d("onAdHidden")
        mAdDisplayCallback?.onDisplayed()
        maxInterstitialAd?.loadAd()
        removeListener()
    }

    override fun onAdClicked(ad: MaxAd?) {
        Timber.d("adClick")
    }

    override fun onAdLoadFailed(adUnitId: String?, error: MaxError?) {
//        mAdLoadCallback?.onLoadFailed()
        mAdDisplayCallback?.onDisplayed()
        removeListener()
        retryAttempt++
        val delayMillis =
            TimeUnit.SECONDS.toMillis(2.0.pow(6.0.coerceAtMost(retryAttempt)).toLong())
        Handler(Looper.getMainLooper()).postDelayed({ maxInterstitialAd?.loadAd() }, delayMillis)
    }

    override fun onAdDisplayFailed(ad: MaxAd?, error: MaxError?) {
        maxInterstitialAd?.loadAd()
        mAdDisplayCallback?.onDisplayed()
        removeListener()
    }


}