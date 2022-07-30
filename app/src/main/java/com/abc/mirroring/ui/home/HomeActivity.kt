package com.abc.mirroring.ui.home

import AdType
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.view.MotionEvent
import android.view.View
import android.widget.CompoundButton
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import com.abc.mirroring.R
import com.abc.mirroring.ads.AdmobHelper
import com.abc.mirroring.base.BaseActivity
import com.abc.mirroring.config.AppPreferences
import com.abc.mirroring.databinding.ActivityHomeBinding
import com.abc.mirroring.databinding.LayoutDialogBrowserMirrorBinding
import com.abc.mirroring.databinding.LayoutDialogLoadRewardAdErrorBrowserBinding
import com.abc.mirroring.databinding.LayoutDialogTutorialFirstOpenBinding
import com.abc.mirroring.extentions.setTintColor
import com.abc.mirroring.helper.MY_PERMISSIONS_REQUEST_CAMERA
import com.abc.mirroring.helper.isDrawOverlaysPermissionGranted
import com.abc.mirroring.helper.requestOverlaysPermission
import com.abc.mirroring.service.CameraPreviewService
import com.abc.mirroring.service.FloatToolService
import com.abc.mirroring.service.ServiceMessage
import com.abc.mirroring.ui.browsermirror.BrowserMirrorActivity
import com.abc.mirroring.ui.browsermirror.BrowserMirrorActivity.Companion.START_WHEN_RUNNING_REQUEST_CODE
import com.abc.mirroring.ui.browsermirror.StreamViewModel
import com.abc.mirroring.ui.devicemirror.DeviceMirrorActivity
import com.abc.mirroring.ui.home.adapter.AdBannerAdapter
import com.abc.mirroring.ui.home.adapter.TutorialDialogAdapter
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
    private lateinit var dialogBrowserBinding: LayoutDialogBrowserMirrorBinding
    private lateinit var dialogBrowserErrorBinding: LayoutDialogLoadRewardAdErrorBrowserBinding
    private lateinit var dialogTutorialBinding: LayoutDialogTutorialFirstOpenBinding
    private lateinit var job: Job
    private lateinit var countDownJob: Job

    @Inject
    lateinit var admobHelper: AdmobHelper

    private var isStreamingBrowser: Boolean = false

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, HomeActivity::class.java)
        }
    }

    override fun initBinding() = ActivityHomeBinding.inflate(layoutInflater)

    override fun initViews() {
        FirebaseTracking.logHomeShowed()
        if (AppPreferences().isTheFirstTimeUseApp == true) {
            AppPreferences().isTheFirstTimeUseApp = false
            admobHelper.loadAdInterstitial(
                this@HomeActivity,
                AdType.HOME_ONBOARDING_INTERSTITIAL
            ) {}
            showTutorialDialog()
        }
        initViewPager()
        observerConnectingBrowser()
        job = scrollToAds()
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
        //set swift mode with floating tools state
        binding.switchModeFloatingTool.isChecked = FloatToolService.isRunning
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun initActions() {
        binding.constraintBrowserMirror.setOnClickListener {
            FirebaseTracking.logHomeCardBrowserClicked()
            if (isStreamingBrowser) {
                val intent = Intent(this, BrowserMirrorActivity::class.java)
                startActivityForResult(intent, START_WHEN_RUNNING_REQUEST_CODE)
            } else {
                showBrowserDialog()
            }
        }
        binding.constrantMirror.setOnClickListener {
            FirebaseTracking.logHomeMirrorClicked()
            admobHelper.showAdInterstitial(
                this@HomeActivity,
                AdType.GO_MIRROR_DEVICE_INTERSTITIAL
            ) {
                DeviceMirrorActivity.gotoActivity(this@HomeActivity)
            }
        }
        binding.imgSetting.setOnClickListener {
            SettingActivity.gotoActivity(this@HomeActivity)
        }
        binding.imgHelp.setOnClickListener {
            FirebaseTracking.logHomeIconHelpClicked()
            TutorialActivity.gotoActivity(this@HomeActivity)
        }
        binding.viewPagerAdHome.setOnTouchListener { v, event ->
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    job.cancel()
                }
                MotionEvent.ACTION_UP -> {
                    job = scrollToAds()
                }
            }
            false
        }
        binding.switchModeFloatingTool.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                FirebaseTracking.logHomeFloatingClicked(isChecked)
                if (isDrawOverlaysPermissionGranted()) {
                    Timber.d("Start float tools")
                    FloatToolService.start(this@HomeActivity)
                    binding.txtStateModeFloatingView.text = getString(R.string.on_mode)
                } else requestOverlaysPermission()
            } else {
                FloatToolService.stop(this@HomeActivity)
                binding.txtStateModeFloatingView.text = getString(R.string.off_mode)
            }
        }
//        binding.testCrash.setOnClickListener {
//            throw RuntimeException("Test Crash")
//        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
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
        StreamViewModel.getInstance().serviceMessageLiveData.observe(this) { serviceMessage ->
            when (serviceMessage) {
                is ServiceMessage.ServiceState -> {
                    isStreamingBrowser = serviceMessage.isStreaming
                    binding.imgStateOnOffConnectBrowser.setTintColor(if (serviceMessage.isStreaming) R.color.blueA01 else R.color.grayA01)
                    binding.txtConnectBrowserState.text =
                        if (serviceMessage.isStreaming) getString(
                            R.string.connecting
                        ) else getString(R.string.connect_with_browser)
                }
                else -> {
                    isStreamingBrowser = false
                }
            }
        }
    }

    override fun initAdmob() {
        admobHelper.loadRewardedAds(this, AdType.BROWSER_MIRROR_REWARD) {}
        admobHelper.loadAdInterstitial(this, AdType.GO_MIRROR_DEVICE_INTERSTITIAL) {}

    }

    private fun setAutoScrollJob(time: Long = 3000L) = lifecycleScope.launchWhenStarted {
        while (true) {
            delay(time)
            var nextStep =
                if (binding.viewPagerAdHome.currentItem < 2) binding.viewPagerAdHome.currentItem + 1 else 0
            binding.viewPagerAdHome.setCurrentItem(nextStep, true)
            updateTabPager(nextStep)
        }
    }

    private fun scrollToAds(time: Long = 3000L) = lifecycleScope.launchWhenStarted {
        delay(time)
        binding.viewPagerAdHome.setCurrentItem(1, true)
        updateTabPager(1)
    }

    private fun initViewPager() {

        binding.viewPagerAdHome
        binding.viewPagerAdHome.offscreenPageLimit = 3
        binding.viewPagerAdHome.adapter = AdBannerAdapter(this, supportFragmentManager)
        updateTabPager(0)
        binding.viewPagerAdHome.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }


            override fun onPageSelected(position: Int) {
                updateTabPager(position)
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })

        binding.imgStateStepBanner1.setOnClickListener {
            binding.viewPagerAdHome.currentItem = 0
            updateTabPager(0)
        }

        binding.imgStateStepBanner2.setOnClickListener {
            binding.viewPagerAdHome.currentItem = 1
            updateTabPager(1)
        }

        binding.imgStateStepBanner3.setOnClickListener {
            binding.viewPagerAdHome.currentItem = 2
            updateTabPager(2)
        }
    }

    private fun updateTabPager(position: Int) {

        binding.imgStateStepBanner1.background =
            resources.getDrawable(R.drawable.ic_state_off_viewpager_home)
        binding.imgStateStepBanner2.background =
            resources.getDrawable(R.drawable.ic_state_off_viewpager_home)
        binding.imgStateStepBanner3.background =
            resources.getDrawable(R.drawable.ic_state_off_viewpager_home)

        if (position == 0) {
            binding.imgStateStepBanner1.background =
                resources.getDrawable(R.drawable.ic_state_on_viewpager_home)
        } else if (position == 1) {
            binding.imgStateStepBanner2.background =
                resources.getDrawable(R.drawable.ic_state_on_viewpager_home)
        } else {
            binding.imgStateStepBanner3.background =
                resources.getDrawable(R.drawable.ic_state_on_viewpager_home)
        }
    }

    private fun dismissBrowserDialog() {
        if (browserDialogShowing) {
            binding.root.removeViewAt(binding.root.childCount - 1)
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
                for (i in 5 downTo 0) {
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
                job.cancel()
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
                    positionOffsetPixels: Int
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
                admobHelper.showAdInterstitial(
                    this@HomeActivity,
                    AdType.HOME_ONBOARDING_INTERSTITIAL
                ) {
                    dismissTutorialDialog()
                }
            }
        }
    }

    private fun updateTabTutorialDialogPager(
        binding: LayoutDialogTutorialFirstOpenBinding,
        position: Int
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

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
        countDownJob.cancel()
    }
}