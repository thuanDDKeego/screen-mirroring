package com.abc.sreenmirroring.ui.home

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import com.abc.sreenmirroring.R
import com.abc.sreenmirroring.base.BaseActivity
import com.abc.sreenmirroring.config.AppPreferences
import com.abc.sreenmirroring.databinding.ActivityHomeBinding
import com.abc.sreenmirroring.databinding.LayoutDialogBrowserMirrorBinding
import com.abc.sreenmirroring.databinding.LayoutDialogTutorialFirstOpenBinding
import com.abc.sreenmirroring.extentions.setTintColor
import com.abc.sreenmirroring.helper.MY_PERMISSIONS_REQUEST_CAMERA
import com.abc.sreenmirroring.helper.isDrawOverlaysPermissionGranted
import com.abc.sreenmirroring.helper.requestOverlaysPermission
import com.abc.sreenmirroring.service.CameraPreviewService
import com.abc.sreenmirroring.service.FloatToolService
import com.abc.sreenmirroring.service.ServiceMessage
import com.abc.sreenmirroring.ui.browsermirror.BrowserMirrorActivity
import com.abc.sreenmirroring.ui.browsermirror.BrowserMirrorActivity.Companion.START_WHEN_RUNNING_REQUEST_CODE
import com.abc.sreenmirroring.ui.browsermirror.StreamViewModel
import com.abc.sreenmirroring.ui.devicemirror.DeviceMirrorActivity
import com.abc.sreenmirroring.ui.home.adapter.AdBannerAdapter
import com.abc.sreenmirroring.ui.home.adapter.TutorialDialogAdapter
import com.abc.sreenmirroring.ui.settings.SettingActivity
import com.abc.sreenmirroring.ui.tutorial.TutorialActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import timber.log.Timber

@AndroidEntryPoint
class HomeActivity : BaseActivity<ActivityHomeBinding>() {
    private var tutorialDialogIsShowing = false
    private var browserDialogShowing = false
    private lateinit var dialogBrowserBinding: LayoutDialogBrowserMirrorBinding
    private lateinit var dialogTutorialBinding: LayoutDialogTutorialFirstOpenBinding
    private lateinit var job: Job

    private var isStreamingBrowser: Boolean = false

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, HomeActivity::class.java)
        }
    }

    override fun initBinding() = ActivityHomeBinding.inflate(layoutInflater)

    override fun initViews() {
        if (AppPreferences().isTheFirstTimeUseApp == true) {
            AppPreferences().isTheFirstTimeUseApp = false
            showTutorialDialog()
        }
        initViewPager()
        observerConnectingBrowser()
        job = setAutoScrollJob()
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
            if (isStreamingBrowser) {
                val intent = Intent(this, BrowserMirrorActivity::class.java)
                startActivityForResult(intent, START_WHEN_RUNNING_REQUEST_CODE)
            } else {
                showBrowserDialog()
            }
        }
        binding.constrantMirror.setOnClickListener {
            DeviceMirrorActivity.gotoActivity(this@HomeActivity)
        }
        binding.imgSetting.setOnClickListener {
            SettingActivity.gotoActivity(this@HomeActivity)
        }
        binding.imgHelp.setOnClickListener {
            TutorialActivity.gotoActivity(this@HomeActivity)
        }
        binding.viewPagerAdHome.setOnTouchListener { v, event ->
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    job.cancel()
                }
                MotionEvent.ACTION_UP -> {
                    job = setAutoScrollJob()
                }
            }
            false
        }
        binding.switchModeFloatingTool.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
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

    private fun setAutoScrollJob(time: Long = 3000L) = lifecycleScope.launchWhenStarted {
        while (true) {
            delay(time)
            var nextStep =
                if (binding.viewPagerAdHome.currentItem < 2) binding.viewPagerAdHome.currentItem + 1 else 0
            binding.viewPagerAdHome.setCurrentItem(nextStep, true)
            updateTabPager(nextStep)
        }
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
        dialogBrowserBinding =
            LayoutDialogBrowserMirrorBinding.inflate(layoutInflater, binding.root, true)
        dialogBrowserBinding.apply {
            txtClose.setOnClickListener {
                dismissBrowserDialog()
            }
            cardDialog.setOnClickListener { }
            constraintBgBrowserDialog.setOnClickListener { dismissBrowserDialog() }

            txtStartVideoInTime.setOnClickListener {
                BrowserMirrorActivity.gotoActivity(this@HomeActivity)
                dismissBrowserDialog()
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
                dismissTutorialDialog()
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
}