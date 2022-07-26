package com.abc.sreenmirroring.ui.home

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import com.abc.sreenmirroring.R
import com.abc.sreenmirroring.base.BaseActivity
import com.abc.sreenmirroring.databinding.ActivityHomeBinding
import com.abc.sreenmirroring.databinding.LayoutDialogBrowserMirrorBinding
import com.abc.sreenmirroring.helper.isDrawOverlaysPermissionGranted
import com.abc.sreenmirroring.helper.requestOverlaysPermission
import com.abc.sreenmirroring.service.FloatToolService
import com.abc.sreenmirroring.ui.browsermirror.BrowserMirrorActivity
import com.abc.sreenmirroring.ui.devicemirror.DeviceMirrorActivity
import com.abc.sreenmirroring.ui.home.adapter.AdBannerAdapter
import com.abc.sreenmirroring.ui.settings.SettingActivity
import com.abc.sreenmirroring.ui.tutorial.TutorialActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import timber.log.Timber

@AndroidEntryPoint
class HomeActivity : BaseActivity<ActivityHomeBinding>() {
    private var browserDialogShowing = false
    private lateinit var dialogBinding: LayoutDialogBrowserMirrorBinding
    private lateinit var job: Job
    override fun initBinding() = ActivityHomeBinding.inflate(layoutInflater)

    override fun initViews() {
        initViewPager()
        job = setAutoScrollJob()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun initActions() {
        binding.constraintBrowserMirror.setOnClickListener {
//            showBrowserDialog()
            BrowserMirrorActivity.gotoActivity(this@HomeActivity)
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
        binding.viewPagerAdHome.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        job.cancel()
                    }
                    MotionEvent.ACTION_UP -> {
                        job = setAutoScrollJob()
                    }
                }
                return false
            }
        })
        binding.switchModeFloatingTool.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                if (isDrawOverlaysPermissionGranted()) {
                    Timber.d("Start float tools")
                    FloatToolService.start(this@HomeActivity)
                } else requestOverlaysPermission()
            } else {
                FloatToolService.stop(this@HomeActivity)
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
        dialogBinding = LayoutDialogBrowserMirrorBinding.inflate(layoutInflater, binding.root, true)
        dialogBinding.txtClose.setOnClickListener {
            dismissBrowserDialog()
        }
        dialogBinding.txtStartVideoInTime.setOnClickListener {
            BrowserMirrorActivity.gotoActivity(this@HomeActivity)
            dismissBrowserDialog()
        }
    }
}