package com.abc.mirroring.ui.tutorial

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.abc.mirroring.base.BaseFragment
import com.abc.mirroring.databinding.FragmentConnectedDevicesBinding
import com.abc.mirroring.ui.tutorial.adapter.DeviceItemAdapter
import com.abc.mirroring.utils.FirebaseTracking

class ConnectedDevicesFragment : BaseFragment<FragmentConnectedDevicesBinding>() {
    private lateinit var adapter: DeviceItemAdapter
    private val viewModel by viewModels<TutorialViewModel>()
    override fun initBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentConnectedDevicesBinding.inflate(inflater, container, false)

    override fun initViews() {
        FirebaseTracking.logHelpDevicesShowed()
        adapter = DeviceItemAdapter(requireActivity(), viewModel.getDeviceItem(requireActivity()))
        Log.i("binding FAQ", binding.toString())
        binding.recyclerViewDevice.adapter = adapter
    }

    override fun initActions() {
    }

}