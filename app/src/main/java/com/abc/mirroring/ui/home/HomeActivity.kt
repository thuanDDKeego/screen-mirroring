package com.abc.mirroring.ui.home

import AdType
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import com.abc.mirroring.R
import com.abc.mirroring.ads.AdmobHelper
import com.abc.mirroring.ads.AppOpenManager
import com.abc.mirroring.base.BaseActivity
import com.abc.mirroring.cast.MainActivity
import com.abc.mirroring.cast.MainActivity.Companion.MEDIA_ROUTE
import com.abc.mirroring.cast.shared.cast.Caster
import com.abc.mirroring.cast.shared.route.MediaRoute
import com.abc.mirroring.config.AppConfigRemote
import com.abc.mirroring.config.AppPreferences
import com.abc.mirroring.databinding.*
import com.abc.mirroring.extentions.setTintColor
import com.abc.mirroring.helper.MY_PERMISSIONS_REQUEST_CAMERA
import com.abc.mirroring.helper.isDrawOverlaysPermissionGranted
import com.abc.mirroring.service.CameraPreviewService
import com.abc.mirroring.service.FloatToolService
import com.abc.mirroring.ui.browsermirror.BrowserMirrorActivity
import com.abc.mirroring.ui.browsermirror.BrowserMirrorActivity.Companion.START_WHEN_RUNNING_REQUEST_CODE
import com.abc.mirroring.ui.devicemirror.DeviceMirrorActivity
import com.abc.mirroring.ui.dialog.DialogCenter
import com.abc.mirroring.ui.feedback.FeedbackActivity
import com.abc.mirroring.ui.home.adapter.TutorialDialogAdapter
import com.abc.mirroring.ui.premium.PremiumActivity2
import com.abc.mirroring.ui.settings.SettingActivity
import com.abc.mirroring.ui.tutorial.TutorialActivity
import com.abc.mirroring.utils.FirebaseLogEvent
import com.abc.mirroring.utils.FirebaseTracking
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class HomeActivity : BaseActivity<ActivityHomeBinding>() {
    private lateinit var goToMirrorActivityResult: ActivityResultLauncher<Intent>
    private lateinit var dialogCenter: DialogCenter
    private var shakeAnimJob: Job? = null

    @Inject
    lateinit var admobHelper: AdmobHelper

    @Inject
    lateinit var caster: Caster

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, HomeActivity::class.java)
        }

        var isStreamingBrowser = MutableLiveData(false)
        val isOpenFloatingToolLiveData = MutableLiveData(FloatToolService.isRunning)
        const val SHOW_RATING_DIALOG = "soRatingDialog"
    }

    override fun initBinding() = ActivityHomeBinding.inflate(layoutInflater)

    override fun initViews() {
        dialogCenter = DialogCenter(this)
        dialogCenter.admobHelper = admobHelper
        initAds()
        FirebaseTracking.logHomeShowed()
        AppPreferences().countTimeOpenApp = AppPreferences().countTimeOpenApp!! + 1
        if (AppPreferences().isTheFirstTimeUseApp == true) {
            AppPreferences().isTheFirstTimeUseApp = false
            dialogCenter.showDialog(
                DialogCenter.DialogType.Tutorial(
                    TutorialDialogAdapter(
                        this,
                        supportFragmentManager
                    )
                )
            )
//            showTutorialDialog()
        } else if (AppPreferences().countTimeOpenApp!! % 3 == 0 && AppPreferences().isPremiumSubscribed == false && AppConfigRemote().enable_premium == true) {
            startActivity(Intent(this@HomeActivity, PremiumActivity2::class.java))
        }

        AppOpenManager.instance?.enableAddWithActivity(HomeActivity::class.java)
        observerConnectingBrowser()
        observerConnectFloatingToolService()
        //set swift mode with floating tools state
//        binding.switchModeFloatingTool.isChecked = FloatToolService.isRunning
        initAnim()
        binding.imgPremium.visibility =
            if (AppConfigRemote().enable_premium == true) View.VISIBLE else View.GONE
    }

    private fun initAnim() {
        val animFade = AnimationUtils.loadAnimation(
            applicationContext,
            R.anim.alpha_scale
        )
        binding.imgBtnConnect.startAnimation(animFade)

        //shake img crown
    }

    private fun initAds() {
        binding.containerAd.visibility = View.GONE
        if (AppConfigRemote().turnOnBottomTutorialNative == true && AppPreferences().isPremiumSubscribed == false) {
            admobHelper.showNativeAdmob(
                this,
                AdType.HOME_NATIVE,
                binding.nativeAdView.nativeAdView,
                true
            ) {
                binding.containerAd.visibility = View.VISIBLE
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (AppPreferences().isPremiumSubscribed == true) {
            hideBannerAds()
            binding.imgPremium.visibility = View.GONE
            binding.imgSaleOffFab.visibility = View.GONE
            shakeAnimJob?.cancel()
            shakeAnimJob = null
        } else {
            //shake img animation
            val shake = AnimationUtils.loadAnimation(this, R.anim.shake)
            shakeAnimJob = CoroutineScope(Dispatchers.IO).launch {
                while (true) {
                    delay(1000L)
                    withContext(Dispatchers.Main) {
                        binding.imgPremium.clearAnimation()
                        binding.imgPremium.startAnimation(shake)
                        if (binding.imgSaleOffFab.isClosed == false) {
                            binding.imgSaleOffFab.clearAnimation()
                            binding.imgSaleOffFab.startAnimation(shake)
                        }
                    }
                    delay(9000L)
                }
            }
        }

        observerWifiState(object : onWifiChangeStateConnection {
            override fun onWifiUnavailable() {
                CoroutineScope(Dispatchers.Main).launch {
                    binding.txtWifiState.text =
                        this@HomeActivity.getString(R.string.wifi_not_connected)
                    binding.imgWifiState.setImageDrawable(
                        ContextCompat.getDrawable(
                            this@HomeActivity,
                            R.drawable.ic_wifi_disconnect
                        )
                    )
                    isStreamingBrowser.value = false
                }
            }

            override fun onWifiAvailable() {
                CoroutineScope(Dispatchers.Main).launch {
                    binding.txtWifiState.text =
                        this@HomeActivity.getString(R.string.wifi_connected)
                    binding.imgWifiState.setImageDrawable(
                        ContextCompat.getDrawable(
                            this@HomeActivity,
                            R.drawable.ic_wifi
                        )
                    )
                }
            }
        })
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun initActions() {
        // Start server streaming
        CoroutineScope(Dispatchers.IO).launch {
            caster.start().also { Timber.i("Caster initialized") }
            caster.discovery.device.collect {
                if (it == null) return@collect

                binding.imgCast.setImageDrawable(
                    ContextCompat.getDrawable(
                        this@HomeActivity,
                        if (it.isConnected) R.drawable.ic_cast_connected else R.drawable.ic_cast
                    )
                )
            }
        }

        goToMirrorActivityResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
                val result = activityResult.resultCode
                val data = activityResult.data
                if (result == RESULT_OK && data != null && AppPreferences().isRated == false) {
                    val isShowRating = data.getBooleanExtra(SHOW_RATING_DIALOG, false)
                    if (isShowRating) dialogCenter.showDialog(DialogCenter.DialogType.Rating { star ->
                        if (star <= 3) {
                            FeedbackActivity.start(this, star)
                        } else {
                            openAppInStore()
                        }
                    })
//                    if (isShowRating) dialogCenter.showRatingDialog { star ->
//                        if (star <= 3) {
//                            FeedbackActivity.start(this, star)
//                        } else {
//                            openAppInStore()
//                        }
//                    }
                }
            }
        binding.constraintBrowserMirror.setOnClickListener {
            FirebaseTracking.log(FirebaseLogEvent.Home_Click_Mirror_to_Web)
            if (isStreamingBrowser.value == true || AppConfigRemote().turnOnHomeBrowserReward == false || AppPreferences().isPremiumSubscribed == true) {
                val intent = Intent(this, BrowserMirrorActivity::class.java)
                startActivityForResult(intent, START_WHEN_RUNNING_REQUEST_CODE)
            } else {
//                dialogCenter.showBrowserDialog()
                dialogCenter.showDialog(DialogCenter.DialogType.Browser)
            }
        }
        binding.constraintMirror.setOnClickListener {
            FirebaseTracking.log(FirebaseLogEvent.Home_Click_Mirror_to_TV)
            val intent = Intent(this@HomeActivity, DeviceMirrorActivity::class.java)
            if (AppConfigRemote().turnOnGoToMirrorDeviceInterstitial == true && AppPreferences().isPremiumSubscribed == false) {
//                dialogCenter.showLoadingAdsDialog()
                dialogCenter.showDialog(DialogCenter.DialogType.LoadingAds)
                admobHelper.showGeneralAdInterstitial(
                    this@HomeActivity,
                ) {
                    dialogCenter.dismissDialog(DialogCenter.DialogType.LoadingAds)
                    goToMirrorActivityResult.launch(intent)
                }
            } else {
                goToMirrorActivityResult.launch(intent)
            }
        }
        binding.imgSetting.setOnClickListener {
            FirebaseTracking.log(FirebaseLogEvent.Home_Click_Setting)
            SettingActivity.gotoActivity(this@HomeActivity)
        }
        binding.imgHelp.setOnClickListener {
            FirebaseTracking.log(FirebaseLogEvent.Home_Click_Help)
            TutorialActivity.gotoActivity(this@HomeActivity)
        }
        binding.switchModeFloatingTool.setOnCheckedChangeListener { _, isChecked ->
            Timber.d("switchMode $isChecked")
            if (isChecked) {
                FirebaseTracking.log(FirebaseLogEvent.Home_Click_Turn_On_Floating_Tools)
                if (isDrawOverlaysPermissionGranted()) {
                    FloatToolService.start(this@HomeActivity)
                } else {
                    FirebaseTracking.log(FirebaseLogEvent.Home_Click_Turn_Off_Floating_Tools)
                    binding.switchModeFloatingTool.isChecked = false
                    dialogCenter.showDialog(DialogCenter.DialogType.AskPermissionOverLay)
//                    showAskPermissionOverlayDialog()
                }
            } else {
                FloatToolService.stop(this@HomeActivity)
            }
        }
        binding.imgPremium.setOnClickListener {
            FirebaseTracking.log(FirebaseLogEvent.Home_Click_Premium)
            startActivity(Intent(this@HomeActivity, PremiumActivity2::class.java))
        }
        binding.imgSaleOffFab.setOnClickListener {
//            Toast.makeText(this, "Sale off Onclick", Toast.LENGTH_SHORT).show()
            FirebaseTracking.log(FirebaseLogEvent.Home_Click_Gift_icon)
            startActivity(Intent(this@HomeActivity, PremiumActivity2::class.java))
        }
        binding.imgCast.setOnClickListener {
            // TODO: open dialog
            FirebaseTracking.log(FirebaseLogEvent.Home_Click_Connect_Devices)
            caster.discovery.picker(this)
        }
        castOnClickSection()
    }

    private fun castOnClickSection() {
        binding.apply {
            llVideo.setOnClickListener {
                FirebaseTracking.log(FirebaseLogEvent.Home_Click_Video)
                goToCast(MediaRoute.Video)
            }
            llImage.setOnClickListener {
                FirebaseTracking.log(FirebaseLogEvent.Home_Click_Image)
                goToCast(MediaRoute.Image)
            }
            llAudio.setOnClickListener {
                FirebaseTracking.log(FirebaseLogEvent.Home_Click_Audio)
                goToCast(MediaRoute.Audio)
            }
            llYoutube.setOnClickListener {
                FirebaseTracking.log(FirebaseLogEvent.Home_Click_Youtube)
                goToCast(MediaRoute.Youtube)
            }
            llDrive.setOnClickListener {
                FirebaseTracking.log(FirebaseLogEvent.Home_Click_Drive)
                Toast.makeText(
                    this@HomeActivity,
                    getString(R.string.coming_soon),
                    Toast.LENGTH_LONG
                ).show()
            }
            llWebCast.setOnClickListener {
                FirebaseTracking.log(FirebaseLogEvent.Home_Click_Web_Cast)
                goToCast(MediaRoute.WebCast)
            }
            llOnlineImage.setOnClickListener {
                FirebaseTracking.log(FirebaseLogEvent.Home_Click_Online_Image)
                Toast.makeText(
                    this@HomeActivity,
                    getString(R.string.coming_soon),
                    Toast.LENGTH_LONG
                ).show()
            }
            llIpTv.setOnClickListener {
                FirebaseTracking.log(FirebaseLogEvent.Home_Click_Iptv)
                Toast.makeText(
                    this@HomeActivity,
                    getString(R.string.coming_soon),
                    Toast.LENGTH_LONG
                ).show()
            }
            llGooglePhotos.setOnClickListener {
                FirebaseTracking.log(FirebaseLogEvent.Home_Click_Google_Photo)
                Toast.makeText(
                    this@HomeActivity,
                    getString(R.string.coming_soon),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun goToCast(route: MediaRoute) {
        val intent = Intent(this@HomeActivity, MainActivity::class.java)
        intent.putExtra(MEDIA_ROUTE, route.route)
        if (AppPreferences().isPremiumSubscribed == false && AppPreferences().countTimeOpenApp!! >= 3) {
            dialogCenter.showDialog(DialogCenter.DialogType.LoadingAds)
            admobHelper.showGeneralAdInterstitial(this@HomeActivity) {
                dialogCenter.dismissDialog(DialogCenter.DialogType.LoadingAds)
                startActivity(intent)
            }
        } else {
            startActivity(intent)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_CAMERA -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted.
                    CameraPreviewService.start(this@HomeActivity)
                } else {
                    // permission denied.
                }
                return
            }
        }
    }

    private fun observerConnectingBrowser() {
        isStreamingBrowser.observe(this) { isStreaming ->
            binding.imgStateOnOffConnectBrowser.setTintColor(if (isStreaming) R.color.greenA02 else R.color.grayA01)
            binding.txtConnectBrowserState.text =
                if (isStreaming) getString(
                    R.string.connecting
                ) else getString(R.string.connect_with_browser)
        }
    }


    private fun observerConnectFloatingToolService() {
        isOpenFloatingToolLiveData.observe(this) {
            binding.switchModeFloatingTool.isChecked = it
        }
    }

    override fun onBackPressed() {
        if (dialogCenter.browserDialogShowing) {
            dialogCenter.dismissDialog(DialogCenter.DialogType.Browser)
        } else if (dialogCenter.browserDialogErrorShowing) {
            dialogCenter.dismissDialog(DialogCenter.DialogType.BrowserError)
        } else if (dialogCenter.tutorialDialogIsShowing) {
        } else if (dialogCenter.mLoadingAdsDialogShowing) {
            dialogCenter.dismissDialog(DialogCenter.DialogType.LoadingAds)
        } else if (dialogCenter.askPermissionOverLayDialogShowing) {
            dialogCenter.dismissDialog(DialogCenter.DialogType.AskPermissionOverLay)
        } else {
            if (!dialogCenter.exitAppDialogShowing && AppPreferences().isPremiumSubscribed == false) {
                dialogCenter.showDialog(DialogCenter.DialogType.ExitApp)
            } else {
                super.onBackPressed()
            }
        }
    }

    private fun hideBannerAds() {
        binding.containerAd.visibility = View.GONE
    }

    override fun onStop() {
        super.onStop()
        shakeAnimJob?.cancel()
        shakeAnimJob = null
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("destroy home")
        dialogCenter.onDestroy()
        caster.shutdown()
//        countDownJob?.cancel()
    }
}