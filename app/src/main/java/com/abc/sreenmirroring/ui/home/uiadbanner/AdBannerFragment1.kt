package com.abc.sreenmirroring.ui.home.uiadbanner

import android.view.LayoutInflater
import android.view.ViewGroup
import com.abc.sreenmirroring.base.BaseFragment
import com.abc.sreenmirroring.databinding.FragmentAdBanner1Binding

class AdBannerFragment1 : BaseFragment<FragmentAdBanner1Binding>() {

    override fun initBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentAdBanner1Binding.inflate(inflater, container, false)

    override fun initViews() {
    }

    override fun initActions() {
    }
}