package com.abc.sreenmirroring.ui.tutorial.adapter


import android.content.Context
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.abc.sreenmirroring.R
import com.abc.sreenmirroring.ui.tutorial.ConnectedDevicesFragment
import com.abc.sreenmirroring.ui.tutorial.FAQFragment
import com.abc.sreenmirroring.ui.tutorial.TutorialFragment

class TutorialPagerAdapter(val context: Context, fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getItem(position: Int): Fragment {
        return when(position){
            0->{
                TutorialFragment()
            }
            1 ->{
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
                context.getString(R.string.faq).toUpperCase()
            }
            else -> {
                context.getString(R.string.connected_devices).toUpperCase()

            }
        }
    }

    override fun getCount(): Int = 3

}