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
import androidx.viewpager.widget.ViewPager
import com.abc.mirroring.R
import com.abc.mirroring.ads.AdmobHelper
import com.abc.mirroring.ads.AppOpenManager
import com.abc.mirroring.base.BaseActivity
import com.abc.mirroring.cast.MainActivity
import com.abc.mirroring.cast.MainActivity.Companion.MEDIA_ROUTE
import com.abc.mirroring.cast.shared.route.MediaRoute
import com.abc.mirroring.config.AppConfigRemote
import com.abc.mirroring.config.AppPreferences
import com.abc.mirroring.databinding.*
import com.abc.mirroring.extentions.setTintColor
import com.abc.mirroring.helper.MY_PERMISSIONS_REQUEST_CAMERA
import com.abc.mirroring.helper.isDrawOverlaysPermissionGranted
import com.abc.mirroring.helper.requestOverlaysPermission
import com.abc.mirroring.service.CameraPreviewService
import com.abc.mirroring.service.FloatToolService
import com.abc.mirroring.ui.browsermirror.BrowserMirrorActivity
import com.abc.mirroring.ui.browsermirror.BrowserMirrorActivity.Companion.START_WHEN_RUNNING_REQUEST_CODE
import com.abc.mirroring.ui.devicemirror.DeviceMirrorActivity
import com.abc.mirroring.ui.home.adapter.TutorialDialogAdapter
import com.abc.mirroring.ui.premium.PremiumActivity
import com.abc.mirroring.ui.settings.SettingActivity
import com.abc.mirroring.ui.tutorial.TutorialActivity
import com.abc.mirroring.utils.FirebaseTracking
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class HomeActivity : BaseActivity<ActivityHomeBinding>() {
    private var tutorialDialogIsShowing = false
    private var browserDialogShowing = false
    private var browserDialogErrorShowing = false
    private var askPermissionOverLayDialogShowing = false
    private var exitAppDialogShowing = false
    private lateinit var dialogBrowserBinding: LayoutDialogBrowserMirrorBinding
    private lateinit var dialogBrowserErrorBinding: LayoutDialogLoadRewardAdErrorBrowserBinding
    private lateinit var dialogTutorialBinding: LayoutDialogTutorialFirstOpenBinding
    private lateinit var dialogExitAppBinding: LayoutDialogExitAppBinding
    private lateinit var dialogAskPermissionOverLayBinding: LayoutDialogAskDisplayOverlayPermissionBinding
    private lateinit var goToMirrorActivityResult: ActivityResultLauncher<Intent>
    private var countDownJob: Job? = null
    private var rewardAdsJob: Job? = null
    private var shakeAnimJob: Job? = null

    @Inject
    lateinit var admobHelper: AdmobHelper


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
        initAds()
        val appPreferences = AppPreferences()
        FirebaseTracking.logHomeShowed()
        appPreferences.countTimeOpenApp = appPreferences.countTimeOpenApp!! + 1
        if (appPreferences.isTheFirstTimeUseApp == true) {
            appPreferences.isTheFirstTimeUseApp = false
            showTutorialDialog()
        } else if (appPreferences.countTimeOpenApp!! % 3 == 0 && AppPreferences().isPremiumSubscribed == false && AppConfigRemote().enable_premium == true) {
            PremiumActivity.gotoActivity(this@HomeActivity)
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
        if (AppConfigRemote().turnOnBottomTutorialNative == true && AppPreferences().isPremiumSubscribed == false) {
            binding.containerAd.visibility = View.VISIBLE
            admobHelper.showNativeAdmob(
                this,
                AdType.HOME_NATIVE,
                binding.nativeAdView.nativeAdView,
                true
            )
        } else {
            binding.containerAd.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        if (AppPreferences().isPremiumSubscribed == true) {
            hideBannerAds()
        }
        //shake img animation
        val shake = AnimationUtils.loadAnimation(this, R.anim.shake)
        shakeAnimJob = CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                delay(1000L)
                withContext(Dispatchers.Main) {
                    binding.imgPremium.clearAnimation()
                    binding.imgPremium.startAnimation(shake)
                }
                delay(9000L)
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
//        binding.txtCast.setOnClickListener {
//            startActivity(Intent(this, MainActivity::class.java))
//        }
        goToMirrorActivityResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
                val result = activityResult.resultCode
                val data = activityResult.data
                if (result == RESULT_OK && data != null && AppPreferences().isRated == false) {
                    val isShowRating = data.getBooleanExtra(SHOW_RATING_DIALOG, false)
                    if (isShowRating) showRatingDialog()
                }
            }
        binding.constraintBrowserMirror.setOnClickListener {
            FirebaseTracking.logHomeCardBrowserClicked()
            if (isStreamingBrowser.value == true || AppConfigRemote().turnOnHomeBrowserReward == false || AppPreferences().isPremiumSubscribed == true) {
                val intent = Intent(this, BrowserMirrorActivity::class.java)
                startActivityForResult(intent, START_WHEN_RUNNING_REQUEST_CODE)
            } else {
                showBrowserDialog()
            }
        }
        binding.constrantMirror.setOnClickListener {
            FirebaseTracking.logHomeMirrorClicked()
            val intent = Intent(this@HomeActivity, DeviceMirrorActivity::class.java)
            if (AppConfigRemote().turnOnGoToMirrorDeviceInterstitial == true && AppPreferences().isPremiumSubscribed == false) {
                showLoadingAdDialog()
                admobHelper.showGeneralAdInterstitial(
                    this@HomeActivity,
                ) {
                    dismissLoadingAdDialog()
                    goToMirrorActivityResult.launch(intent)
                }
            } else {
                goToMirrorActivityResult.launch(intent)
            }
        }
        binding.imgSetting.setOnClickListener {
            SettingActivity.gotoActivity(this@HomeActivity)
        }
        binding.imgHelp.setOnClickListener {
            FirebaseTracking.logHomeIconHelpClicked()
            TutorialActivity.gotoActivity(this@HomeActivity)
        }
        binding.switchModeFloatingTool.setOnCheckedChangeListener { _, isChecked ->
            Timber.d("switchMode $isChecked")
            if (isChecked) {
                FirebaseTracking.logHomeFloatingClicked(isChecked)
                if (isDrawOverlaysPermissionGranted()) {
                    FloatToolService.start(this@HomeActivity)
                } else {
                    binding.switchModeFloatingTool.isChecked = false
                    showAskPermissionOverlayDialog()
                }
            } else {
                FloatToolService.stop(this@HomeActivity)
            }
        }
        binding.imgPremium.setOnClickListener {
            startActivity(Intent(this@HomeActivity, PremiumActivity::class.java))
        }
        castOnClickSection()
    }

    private fun castOnClickSection() {
        binding.apply {
            llVideo.setOnClickListener {
                goToCast(MediaRoute.Video)
            }
            llImage.setOnClickListener {
                goToCast(MediaRoute.Image)
            }
            llAudio.setOnClickListener {
                goToCast(MediaRoute.Audio)
            }
            llYoutube.setOnClickListener {
                goToCast(MediaRoute.Youtube)
            }
            llDrive.setOnClickListener {
                Toast.makeText(this@HomeActivity, getString(R.string.coming_soon), Toast.LENGTH_LONG).show()
            }
            llWebCast.setOnClickListener {
                goToCast(MediaRoute.WebCast)
            }
        }
    }

    private fun goToCast(route: MediaRoute){
        val intent = Intent(this@HomeActivity, MainActivity::class.java)
        intent.putExtra(MEDIA_ROUTE, route.route)
        startActivity(intent)

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

    private fun dismissBrowserDialog() {
        if (browserDialogShowing) {
            binding.root.removeViewAt(binding.root.childCount - 1)
            countDownJob?.cancel()
            countDownJob = null
            rewardAdsJob?.cancel()
            rewardAdsJob = null
            Timber.d("jobState $countDownJob $rewardAdsJob")
            browserDialogShowing = false
        }
    }

    private fun showBrowserDialog() {
        if (browserDialogShowing) return
        browserDialogShowing = true
        FirebaseTracking.logHomeBrowserDialogShowed()
        dialogBrowserBinding =
            LayoutDialogBrowserMirrorBinding.inflate(layoutInflater, binding.root, true)
        dialogBrowserBinding.apply {
            txtStartVideoInTime.text =
                getString(R.string.video_starting_in, "5")
            countDownJob = CoroutineScope(Dispatchers.Main).launch {
                for (i in 4 downTo 0) {
                    delay(1000L)
                    txtStartVideoInTime.text =
                        getString(R.string.video_starting_in, i.toString())
                    if (i == 0) {
                        goToRewardAds()
                    }
                }
            }
            txtClose.setOnClickListener {
                dismissBrowserDialog()
            }
            cardDialog.setOnClickListener { }
            constraintBgBrowserDialog.setOnClickListener { dismissBrowserDialog() }

            txtStartVideoInTime.setOnClickListener {
                countDownJob?.cancel()
                goToRewardAds()
            }
        }
    }

    private fun goToRewardAds() {
        dialogBrowserBinding.apply {
            txtStartVideoInTime.setTextColor(
                ContextCompat.getColor(
                    this@HomeActivity,
                    R.color.txt_disable_gray
                )
            )
            txtStartVideoInTime.setOnClickListener { }
            progressBarLoadAds.visibility = View.VISIBLE
            if (rewardAdsJob == null) {
                rewardAdsJob = CoroutineScope(Dispatchers.Main).launch {
                    admobHelper.showRewardedAds(
                        this@HomeActivity,
                        AdType.BROWSER_MIRROR_REWARD
                    ) { isSuccess ->
                        if (isSuccess) {
                            BrowserMirrorActivity.gotoActivity(this@HomeActivity)
                            dismissBrowserDialog()
                        } else {
                            dismissBrowserDialog()
                            showBrowserErrorDialog()
                        }
                    }
                }
            }
        }
    }

    private fun dismissBrowserErrorDialog() {
        if (browserDialogErrorShowing) {
            binding.root.removeViewAt(binding.root.childCount - 1)
            browserDialogErrorShowing = false
        }
    }

    private fun showBrowserErrorDialog() {
        if (browserDialogErrorShowing) return
        browserDialogErrorShowing = true
        dialogBrowserErrorBinding =
            LayoutDialogLoadRewardAdErrorBrowserBinding.inflate(layoutInflater, binding.root, true)
        dialogBrowserErrorBinding.apply {
            txtCancel.setOnClickListener {
                dismissBrowserErrorDialog()
            }
            cardDialog.setOnClickListener { }
            constraintBgDialogDisconnect.setOnClickListener { dismissBrowserDialog() }

            txtRetry.setOnClickListener {
                dismissBrowserErrorDialog()
                showBrowserDialog()
            }
        }
    }

    private fun showTutorialDialog() {
        tutorialDialogIsShowing = true
        dialogTutorialBinding =
            LayoutDialogTutorialFirstOpenBinding.inflate(layoutInflater, binding.root, true)
        val tutorialAdapter = TutorialDialogAdapter(this, supportFragmentManager)
        dialogTutorialBinding.apply {
            viewPagerTutorialDialog.adapter = tutorialAdapter
            constraintBgDialogTutorial.setOnClickListener {}
            updateTabTutorialDialogPager(dialogTutorialBinding, 0)

            viewPagerTutorialDialog.addOnPageChangeListener(object :
                ViewPager.OnPageChangeListener {
                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int,
                ) {
                }

                override fun onPageSelected(position: Int) {
                    updateTabTutorialDialogPager(dialogTutorialBinding, position)
                }

                override fun onPageScrollStateChanged(state: Int) {}
            })

            imgStateStep1.setOnClickListener {
                viewPagerTutorialDialog.currentItem = 0
                updateTabTutorialDialogPager(dialogTutorialBinding, 0)
            }

            imgStateStep2.setOnClickListener {
                viewPagerTutorialDialog.currentItem = 1
                updateTabTutorialDialogPager(dialogTutorialBinding, 1)
            }

            imgStateStep3.setOnClickListener {
                viewPagerTutorialDialog.currentItem = 2
                updateTabTutorialDialogPager(dialogTutorialBinding, 2)
            }
            btnPrevious.setOnClickListener {
                viewPagerTutorialDialog.setCurrentItem(
                    viewPagerTutorialDialog.currentItem - 1,
                    true
                )
            }
            txtOk.setOnClickListener {
                if (AppPreferences().isPremiumSubscribed == true) {
                    dismissTutorialDialog()
                } else {
                    showLoadingAdDialog()
                    admobHelper.showGeneralAdInterstitial(
                        this@HomeActivity,
                    ) {
                        dismissLoadingAdDialog()
                        dismissTutorialDialog()
                    }
                }
            }
        }
    }

    private fun dismissExitAppDialog() {
        if (exitAppDialogShowing) {
            binding.root.removeViewAt(binding.root.childCount - 1)
            exitAppDialogShowing = false
        }
    }

    private fun showExitAppDialog() {
        if (exitAppDialogShowing) return
        exitAppDialogShowing = true
        dialogExitAppBinding =
            LayoutDialogExitAppBinding.inflate(layoutInflater, binding.root, true)
        dialogExitAppBinding.apply {

            admobHelper.showNativeAdmob(this@HomeActivity, AdType.EXIT_APP_NATIVE, nativeAdView)

            btnExitApp.setOnClickListener {
                super.onBackPressed()
            }
            btnClose.setOnClickListener {
                dismissExitAppDialog()
            }
            constraintExitAppDialog.setOnClickListener {
                dismissExitAppDialog()
            }

            cardDialog.setOnClickListener { }
        }
    }

    private fun dismissAskPermissionOverlayDialog() {
        if (askPermissionOverLayDialogShowing) {
            binding.root.removeViewAt(binding.root.childCount - 1)
            askPermissionOverLayDialogShowing = false
        }
    }

    private fun showAskPermissionOverlayDialog() {
        if (askPermissionOverLayDialogShowing) return
        askPermissionOverLayDialogShowing = true
        dialogAskPermissionOverLayBinding =
            LayoutDialogAskDisplayOverlayPermissionBinding.inflate(
                layoutInflater,
                binding.root,
                true
            )
        dialogAskPermissionOverLayBinding.apply {
            btnClose.setOnClickListener { dismissAskPermissionOverlayDialog() }
            btnAllow.setOnClickListener {
                AppOpenManager.instance?.disableAddWithActivity(HomeActivity::class.java)
                requestOverlaysPermission()
                dismissAskPermissionOverlayDialog()
            }
            constraintBgDialogAskPermission.setOnClickListener {
                dismissAskPermissionOverlayDialog()
            }
            llDialog.setOnClickListener {}
        }
    }


    private fun updateTabTutorialDialogPager(
        binding: LayoutDialogTutorialFirstOpenBinding,
        position: Int,
    ) {

        binding.apply {
            imgStateStep1.setImageResource(R.drawable.ic_state_off_tutorial_dialog)
            imgStateStep2.setImageResource(R.drawable.ic_state_off_tutorial_dialog)
            imgStateStep3.setImageResource(R.drawable.ic_state_off_tutorial_dialog)
            if (position == 0) {
                imgStateStep1.setImageResource(R.drawable.ic_state_on_tutorial_dialog)
            } else if (position == 1) {
                imgStateStep2.setImageResource(R.drawable.ic_state_on_tutorial_dialog)
            } else {
                imgStateStep3.setImageResource(R.drawable.ic_state_on_tutorial_dialog)
            }
            if (position != 0) {
                btnPrevious.visibility = View.VISIBLE
            } else {
                btnPrevious.visibility = View.INVISIBLE
            }
            if (position < 2) {
                btnNext.setOnClickListener {
                    viewPagerTutorialDialog.currentItem = viewPagerTutorialDialog.currentItem + 1
                }
                txtOk.visibility = View.INVISIBLE
                btnNext.visibility = View.VISIBLE
            } else {
                btnNext.visibility = View.INVISIBLE
                txtOk.visibility = View.VISIBLE
            }
        }
    }

    private fun dismissTutorialDialog() {
        if (tutorialDialogIsShowing) {
            binding.root.removeViewAt(binding.root.childCount - 1)
            tutorialDialogIsShowing = false
        }
    }

    override fun onBackPressed() {
        if (browserDialogShowing) {
            dismissBrowserDialog()
        } else if (browserDialogErrorShowing) {
            dismissBrowserErrorDialog()
        } else if (tutorialDialogIsShowing) {
        } else if (mLoadingAdDialogShowing) {
        } else if (askPermissionOverLayDialogShowing) {
            dismissAskPermissionOverlayDialog()
        } else {
            if (!exitAppDialogShowing && AppPreferences().isPremiumSubscribed == false) {
                showExitAppDialog()
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
        countDownJob?.cancel()
    }
}