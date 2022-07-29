package com.abc.mirroring.ui.tutorial.adapter

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.abc.mirroring.ui.tutorial.tutorialguide.TutorialFragmentGuide1
import com.abc.mirroring.ui.tutorial.tutorialguide.TutorialFragmentGuide2
import com.abc.mirroring.ui.tutorial.tutorialguide.TutorialFragmentGuide3

class TutorialGuideAdapter(
    val context: Context,
    fragmentManager: FragmentManager
) :
    FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                TutorialFragmentGuide1()
            }
            1 -> {
                TutorialFragmentGuide2()
            }
            else -> {
                TutorialFragmentGuide3()
            }
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return ""
    }

    override fun getCount(): Int = 3

}