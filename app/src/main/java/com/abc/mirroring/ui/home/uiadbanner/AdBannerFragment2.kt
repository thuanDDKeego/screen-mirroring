package com.abc.mirroring.ui.home.uiadbanner

import AdType
import android.view.LayoutInflater
import android.view.ViewGroup
import com.abc.mirroring.ads.AdmobHelper
import com.abc.mirroring.base.BaseFragment
import com.abc.mirroring.databinding.FragmentAdBanner2Binding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AdBannerFragment2 : BaseFragment<FragmentAdBanner2Binding>() {

    @Inject
    lateinit var admobHelper: AdmobHelper
    override fun initBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentAdBanner2Binding.inflate(inflater, container, false)

    override fun initViews() {
    }

    override fun initActions() {
    }

    override fun showAds() {
//        admobHelper.showNativeAdmob(
//            requireContext(),
//            AdType.HOME_NATIVE,
//            binding.admobNativeView.nativeAdView
//        )
    }
}