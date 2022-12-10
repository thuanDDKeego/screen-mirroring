package com.abc.mirroring.ui.devicemirror

import AdType
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.*
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.abc.mirroring.R
import com.abc.mirroring.ads.AdmobHelper
import com.abc.mirroring.ads.AppOpenManager
import com.abc.mirroring.base.BaseActivity
import com.abc.mirroring.config.AppConfigRemote
import com.abc.mirroring.config.AppPreferences
import com.abc.mirroring.databinding.ActivityDeviceMirrorBinding
import com.abc.mirroring.ui.dialog.DialogCenter
import com.abc.mirroring.ui.home.HomeActivity
import com.abc.mirroring.ui.tutorial.TutorialActivity
import com.abc.mirroring.utils.FirebaseTracking
import com.abc.mirroring.utils.Global
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class DeviceMirrorActivity : BaseActivity<ActivityDeviceMirrorBinding>() {
    private lateinit var networkRequest: NetworkRequest
    private var connectedWifi = false

    @Inject
    lateinit var admobHelper: AdmobHelper
    private lateinit var dialogCenter: DialogCenter

    companion object {
        fun gotoActivity(activity: Activity) {
            val intent = Intent(activity, DeviceMirrorActivity::class.java)
            activity.startActivity(intent)
        }
    }

    override fun initBinding() = ActivityDeviceMirrorBinding.inflate(layoutInflater)

    override fun initViews() {
        dialogCenter = DialogCenter(this)
        dialogCenter.admobHelper = admobHelper
        FirebaseTracking.logMirrorSelectDeviceShowed()
        val connManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        if (networkInfo?.detailedState == NetworkInfo.DetailedState.DISCONNECTED) {
            setupCheckWifiConnectView(false)
        }

//        networkRequest = NetworkRequest.Builder()
//            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
//            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
//            .build()
//
//        val connectivityManager =
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                getSystemService(ConnectivityManager::class.java) as ConnectivityManager
//            } else {
//
//            }
//        connectivityManager.requestNetwork(networkRequest, networkCallback)
        AppOpenManager.instance?.enableAddWithActivity(DeviceMirrorActivity::class.java)
    }

    override fun onResume() {
        super.onResume()
        observerWifiState(object : onWifiChangeStateConnection {
            override fun onWifiUnavailable() {
                    setupCheckWifiConnectView(false)
            }

            override fun onWifiAvailable() {
                setupCheckWifiConnectView(true)
            }
        })
    }

    override fun initAdmob() {
        if(AppConfigRemote().turnOnTopNativeDeviceMirror == true && AppPreferences().isPremiumSubscribed == false) {
            binding.containerAd.visibility = View.VISIBLE
            admobHelper.showNativeAdmob(
                this@DeviceMirrorActivity,
                AdType.MIRROR_DEVICE_NATIVE,
                binding.admobNativeView.nativeAdView,
                true
            )
        } else {
            binding.containerAd.visibility = View.GONE
        }
    }

    override fun initActions() {
        binding.apply {
            btnSelectDevice.setOnClickListener {
                AppOpenManager.instance?.disableAddWithActivity(DeviceMirrorActivity::class.java)
                selectDeviceMirror()
            }
            btnGoToWifiSetting.setOnClickListener {
                startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
            }
            btnBack.setOnClickListener { onBackPressed() }
        }
        binding.imgHelp.setOnClickListener {
            FirebaseTracking.logHomeIconHelpClicked()
            TutorialActivity.gotoActivity(this@DeviceMirrorActivity)
        }
        binding.imgBattery.setOnClickListener {
            dialogCenter.showDialog(DialogCenter.DialogType.StopOptimizeBattery)
        }
    }

//    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
//        // network is available for use
//        override fun onAvailable(network: Network) {
//            super.onAvailable(network)
//            setupCheckWifiConnectView(true)
//        }
//
//        // lost network connection
//        override fun onLost(network: Network) {
//            super.onLost(network)
//            setupCheckWifiConnectView(false)
//        }
//    }

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
            delay(800)
            binding.progressCheckConnectionDistance.visibility = View.INVISIBLE
            if (isConnected) {
                binding.imgResultCheckConnectionDistance.visibility = View.VISIBLE
                binding.imgResultCheckConnectionDistance.background =
                    ContextCompat.getDrawable(this@DeviceMirrorActivity, R.drawable.ic_tick)
//                    resources.getDrawable(R.drawable.ic_tick)
                delay(600)
                binding.imgResultCheckSignalTransmission.visibility = View.VISIBLE
                binding.progressCheckSignalTransmission.visibility = View.INVISIBLE
                binding.imgResultCheckSignalTransmission.background =
                    ContextCompat.getDrawable(this@DeviceMirrorActivity, R.drawable.ic_tick)
                delay(400)
                binding.imgResultTestConnectionSpeed.visibility = View.VISIBLE
                binding.progressTestConnectionSpeed.visibility = View.INVISIBLE
                binding.imgResultTestConnectionSpeed.background =
                    ContextCompat.getDrawable(this@DeviceMirrorActivity, R.drawable.ic_tick)
                delay(100)
                binding.btnGoToWifiSetting.visibility = View.GONE
                binding.btnSelectDevice.visibility = View.VISIBLE
            } else {
                binding.imgResultCheckConnectionDistance.visibility = View.VISIBLE
                binding.imgResultCheckConnectionDistance.background =
                    ContextCompat.getDrawable(this@DeviceMirrorActivity, R.drawable.ic_x_error)
                delay(200)
                binding.imgResultCheckSignalTransmission.visibility = View.VISIBLE
                binding.progressCheckSignalTransmission.visibility = View.INVISIBLE
                binding.imgResultCheckSignalTransmission.background =
                    ContextCompat.getDrawable(this@DeviceMirrorActivity, R.drawable.ic_x_error)
                delay(200)
                binding.imgResultTestConnectionSpeed.visibility = View.VISIBLE
                binding.progressTestConnectionSpeed.visibility = View.INVISIBLE
                binding.imgResultTestConnectionSpeed.background =
                    ContextCompat.getDrawable(this@DeviceMirrorActivity, R.drawable.ic_x_error)
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

    override fun onBackPressed() {
        val returnIntent = Intent()
        returnIntent.putExtra(HomeActivity.SHOW_RATING_DIALOG, true)
        setResult(RESULT_OK, returnIntent)
        super.onBackPressed()
    }

}

