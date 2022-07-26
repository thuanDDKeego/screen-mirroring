package com.abc.sreenmirroring.ui.home

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import android.widget.CompoundButton
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import com.abc.sreenmirroring.R
import com.abc.sreenmirroring.base.BaseActivity
import com.abc.sreenmirroring.config.AppPreferences
import com.abc.sreenmirroring.databinding.ActivityHomeBinding
import com.abc.sreenmirroring.databinding.LayoutDialogBrowserMirrorBinding
import com.abc.sreenmirroring.databinding.LayoutDialogTutorialFirstOpenBinding
import com.abc.sreenmirroring.helper.isDrawOverlaysPermissionGranted
import com.abc.sreenmirroring.helper.requestOverlaysPermission
import com.abc.sreenmirroring.service.FloatToolService
import com.abc.sreenmirroring.ui.browsermirror.BrowserMirrorActivity
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
    private var browserDialog = false
    private var tutorialDialogIsShowing = false
    private var browserDialogShowing = false
    private lateinit var dialogBrowserBinding: LayoutDialogBrowserMirrorBinding
    private lateinit var dialogTutorialBinding: LayoutDialogTutorialFirstOpenBinding
    private lateinit var job: Job
    override fun initBinding() = ActivityHomeBinding.inflate(layoutInflater)

    override fun initViews() {
        if (AppPreferences().isTheFirstTimeUseApp == true) {
            AppPreferences().isTheFirstTimeUseApp = false
        }
        showTutorialDialog()
        initViewPager()
        job = setAutoScrollJob()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun initActions() {
        binding.constraintBrowserMirror.setOnClickListener {
            showBrowserDialog()
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
        binding.switchModeFloatingTool.setOnCheckedChangeListener(object :
            CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
                if (isChecked) {
                    if (isDrawOverlaysPermissionGranted()) {
                        Timber.d("Start float tools")
                        FloatToolService.start(this@HomeActivity)
                    } else requestOverlaysPermission()
                } else {
                    FloatToolService.stop(this@HomeActivity)
                }
            }
        })
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


    private fun showBrowserDialog(autoShow: Boolean = true) {
//        if (browserDialog) return
//        browserDialog = true
        dialogBrowserBinding =
            LayoutDialogBrowserMirrorBinding.inflate(layoutInflater, binding.root, true)
        dialogBrowserBinding.txtClose.setOnClickListener {
            dialogBrowserBinding.root.visibility = View.INVISIBLE
        }
        CoroutineScope(Dispatchers.Main).launch {
            delay(5000)
            BrowserMirrorActivity.gotoActivity(this@HomeActivity)
        }
    }

    private fun showTutorialDialog() {
        dialogTutorialBinding =
            LayoutDialogTutorialFirstOpenBinding.inflate(layoutInflater, binding.root, true)
        var tutorialAdapter = TutorialDialogAdapter(this, supportFragmentManager)
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
                binding.viewPagerAdHome.currentItem = 0
                updateTabTutorialDialogPager(dialogTutorialBinding, 0)
            }

            imgStateStep2.setOnClickListener {
                binding.viewPagerAdHome.currentItem = 1
                updateTabTutorialDialogPager(dialogTutorialBinding, 1)
            }

            imgStateStep3.setOnClickListener {
                binding.viewPagerAdHome.currentItem = 2
                updateTabTutorialDialogPager(dialogTutorialBinding, 2)
            }
            imgStateStep4.setOnClickListener {
                binding.viewPagerAdHome.currentItem = 3
                updateTabTutorialDialogPager(dialogTutorialBinding, 3)
            }
            txtClose.setOnClickListener {
                viewPagerTutorialDialog.currentItem = viewPagerTutorialDialog.currentItem - 1
            }
        }
    }

    private fun updateTabTutorialDialogPager(
        binding: LayoutDialogTutorialFirstOpenBinding,
        position: Int
    ) {

        binding.apply {
            imgStateStep1.background =
                resources.getDrawable(R.drawable.ic_state_off_tutorial_dialog)
            imgStateStep2.background =
                resources.getDrawable(R.drawable.ic_state_off_tutorial_dialog)
            imgStateStep3.background =
                resources.getDrawable(R.drawable.ic_state_off_tutorial_dialog)
            imgStateStep4.background =
                resources.getDrawable(R.drawable.ic_state_off_tutorial_dialog)
            if (position == 0) {
                imgStateStep1.background =
                    resources.getDrawable(R.drawable.ic_state_on_tutorial_dialog)
            } else if (position == 1) {
                imgStateStep2.background =
                    resources.getDrawable(R.drawable.ic_state_on_tutorial_dialog)
            } else if (position == 2) {
                imgStateStep3.background =
                    resources.getDrawable(R.drawable.ic_state_on_tutorial_dialog)
            } else {
                imgStateStep4.background =
                    resources.getDrawable(R.drawable.ic_state_on_tutorial_dialog)
            }
            if (position != 0) {
                txtClose.visibility = View.VISIBLE
            } else {
                txtClose.visibility = View.INVISIBLE
            }
            if (position < 3) {
                txtUpgrade.text = this@HomeActivity.getString(R.string.next)
                txtUpgrade.setOnClickListener {
                    viewPagerTutorialDialog.currentItem = viewPagerTutorialDialog.currentItem + 1
                }
            } else {
                txtUpgrade.text = this@HomeActivity.getString(R.string.go)
                txtUpgrade.setOnClickListener {
                    binding.root.visibility = View.GONE
                }
            }
        }
    }
}