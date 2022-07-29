package com.abc.sreenmirroring.ui.tutorial

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.viewpager.widget.ViewPager
import com.abc.sreenmirroring.R
import com.abc.sreenmirroring.base.BaseFragment
import com.abc.sreenmirroring.databinding.FragmentTutorialBinding
import com.abc.sreenmirroring.ui.tutorial.adapter.TutorialGuideAdapter

class TutorialFragment : BaseFragment<FragmentTutorialBinding>() {

    private val viewModel by viewModels<TutorialViewModel>()
    private lateinit var adapter: TutorialGuideAdapter

    override fun initBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentTutorialBinding.inflate(inflater, container, false)

    override fun initViews() {
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
                    positionOffsetPixels: Int
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
                viewPagerTutorial.setCurrentItem(
                    viewPagerTutorial.currentItem - 1,
                    true
                )
            }
        }
    }

    override fun initActions() {

    }

    private fun setUpTutorialView(currentStep: Int = 0) {

    }

    private fun updateTabTutorialDialogPager(
        position: Int
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
                    viewPagerTutorial.currentItem = viewPagerTutorial.currentItem + 1
                }
                btnNext.visibility = View.VISIBLE
            } else {
                btnNext.visibility = View.INVISIBLE
            }
        }
    }
}