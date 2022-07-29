package com.abc.sreenmirroring.ui.home.adapter

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.abc.sreenmirroring.ui.home.uiadbanner.AdBannerFragment1
import com.abc.sreenmirroring.ui.home.uiadbanner.AdBannerFragment2

class AdBannerAdapter(val context: Context, fragmentManager: FragmentManager) :
    FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getItem(position: Int): Fragment {
        return when (position) {
            0, 2 -> {
                AdBannerFragment1()
            }
            else -> {
                AdBannerFragment2()
            }
//            else -> {
//                AdBannerFragment3()
//            }
//        }
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return "AdBanner"
    }

    override fun getCount(): Int = 3

}