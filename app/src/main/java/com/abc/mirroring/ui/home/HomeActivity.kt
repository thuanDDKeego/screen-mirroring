package com.abc.mirroring.ui.home

import AdType
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
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
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import dagger.hilt.android.AndroidEntryPoint
import dev.sofi.ads.AdCenter
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class HomeActivity : BaseActivity<ActivityHomeXmasBinding>() {
    private lateinit var goToActivityAndReceptShowDialogRateResult: ActivityResultLauncher<Intent>
    private lateinit var dialogCenter: DialogCenter
    private var shakeAnimJob: Job? = null

    @Inject
    lateinit var admobHelper: AdmobHelper

    @Inject
    lateinit var adCenter: AdCenter

    @Inject
    lateinit var caster: Caster

    private lateinit var appUpdateManager: AppUpdateManager
    private val MY_REQUEST_CODE = 1

    // Declare the launcher at the top of your Activity/Fragment:
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications.
        } else {
            // TODO: Inform user that that your app will not show notifications.
        }
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, HomeActivity::class.java)
        }

        var isStreamingBrowser = MutableLiveData(false)
        val isOpenFloatingToolLiveData = MutableLiveData(FloatToolService.isRunning)
        const val SHOW_RATING_DIALOG = "soRatingDialog"
    }

    override fun initBinding() = ActivityHomeXmasBinding.inflate(layoutInflater)

    override fun initViews() {
        checkForUpdateAvailability()
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
        } else {
            if (AppPreferences().countTimeOpenApp!! % 3 == 0 && AppPreferences().isPremiumSubscribed == false && AppConfigRemote().enable_premium == true) {
                startActivity(Intent(this@HomeActivity, PremiumActivity2::class.java))
            }
        }

        askNotificationPermission()
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

//        listOf<View>(
//            binding.llVideo,
//            binding.llImage,
//            binding.llAudio,
//            binding.llYoutube,
//            binding.llWebCast,
//            binding.llDrive,
//            binding.llOnlineImage,
//            binding.llIpTv,
//            binding.llGooglePhotos,
//        ).forEachIndexed { index, view ->
//            YoYo.with(Techniques.Pulse)
//                .delay((index + 1) * 200L)
//                .duration(800)
//                .playOn(view);
//        }
    }

    private fun initAds() {
        binding.containerAd.visibility = View.GONE
        if (AppPreferences().isPremiumSubscribed == false) {
            adCenter.interstitial?.load(this)
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
        checkForUpdateStalled()
        if (AppPreferences().isPremiumSubscribed == true) {
            hideBannerAds()
            binding.imgPremium.visibility = View.GONE
            binding.imgPremium.clearAnimation()
            binding.imgSaleOffFab.visibility = View.GONE
            binding.imgSaleOffFab.isClosed = true
            binding.imgSaleOffFab.clearAnimation()
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
        CoroutineScope(Dispatchers.Default).launch {
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

        goToActivityAndReceptShowDialogRateResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
                val result = activityResult.resultCode
                val data = activityResult.data
                if (result == RESULT_OK && data != null && AppPreferences().isRated == false && AppPreferences().countSatisfied!! > 1) {
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
        setupBrowserMirrorActions()
        setupScreenMirroringActions()
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

    private fun setupBrowserMirrorActions() {
        binding.constraintBrowserMirror.setOnClickListener {
            FirebaseTracking.log(FirebaseLogEvent.Home_Click_Mirror_to_Web)
            if (isStreamingBrowser.value == true || AppConfigRemote().turnOnHomeBrowserReward == false || AppPreferences().isPremiumSubscribed == true) {
                val intent = Intent(this, BrowserMirrorActivity::class.java)
                startActivityForResult(intent, START_WHEN_RUNNING_REQUEST_CODE)
            } else {
                if (AppPreferences().browserMirroringCountUsages!! > AppConfigRemote().browserMirroringUsages!!) {
                    dialogCenter.showDialog(
                        DialogCenter.DialogType.AskingForPremium(
                            getString(R.string.subscribe_premium),
                            getString(R.string.you_have_reached_the_number_of_free_uses),
                            R.mipmap.bg_browser_dialog_header
                        ) {})
                    return@setOnClickListener
                }
                dialogCenter.showDialog(DialogCenter.DialogType.RewardAdNotification(
                    getString(R.string.browser_mirror_uppercase),
                    getString(R.string.watch_short_video_to_unlock_browser),
                    onRewarded = {
                        BrowserMirrorActivity.gotoActivity(this@HomeActivity)
                    }
                ) {
                    Toast.makeText(this, getString(R.string.failed_to_unlock_watching_again), Toast.LENGTH_LONG).show()
                })
            }
        }
    }

    private fun setupScreenMirroringActions() {
        binding.constraintMirror.setOnClickListener {
            FirebaseTracking.log(FirebaseLogEvent.Home_Click_Mirror_to_TV)
            val intent = Intent(this@HomeActivity, DeviceMirrorActivity::class.java)
            if (AppConfigRemote().turnOnGoToMirrorDeviceInterstitial == true && AppPreferences().isPremiumSubscribed == false) {
                if (AppPreferences().screenMirroringCountUsages!! > AppConfigRemote().screenMirroringUsages!!) {
                    dialogCenter.showDialog(
                        DialogCenter.DialogType.AskingForPremium(
                            getString(R.string.subscribe_premium),
                            getString(R.string.you_have_reached_the_number_of_free_uses),
                            R.mipmap.bg_browser_dialog_header
                        ) {})
                    return@setOnClickListener
                }
                AppPreferences().screenMirroringCountUsages =
                    AppPreferences().screenMirroringCountUsages!! + 1
                dialogCenter.showDialog(DialogCenter.DialogType.LoadingAds)
//                admobHelper.showGeneralAdInterstitial(
                adCenter.interstitial?.show(
                    this@HomeActivity
                ) {
                    AppPreferences().countAdsClosed = AppPreferences().countAdsClosed!! + 1
                    dialogCenter.dismissDialog(DialogCenter.DialogType.LoadingAds)
                    goToActivityAndReceptShowDialogRateResult.launch(intent)
                }
            } else {
                goToActivityAndReceptShowDialogRateResult.launch(intent)
            }
        }
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
//                dialogCenter.showDialog(DialogCenter.DialogType.RewardAdNotification(
//                    label = "",
//                    content = getString(R.string.watch_short_video_to_unlock_youtube),
//                    backgroundId = R.drawable.bg_youtube_dialog,
//                    onRewarded = {
//                        val intent = Intent(this@HomeActivity, MainActivity::class.java)
//                        intent.putExtra(MEDIA_ROUTE, MediaRoute.Youtube.route)
//                        goToActivityAndReceptShowDialogRateResult.launch(intent)
//                    }
//                ) {
//                    Toast.makeText(this@HomeActivity, getString(R.string.failed_to_unlock_watching_again), Toast.LENGTH_LONG).show()
//                })
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
            llTiktok.setOnClickListener {
                FirebaseTracking.log(FirebaseLogEvent.Home_Click_Tiktok)
                Toast.makeText(
                    this@HomeActivity,
                    getString(R.string.coming_soon),
                    Toast.LENGTH_LONG
                ).show()
            }
            llIpTv.setOnClickListener {
                FirebaseTracking.log(FirebaseLogEvent.Home_Click_Iptv)
                goToCast(MediaRoute.IPTV)
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
//            admobHelper.showGeneralAdInterstitial(this@HomeActivity) {
            adCenter.interstitial?.show(this) {
                dialogCenter.dismissDialog(DialogCenter.DialogType.LoadingAds)
                AppPreferences().countAdsClosed = AppPreferences().countAdsClosed!! + 1
                goToActivityAndReceptShowDialogRateResult.launch(intent)
            }
        } else {
            goToActivityAndReceptShowDialogRateResult.launch(intent)
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


    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun onBackPressed() {
        if (dialogCenter.browserDialogShowing) {
            dialogCenter.dismissDialog(DialogCenter.DialogType.RewardAdNotification(onRewarded = {}) {})
        } else if (dialogCenter.browserDialogErrorShowing) {
            dialogCenter.dismissDialog(DialogCenter.DialogType.RewardAdNotificationError)
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

    private fun checkForUpdateAvailability() {
        appUpdateManager = AppUpdateManagerFactory.create(this)

// Returns an intent object that you use to check for an update.
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

// Checks that the platform will allow the specified type of update.
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                // This example applies an immediate update. To apply a flexible update
                // instead, pass in AppUpdateType.FLEXIBLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
//                && appUpdateInfo.updatePriority() >= 4 /* high priority */
            ) {
                // Request an immediate update.
                appUpdateManager.startUpdateFlowForResult(
                    // Pass the intent that is returned by 'getAppUpdateInfo()'.
                    appUpdateInfo,
                    // Or 'AppUpdateType.FLEXIBLE' for flexible updates.
                    AppUpdateType.IMMEDIATE,
                    // The current activity making the update request.
                    this,
                    // Include a request code to later monitor this update request.
                    MY_REQUEST_CODE
                )
            }
        }
    }

    // Checks that the update is not stalled during 'onResume()'.
// However, you should execute this check at all entry points into the app.
    private fun checkForUpdateStalled() {
        appUpdateManager
            .appUpdateInfo
            .addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.updateAvailability()
                    == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
                ) {
                    // If an in-app update is already running, resume the update.
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        AppUpdateType.IMMEDIATE,
                        this,
                        MY_REQUEST_CODE
                    )
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MY_REQUEST_CODE) {
            if (resultCode != RESULT_OK) {
                Timber.d("MY_APP", "Update flow failed! Result code: $resultCode")
                // If the update is cancelled or fails,
                // you can request to start the update again.
            }
        }
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