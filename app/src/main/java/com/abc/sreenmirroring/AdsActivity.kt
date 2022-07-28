package com.abc.sreenmirroring

import AdType
import com.abc.sreenmirroring.ads.AdmobHelper
import com.abc.sreenmirroring.base.BaseActivity
import com.abc.sreenmirroring.databinding.ActivityAdsBinding
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
            admobHelper.showAdInterstitial(this, AdType.GALLERY_INTERSTITIAL) {}
        }
        binding.btnRewardIntersitialAd.setOnClickListener {
            Timber.d("====showAdmob")
            admobHelper.showRewardedAds(this, AdType.BROWSER_MIRROR_REWARD) {}
        }
    }

    override fun initAdmob() {
        admobHelper.loadAdInterstitial(this, AdType.GALLERY_INTERSTITIAL) {}
        admobHelper.loadRewardedAds(this, AdType.BROWSER_MIRROR_REWARD) {}
        admobHelper.showNativeAdmob(this, AdType.HOME_NATIVE, binding.admobNativeView.nativeAdView)
        //ad banner
        admobHelper.loadAdBanner(binding.adBanner.adView)
    }


}