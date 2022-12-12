package com.abc.mirroring.ui.tutorial.adapter


import android.content.Context
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.abc.mirroring.R
import com.abc.mirroring.ui.tutorial.CastFragment
import com.abc.mirroring.ui.tutorial.ConnectedDevicesFragment
import com.abc.mirroring.ui.tutorial.FAQFragment
import com.abc.mirroring.ui.tutorial.TutorialFragment

class TutorialPagerAdapter(val context: Context, fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getItem(position: Int): Fragment {
        return when(position){
            0->{
                TutorialFragment()
            }
            1 ->{
                CastFragment()
            }
            2-> {
                FAQFragment()
            }
            else -> {
                ConnectedDevicesFragment()
            }

        }
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        return super.instantiateItem(container, position)
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when(position) {
            0->{
                context.getString(R.string.tutorial).toUpperCase()
            }
            1 -> {
                context.getString(R.string.cast).toUpperCase()
            }
            2 -> {
                context.getString(R.string.faq).toUpperCase()
            }
            else -> {
                context.getString(R.string.connected_devices).toUpperCase()

            }
        }
    }

    override fun getCount(): Int = 4

}