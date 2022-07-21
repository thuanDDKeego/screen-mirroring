package com.abc.sreenmirroring.ui.devicemirror

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.*
import android.provider.Settings
import android.view.View
import android.widget.Toast
import com.abc.sreenmirroring.R
import com.abc.sreenmirroring.base.BaseActivity
import com.abc.sreenmirroring.databinding.ActivityDeviceMirrorBinding
import com.abc.sreenmirroring.utils.Global
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DeviceMirrorActivity : BaseActivity<ActivityDeviceMirrorBinding>() {
    private lateinit var networkRequest: NetworkRequest
    private var connectedWifi = false

    companion object {
        fun gotoActivity(activity: Activity) {
            val intent = Intent(activity, DeviceMirrorActivity::class.java)
            activity.startActivity(intent)
        }
    }

    override fun initBinding() = ActivityDeviceMirrorBinding.inflate(layoutInflater)

    override fun initViews() {
        networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()

        val connectivityManager = getSystemService(ConnectivityManager::class.java) as ConnectivityManager
        connectivityManager.requestNetwork(networkRequest, networkCallback)
    }

    override fun initActions() {
        binding.btnSelectDevice.setOnClickListener {
            selectDeviceMirror()
        }
        binding.btnGoToWifiSetting.setOnClickListener {
            startActivity( Intent(Settings.ACTION_WIFI_SETTINGS));
        }
    }

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        // network is available for use
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            setupCheckWifiConnectView(true)
        }

        // Network capabilities have changed for the network

        // lost network connection
        override fun onLost(network: Network) {
            super.onLost(network)
            setupCheckWifiConnectView(false)
        }
    }

//    override fun onResume() {
//        super.onResume()
//        setupCheckWifiConnectView()
//    }

    private fun resetWifiLoadingView() {
        binding.progressCheckConnectionDistance.visibility = View.VISIBLE
        binding.progressCheckSignalTransmission.visibility = View.VISIBLE
        binding.progressTestConnectionSpeed.visibility = View.VISIBLE
        binding.imgResultCheckConnectionDistance.visibility = View.INVISIBLE
        binding.imgResultCheckSignalTransmission.visibility = View.INVISIBLE
        binding.imgResultTestConnectionSpeed.visibility = View.INVISIBLE
    }

    private fun setupCheckWifiConnectView(isConnected: Boolean) {
        CoroutineScope(Dispatchers.Main).launch {
            resetWifiLoadingView()
            delay(2000)
            binding.progressCheckConnectionDistance.visibility = View.INVISIBLE
            if (isConnected) {
                binding.imgResultCheckConnectionDistance.visibility = View.VISIBLE
                binding.imgResultCheckConnectionDistance.background =
                    resources.getDrawable(R.drawable.ic_tick)
                delay(800)
                binding.imgResultCheckSignalTransmission.visibility = View.VISIBLE
                binding.progressCheckSignalTransmission.visibility = View.INVISIBLE
                binding.imgResultCheckSignalTransmission.background =
                    resources.getDrawable(R.drawable.ic_tick)
                delay(600)
                binding.imgResultTestConnectionSpeed.visibility = View.VISIBLE
                binding.progressTestConnectionSpeed.visibility = View.INVISIBLE
                binding.imgResultTestConnectionSpeed.background =
                    resources.getDrawable(R.drawable.ic_tick)
                delay(100)
                binding.btnGoToWifiSetting.visibility = View.GONE
                binding.btnSelectDevice.visibility = View.VISIBLE
            } else {
                binding.imgResultCheckConnectionDistance.visibility = View.VISIBLE
                binding.imgResultCheckConnectionDistance.background =
                    resources.getDrawable(R.drawable.ic_x_error)
                delay(200)
                binding.imgResultCheckSignalTransmission.visibility = View.VISIBLE
                binding.progressCheckSignalTransmission.visibility = View.INVISIBLE
                binding.imgResultCheckSignalTransmission.background =
                    resources.getDrawable(R.drawable.ic_x_error)
                delay(200)
                binding.imgResultTestConnectionSpeed.visibility = View.VISIBLE
                binding.progressTestConnectionSpeed.visibility = View.INVISIBLE
                binding.imgResultTestConnectionSpeed.background =
                    resources.getDrawable(R.drawable.ic_x_error)
                delay(100)
                binding.btnSelectDevice.visibility = View.GONE
                binding.btnGoToWifiSetting.visibility = View.VISIBLE
            }
        }

    }


    private fun selectDeviceMirror() {
        try {
            Global.SELECT_FROM_SETTING = true
            startActivity(Intent("android.settings.WIFI_DISPLAY_SETTINGS"))
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
            try {
                Global.SELECT_FROM_SETTING = true
                startActivity(Intent("com.samsung.wfd.LAUNCH_WFD_PICKER_DLG"))
            } catch (e2: java.lang.Exception) {
                try {
                    Global.SELECT_FROM_SETTING = true
                    startActivity(Intent("android.settings.CAST_SETTINGS"))
                } catch (e3: java.lang.Exception) {
                    Toast.makeText(
                        this@DeviceMirrorActivity,
                        "not support device",
                        Toast.LENGTH_LONG
                    )
                        .show()
                }
            }
        }
    }

}

