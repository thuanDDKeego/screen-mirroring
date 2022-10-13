package com.abc.mirroring.ads

import AdConfig
import AdType
import android.app.Activity
import android.content.Context
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.abc.mirroring.R
import com.abc.mirroring.config.AppConfigRemote
import com.abc.mirroring.config.AppPreferences
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.*
import timber.log.Timber
import kotlin.coroutines.coroutineContext

class AdmobHelper {

    private val adRequest: AdRequest
        get() {
            return AdRequest.Builder().build()
        }
    val adsInterstitial = HashMap<AdType, InterstitialAd?>()
    val adsRewarded = HashMap<AdType, RewardedAd?>()

    fun resetInterstitialAd(type: AdType) {
        adsInterstitial[type] = null
    }

    fun loadAdBanner(
        mAdView: AdView,
    ) {
        mAdView.loadAd(adRequest)
        mAdView.adListener = object : AdListener() {
            override fun onAdClicked() {}

            override fun onAdClosed() {}

            override fun onAdFailedToLoad(adError: LoadAdError) {}

            override fun onAdImpression() {}

            override fun onAdLoaded() {}

            override fun onAdOpened() {}
        }
    }


    fun loadAdInterstitial(
        context: Context,
        type: AdType,
        callback: (mInterstitialAd: InterstitialAd?) -> Unit,
    ) {
        if (AppPreferences().isPremiumActive == true) {
            callback(null)
        } else {
            var showAds = true
            var job: Job? = null
            val timeout = AppConfigRemote().adsTimeout ?: 0
            if (timeout != 0) {
                job = CoroutineScope(Dispatchers.Main).launch {
                    delay(timeout.toLong())
                    showAds = false
                    callback(null)
                }
            }
            if (adsInterstitial[type] != null && showAds) {
                callback(adsInterstitial[type])
                job?.cancel()
                return
            }
            InterstitialAd.load(context, context.getString(type.adsId), adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(mInterstitialAd: InterstitialAd) {
                        Timber.d("====onLoaded $mInterstitialAd")
                        super.onAdLoaded(mInterstitialAd)
                        adsInterstitial[type] = mInterstitialAd
                        if (showAds) {
                            callback(adsInterstitial[type])
                            adsInterstitial[type] = null
                            job?.cancel()
                        }
                    }

                    override fun onAdFailedToLoad(mInterstitialAd: LoadAdError) {
                        Timber.d("====onLoad failed${mInterstitialAd}")
                        super.onAdFailedToLoad(mInterstitialAd)
                        adsInterstitial[type] = null
                        if (showAds) {
                            callback(adsInterstitial[type])
                            job?.cancel()
                        }
                    }
                })
        }
    }

    fun showAdInterstitial(
        context: Context,
        type: AdType,
        callback: () -> Unit,
    ) {
        if (AppPreferences().isPremiumActive == true) {
            callback()
        } else {
            Timber.d("====show ${adsInterstitial[type]}")
            val fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent()
                    callback()
                    adsInterstitial[type] = null
                    Timber.d("adsInterstitial ${adsInterstitial[type]}")
                    loadAdInterstitial(context, type) {}
                }

                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    super.onAdFailedToShowFullScreenContent(p0)
                    callback()
                    adsInterstitial[type] = null
                    Timber.d("adsInterstitial ${adsInterstitial[type]}")
                    loadAdInterstitial(context, type) {}

                }
            }
            if (adsInterstitial[type] != null) {
                Timber.d("adsInterstitial ${adsInterstitial[type]}")
                adsInterstitial[type]?.fullScreenContentCallback = fullScreenContentCallback
                adsInterstitial[type]?.show(context as Activity)
            } else {
                loadAdInterstitial(context, type) {
                    if (adsInterstitial[type] != null) {
                        adsInterstitial[type]?.fullScreenContentCallback = fullScreenContentCallback
                        adsInterstitial[type]?.show(context as Activity)
                    } else {
                        callback()
                    }
                }
            }
        }
    }

    private fun populateUnifiedNativeAdView(
        context: Context,
        nativeAd: NativeAd,
        adView: NativeAdView,
        haveIcon: Boolean = false,
    ) {
        Timber.d("nativeAd $nativeAd")
        adView.mediaView = adView.findViewById(R.id.ad_media)
        if (haveIcon) {
            adView.iconView = adView.findViewById(R.id.ad_icon)
            if (adView.iconView != null) {
                (adView.iconView as ImageView).setImageDrawable(nativeAd.icon?.drawable)
            }
        }
        if (adView.mediaView != null) {
            adView.mediaView?.postDelayed({
                if (AppGlobal.BUILD_DEBUG) {
                    val sizeMin = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 120f,
                        context.resources?.displayMetrics
                    )
//                    if ((adView.mediaView?.width
//                            ?: 0) < sizeMin || (adView.mediaView?.height ?: 0) < sizeMin
//                    ) {
//                        Toast.makeText(context, "Size media native not valid", Toast.LENGTH_SHORT)
//                            .show()
//                    }
                }
            }, 1000)
        }
        // Set other ad assets.
        adView.headlineView = adView.findViewById(R.id.ad_headline)
        adView.bodyView = adView.findViewById(R.id.ad_body)
        adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)

        // The headline is guaranteed to be in every UnifiedNativeAd.
        try {
            (adView.headlineView as TextView).text = nativeAd.headline
        } catch (e: Exception) {
            Log.e("nativeAdview", e.toString())
            e.printStackTrace()
        }

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        try {
            if (nativeAd.body == null) {
                adView.bodyView?.visibility = View.INVISIBLE
            } else {
                adView.bodyView?.visibility = View.VISIBLE
                (adView.bodyView as TextView).text = nativeAd.body
            }
        } catch (e: Exception) {
            Log.e("nativeAdview", e.toString())
            e.printStackTrace()
        }
        try {
            if (nativeAd.callToAction == null) {
                adView.callToActionView?.visibility = View.INVISIBLE
            } else {
                adView.callToActionView?.visibility = View.VISIBLE
                (adView.callToActionView as TextView).text = nativeAd.callToAction
            }
        } catch (e: Exception) {
            Log.e("nativeAdview", e.toString())
            e.printStackTrace()
        }
        adView.setNativeAd(nativeAd)
    }

    fun showNativeAdmob(
        context: Context,
        adType: AdType,
        adView: NativeAdView,
        haveIcon: Boolean = false,
    ) {
        val builder = AdLoader.Builder(context, context.getString(adType.adsId))
        builder.forNativeAd {
            Log.d("showNativeAdmob", "forNativeAd $it")
            populateUnifiedNativeAdView(context, it, adView, haveIcon)
        }
        val videoOptions = VideoOptions.Builder()
            .setStartMuted(true)
            .build()
        val adOptions = NativeAdOptions.Builder()
            .setVideoOptions(videoOptions)
            .build()
        builder.withNativeAdOptions(adOptions)
        val adLoader = builder.withAdListener(object : AdListener() {
            override fun onAdFailedToLoad(errorCode: LoadAdError) {
            }
        }).build()
        adLoader.loadAd(adRequest)
    }

    fun loadRewardedAds(
        context: Context,
        type: AdType, callback: (mRewardedAd: RewardedAd?) -> Unit,
    ) {
        var showAds = true
        var job: Job? = null
        val timeout = AppConfigRemote().adsTimeout ?: 0
        if (timeout != 0) {
            job = CoroutineScope(Dispatchers.Main).launch {
                delay(timeout.toLong())
                showAds = false
                callback(null)
            }
        }
        if (adsRewarded[type] != null && showAds) {
            callback.invoke(adsRewarded[type])
            job?.cancel()
            return
        }
        RewardedAd.load(
            context,
            context.getString(type.adsId),
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    adsRewarded[type] = null
                    if (showAds) {
                        callback(null)
                        job?.cancel()
                    }
                }

                override fun onAdLoaded(rewardedAd: RewardedAd) {
                    adsRewarded[type] = rewardedAd
                    if (showAds) {
                        callback(adsRewarded[type])
                        job?.cancel()
                    }
                }
            })
    }

    fun showRewardedAds(context: Context, type: AdType, callback: (Boolean) -> Unit) =
        if (AppPreferences().isPremiumActive == true) {
            callback.invoke(true)
        } else {
//            if (adsRewarded[type] != null) {
            var isSuccess = false
            val fullScreenCall = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent()
                    callback(isSuccess)
                    adsRewarded[type] = null
                }

                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    super.onAdFailedToShowFullScreenContent(p0)
                    isSuccess = false
                    callback(isSuccess)
                    adsRewarded[type] = null
                }
            }
            val onUserEarnedRewardListener = OnUserEarnedRewardListener { // User earned reward.
                isSuccess = true
            }
            loadRewardedAds(context, AdType.BROWSER_MIRROR_REWARD) {
                if (it != null) {
                    it.apply {
                        fullScreenContentCallback = fullScreenCall
                        show(context as Activity, onUserEarnedRewardListener)
                    }
                } else {
                    callback(false)
                }
            }
        }
//        else {
//                loadRewardedAds(context, type){}
//                adsRewarded[type] = null
//                callback.invoke(false)
//            }

//    companion object {
//        @SuppressLint("StaticFieldLeak")
//        @Volatile
//        private var INSTANCE: AdmobHelper? = null
//
//        @JvmStatic
//        @get:Synchronized
//        val instance: AdmobHelper?
//            get() {
//                if (INSTANCE == null) {
//                    INSTANCE = AdmobHelper()
//                }
//                return INSTANCE
//            }
//    }
}