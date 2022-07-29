package com.abc.mirroring.ui.home.uiadbanner

import android.view.LayoutInflater
import android.view.ViewGroup
import com.abc.mirroring.base.BaseFragment
import com.abc.mirroring.databinding.FragmentAdBanner1Binding

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