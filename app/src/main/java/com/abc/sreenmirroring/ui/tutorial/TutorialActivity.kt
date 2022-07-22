package com.abc.sreenmirroring.ui.tutorial

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.AdapterView
import androidx.viewpager.widget.ViewPager
import com.abc.sreenmirroring.R
import com.abc.sreenmirroring.base.BaseActivity
import com.abc.sreenmirroring.databinding.ActivityTutorialBinding
import com.abc.sreenmirroring.ui.tutorial.adapter.TutorialPagerAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TutorialActivity : BaseActivity<ActivityTutorialBinding>(),
    AdapterView.OnItemSelectedListener {

    companion object {
        fun gotoActivity(activity: Activity) {
            val intent = Intent(activity, TutorialActivity::class.java)
            activity.startActivity(intent)
        }
    }

    override fun initBinding() = ActivityTutorialBinding.inflate(layoutInflater)


    override fun initViews() {
        initViewPager()
    }

    override fun initActions() {
        binding.btnBack.setOnClickListener {
            Log.i("back pressed", "")
            onBackPressed()

        }
    }

    private fun initViewPager() {
        binding.viewPager.offscreenPageLimit = 3
        binding.viewPager.adapter = TutorialPagerAdapter(this, supportFragmentManager)

        fun updateTabPager(position: Int) {

            binding.txtTutorial.setTextColor(getColor(R.color.grayA06))
            binding.viewTutorial.setBackgroundColor(getColor(R.color.white))
            binding.txtFAQ.setTextColor(getColor(R.color.grayA06))
            binding.viewFAQ.setBackgroundColor(getColor(R.color.white))
            binding.txtConnectedDevices.setTextColor(getColor(R.color.grayA06))
            binding.viewConnectedDevices.setBackgroundColor(getColor(R.color.white))

            if (position == 0) {
                binding.txtTutorial.setTextColor(getColor(R.color.blueA01))
                binding.viewTutorial.setBackgroundColor(getColor(R.color.blueA01))
            } else if (position == 1) {
                binding.txtFAQ.setTextColor(getColor(R.color.blueA01))
                binding.viewFAQ.setBackgroundColor(getColor(R.color.blueA01))
            } else {
                binding.txtConnectedDevices.setTextColor(getColor(R.color.blueA01))
                binding.viewConnectedDevices.setBackgroundColor(getColor(R.color.blueA01))
            }
        }

        binding.viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
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
        if (binding.viewPager.currentItem == 1 || binding.viewPager.currentItem == 2) {
            binding.viewPager.currentItem = 0
        } else {
            super.onBackPressed()
        }
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
    }

}