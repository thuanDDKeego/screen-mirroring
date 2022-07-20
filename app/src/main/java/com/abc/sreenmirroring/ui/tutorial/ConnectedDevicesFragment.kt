package com.abc.sreenmirroring.ui.tutorial

import android.view.LayoutInflater
import android.view.ViewGroup
import com.abc.sreenmirroring.base.BaseFragment
import com.abc.sreenmirroring.databinding.FragmentConnectedDevicesBinding

class ConnectedDevicesFragment : BaseFragment<FragmentConnectedDevicesBinding>() {

    override fun initBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentConnectedDevicesBinding.inflate(inflater, container, false)

    override fun initViews() {
    }

    override fun initActions() {
    }

}