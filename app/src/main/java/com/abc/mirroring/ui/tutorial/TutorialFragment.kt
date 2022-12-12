package com.abc.mirroring.ui.tutorial

import  AdType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.viewpager.widget.ViewPager
import com.abc.mirroring.R
import com.abc.mirroring.ads.AdmobHelper
import com.abc.mirroring.base.BaseFragment
import com.abc.mirroring.config.AppConfigRemote
import com.abc.mirroring.config.AppPreferences
import com.abc.mirroring.databinding.FragmentTutorialBinding
import com.abc.mirroring.ui.tutorial.adapter.TutorialGuideAdapter
import com.abc.mirroring.utils.FirebaseLogEvent
import com.abc.mirroring.utils.FirebaseTracking
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TutorialFragment : BaseFragment<FragmentTutorialBinding>() {

    private val viewModel by viewModels<TutorialViewModel>()
    private lateinit var adapter: TutorialGuideAdapter

    @Inject
    lateinit var admobHelper: AdmobHelper
    override fun initBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ) = FragmentTutorialBinding.inflate(inflater, container, false)

    override fun initViews() {
        FirebaseTracking.logHelpTutorialShowed()
        viewModel.currentStepTutorial.observe(viewLifecycleOwner) {
            setUpTutorialView(it)
        }
        adapter = TutorialGuideAdapter(requireActivity(), requireActivity().supportFragmentManager)
        binding.apply {
            viewPagerTutorial.adapter = adapter
            updateTabTutorialDialogPager(0)
            viewPagerTutorial.addOnPageChangeListener(object :
                ViewPager.OnPageChangeListener {
                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int,
                ) {
                }

                override fun onPageSelected(position: Int) {
                    updateTabTutorialDialogPager(position)
                }

                override fun onPageScrollStateChanged(state: Int) {}
            })

            imgStateStep1.setOnClickListener {
                updateTabTutorialDialogPager(0)
            }

            imgStateStep2.setOnClickListener {
                updateTabTutorialDialogPager(1)
            }

            imgStateStep3.setOnClickListener {
                updateTabTutorialDialogPager(2)
            }
            btnPrevious.setOnClickListener {
                FirebaseTracking.log(if (viewPagerTutorial.currentItem == 3) FirebaseLogEvent.Tutorial_Click_previous_2 else FirebaseLogEvent.Tutorial_Click_previous_1)
                viewPagerTutorial.setCurrentItem(
                    viewPagerTutorial.currentItem - 1,
                    true
                )
            }
        }
    }

    override fun showAds() {
        if (AppConfigRemote().turnOnBottomTutorialNative == true && AppPreferences().isPremiumSubscribed == false) {
            binding.containerAd.visibility = View.VISIBLE
            admobHelper.showNativeAdmob(
                requireActivity(),
                AdType.TUTORIAL_NATIVE,
                binding.nativeAdView.nativeAdView,
                true
            )
        } else {
            binding.containerAd.visibility = View.GONE
        }
    }

    override fun initActions() {

    }

    private fun setUpTutorialView(currentStep: Int = 0) {

    }

    private fun updateTabTutorialDialogPager(
        position: Int,
    ) {

        binding.apply {
            viewPagerTutorial.currentItem = position
            imgStateStep1.setImageResource(R.drawable.ic_state_off_tutorial_dialog)
            imgStateStep2.setImageResource(R.drawable.ic_state_off_tutorial_dialog)
            imgStateStep3.setImageResource(R.drawable.ic_state_off_tutorial_dialog)
            if (position == 0) {
                imgStateStep1.setImageResource(R.drawable.ic_state_on_tutorial_dialog)
            } else if (position == 1) {
                imgStateStep2.setImageResource(R.drawable.ic_state_on_tutorial_dialog)
            } else {
                imgStateStep3.setImageResource(R.drawable.ic_state_on_tutorial_dialog)
            }
            if (position != 0) {
                btnPrevious.visibility = View.VISIBLE
            } else {
                btnPrevious.visibility = View.INVISIBLE
            }
            if (position < 2) {
                btnNext.setOnClickListener {
                    FirebaseTracking.log(if (position == 1) FirebaseLogEvent.Tutorial_Click_next_1 else FirebaseLogEvent.Tutorial_Click_next_2)

                    viewPagerTutorial.currentItem = viewPagerTutorial.currentItem + 1
                }
                btnNext.visibility = View.VISIBLE
            } else {
                btnNext.visibility = View.INVISIBLE
            }
        }
    }
}