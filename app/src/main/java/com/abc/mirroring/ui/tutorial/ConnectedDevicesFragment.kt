package com.abc.mirroring.ui.tutorial

import android.view.LayoutInflater
import android.view.ViewGroup
import com.abc.mirroring.base.BaseFragment
import com.abc.mirroring.databinding.FragmentConnectedDevicesBinding
import com.abc.mirroring.utils.FirebaseTracking

class ConnectedDevicesFragment : BaseFragment<FragmentConnectedDevicesBinding>() {

    override fun initBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentConnectedDevicesBinding.inflate(inflater, container, false)

    override fun initViews() {
        FirebaseTracking.logHelpDevicesShowed()
    }

    override fun initActions() {
    }

}