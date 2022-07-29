package com.abc.mirroring

import AdType
import com.abc.mirroring.ads.AdmobHelper
import com.abc.mirroring.base.BaseActivity
import com.abc.mirroring.databinding.ActivityAdsBinding
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class AdsActivity : BaseActivity<ActivityAdsBinding>() {
    @Inject
    lateinit var admobHelper: AdmobHelper
    override fun initBinding() = ActivityAdsBinding.inflate(layoutInflater)
    override fun initViews() {

    }

    override fun initActions() {
        binding.btnInterstitialAd.setOnClickListener {
            Timber.d("====showAdmob")
            admobHelper.showAdInterstitial(this, AdType.BACK_FROM_TUTORIAL_INTERSTITIAL) {}
        }
        binding.btnRewardIntersitialAd.setOnClickListener {
            Timber.d("====showAdmob")
            admobHelper.showRewardedAds(this, AdType.BROWSER_MIRROR_REWARD) {}
        }
    }

    override fun initAdmob() {
        admobHelper.loadAdInterstitial(this, AdType.BACK_FROM_TUTORIAL_INTERSTITIAL) {}
        admobHelper.loadRewardedAds(this, AdType.BROWSER_MIRROR_REWARD) {}
        admobHelper.showNativeAdmob(this, AdType.HOME_NATIVE, binding.admobNativeView.nativeAdView)
        //ad banner
        admobHelper.loadAdBanner(binding.adBanner.adView)
    }


}