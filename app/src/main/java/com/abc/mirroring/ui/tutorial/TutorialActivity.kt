package com.abc.mirroring.ui.tutorial

import android.app.Activity
import android.content.Intent
import android.view.View
import android.widget.AdapterView
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import com.abc.mirroring.R
import com.abc.mirroring.ads.AdmobHelper
import com.abc.mirroring.base.BaseActivity
import com.abc.mirroring.config.AppConfigRemote
import com.abc.mirroring.config.AppPreferences
import com.abc.mirroring.databinding.ActivityTutorialBinding
import com.abc.mirroring.ui.dialog.DialogCenter
import com.abc.mirroring.ui.tutorial.adapter.TutorialPagerAdapter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TutorialActivity : BaseActivity<ActivityTutorialBinding>(),
    AdapterView.OnItemSelectedListener {

    @Inject
    lateinit var admobHelper: AdmobHelper

    private lateinit var dialogCenter: DialogCenter

    companion object {
        fun gotoActivity(activity: Activity) {
            val intent = Intent(activity, TutorialActivity::class.java)
            activity.startActivity(intent)
        }
    }

    override fun initBinding() = ActivityTutorialBinding.inflate(layoutInflater)

    override fun initViews() {
        dialogCenter = DialogCenter(this)
        initViewPager()
    }

    override fun initActions() {
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun initViewPager() {
        binding.viewPager.offscreenPageLimit = 3
        binding.viewPager.adapter = TutorialPagerAdapter(this, supportFragmentManager)

        fun updateTabPager(position: Int) {

            binding.txtTutorial.setTextColor(ContextCompat.getColor(this, R.color.grayA06))
            binding.viewTutorial.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            binding.txtFAQ.setTextColor(ContextCompat.getColor(this, R.color.grayA06))
            binding.viewFAQ.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            binding.txtConnectedDevices.setTextColor(ContextCompat.getColor(this, R.color.grayA06))
            binding.viewConnectedDevices.setBackgroundColor(
                ContextCompat.getColor(
                    this,
                    R.color.white
                )
            )

            if (position == 0) {
                binding.txtTutorial.setTextColor(ContextCompat.getColor(this, R.color.blueA01))
                binding.viewTutorial.setBackgroundColor(
                    ContextCompat.getColor(
                        this,
                        R.color.blueA01
                    )
                )
            } else if (position == 1) {
                binding.txtFAQ.setTextColor(ContextCompat.getColor(this, R.color.blueA01))
                binding.viewFAQ.setBackgroundColor(ContextCompat.getColor(this, R.color.blueA01))
            } else {
                binding.txtConnectedDevices.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.blueA01
                    )
                )
                binding.viewConnectedDevices.setBackgroundColor(
                    ContextCompat.getColor(
                        this,
                        R.color.blueA01
                    )
                )
            }
        }

        binding.viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int,
            ) {
            }

            override fun onPageSelected(position: Int) {
                updateTabPager(position)
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })

        binding.llTutorial.setOnClickListener {
            binding.viewPager.currentItem = 0
            updateTabPager(0)
        }

        binding.llFAQ.setOnClickListener {
            binding.viewPager.currentItem = 1
            updateTabPager(1)
        }

        binding.llConnectedDevices.setOnClickListener {
            binding.viewPager.currentItem = 2
            updateTabPager(2)
        }
    }

    override fun onBackPressed() {
        if(dialogCenter.mLoadingAdsDialogShowing) return
        if (binding.viewPager.currentItem == 1 || binding.viewPager.currentItem == 2) {
            binding.viewPager.currentItem = 0
        } else {
            if (AppConfigRemote().turnOnBackFromTutorialInterstitial == true && AppPreferences().isPremiumSubscribed == false) {
                dialogCenter.showDialog(DialogCenter.DialogType.LoadingAds)
//                showLoadingAdsDialog()
                admobHelper.showGeneralAdInterstitial(
                    this@TutorialActivity,
                ) {
                    dialogCenter.dismissDialog(DialogCenter.DialogType.LoadingAds)
//                    dialogCenter.dismissLoadingAdDialog()
                    super.onBackPressed()
                }
            } else {
                super.onBackPressed()
            }
        }
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
    }

}