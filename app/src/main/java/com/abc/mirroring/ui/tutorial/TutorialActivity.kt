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
import com.abc.mirroring.utils.FirebaseLogEvent
import com.abc.mirroring.utils.FirebaseTracking
import dagger.hilt.android.AndroidEntryPoint
import one.shot.haki.ads.AdCenter
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
        binding.viewPager.offscreenPageLimit = 4
        binding.viewPager.adapter = TutorialPagerAdapter(this, supportFragmentManager)

        fun updateTabPager(position: Int) {

            binding.txtMirror.setTextColor(ContextCompat.getColor(this, R.color.grayA06))
            binding.viewMirror.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            binding.txtCast.setTextColor(ContextCompat.getColor(this, R.color.grayA06))
            binding.viewCast.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            binding.txtIPTV.setTextColor(ContextCompat.getColor(this, R.color.grayA06))
            binding.viewIPTV.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            binding.txtFAQ.setTextColor(ContextCompat.getColor(this, R.color.grayA06))
            binding.viewFAQ.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            binding.txtConnectedDevices.setTextColor(ContextCompat.getColor(this, R.color.grayA06))
            binding.viewConnectedDevices.setBackgroundColor(ContextCompat.getColor(this, R.color.white))

            when (position) {
                0 -> {
                    binding.txtMirror.setTextColor(ContextCompat.getColor(this, R.color.blueA01))
                    binding.viewMirror.setBackgroundColor(
                        ContextCompat.getColor(
                            this,
                            R.color.blueA01
                        )
                    )
                }

                1 -> {
                    binding.txtCast.setTextColor(ContextCompat.getColor(this, R.color.blueA01))
                    binding.viewCast.setBackgroundColor(ContextCompat.getColor(this, R.color.blueA01))
                }

                2 -> {
                    binding.txtIPTV.setTextColor(ContextCompat.getColor(this, R.color.blueA01))
                    binding.viewIPTV.setBackgroundColor(ContextCompat.getColor(this, R.color.blueA01))
                }

                3 -> {
                    binding.txtFAQ.setTextColor(ContextCompat.getColor(this, R.color.blueA01))
                    binding.viewFAQ.setBackgroundColor(ContextCompat.getColor(this, R.color.blueA01))
                }

                else -> {
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

        binding.llMirror.setOnClickListener {
            binding.viewPager.currentItem = 0
            updateTabPager(0)
        }

        binding.llCast.setOnClickListener {
            FirebaseTracking.log(FirebaseLogEvent.Tutorial_Click_Cast)
            binding.viewPager.currentItem = 1
            updateTabPager(1)
        }

        binding.llIPTV.setOnClickListener {
            FirebaseTracking.log(FirebaseLogEvent.Tutorial_Click_IPTV)
            binding.viewPager.currentItem = 2
            updateTabPager(2)
        }

        binding.llFAQ.setOnClickListener {
            FirebaseTracking.log(FirebaseLogEvent.Tutorial_Click_FAQ)
            binding.viewPager.currentItem = 3
            updateTabPager(3)
        }

        binding.llConnectedDevices.setOnClickListener {
            FirebaseTracking.log(FirebaseLogEvent.Tutorial_Click_Connecting_devices)
            binding.viewPager.currentItem = 4
            updateTabPager(4)
        }
    }


    override fun onBackPressed() {
        if (dialogCenter.mLoadingAdsDialogShowing) return
        if (binding.viewPager.currentItem != 0) {
            binding.viewPager.currentItem = 0
        } else {
            FirebaseTracking.log(FirebaseLogEvent.Tutorial_Click_Back)
            if (AppConfigRemote().turnOnBackFromTutorialInterstitial == true && AppPreferences().isPremiumSubscribed == false) {
                dialogCenter.showDialog(DialogCenter.DialogType.LoadingAds)
//                showLoadingAdsDialog()
//                admobHelper.showGeneralAdInterstitial(
                AdCenter.getInstance().interstitial?.show(
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