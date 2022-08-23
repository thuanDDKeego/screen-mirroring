package com.abc.mirroring.ui.tutorial

import AdType
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import com.abc.mirroring.ads.AdmobHelper
import com.abc.mirroring.ads.ApplovinUtils
import com.abc.mirroring.base.BaseFragment
import com.abc.mirroring.config.AppConfigRemote
import com.abc.mirroring.databinding.FragmentConnectedDevicesBinding
import com.abc.mirroring.ui.tutorial.adapter.DeviceItemAdapter
import com.abc.mirroring.utils.FirebaseTracking
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ConnectedDevicesFragment : BaseFragment<FragmentConnectedDevicesBinding>() {
    @Inject
    lateinit var admobHelper: AdmobHelper
    private lateinit var adapter: DeviceItemAdapter
    private val viewModel by viewModels<TutorialViewModel>()
    override fun initBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ) = FragmentConnectedDevicesBinding.inflate(inflater, container, false)

    override fun initViews() {
        FirebaseTracking.logHelpDevicesShowed()
        adapter = DeviceItemAdapter(requireActivity(), viewModel.getDeviceItem(requireActivity()))
        binding.recyclerViewDevice.adapter = adapter
    }

    override fun initActions() {
    }

    override fun showAds() {

        if (AppConfigRemote().isUsingAdmobNative == true) {
            admobHelper.showNativeAdmob(
                requireActivity(),
                AdType.CONNECT_DEVICE_NATIVE,
                binding.nativeAdView.nativeAdView,
                true
            )

        } else ApplovinUtils.getInstance()
            .loadAndShowBanner(requireActivity() as AppCompatActivity,
                AdType.APPLOVIN_NATIVE_MEDIUM,
                binding.containerAd)
    }

}