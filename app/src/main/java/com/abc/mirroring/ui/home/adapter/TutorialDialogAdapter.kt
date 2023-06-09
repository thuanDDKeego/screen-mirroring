package com.abc.mirroring.ui.home.adapter

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.abc.mirroring.ui.home.uitutorialdialog.TutorialDialogFragment1
import com.abc.mirroring.ui.home.uitutorialdialog.TutorialDialogFragment2
import com.abc.mirroring.ui.home.uitutorialdialog.TutorialDialogFragment3

class TutorialDialogAdapter(val context: Context, fragmentManager: FragmentManager) :
    FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                TutorialDialogFragment1()
            }
            1 -> {
                TutorialDialogFragment2()
            }
            else -> {
                TutorialDialogFragment3()
            }
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return ""
    }

    override fun getCount(): Int = 3

}