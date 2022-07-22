package com.abc.sreenmirroring.ui.tutorial

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.abc.sreenmirroring.R
import com.abc.sreenmirroring.base.BaseFragment
import com.abc.sreenmirroring.databinding.FragmentTutorialBinding

class TutorialFragment : BaseFragment<FragmentTutorialBinding>() {

    private val viewModel by viewModels<TutorialViewModel>()

    override fun initBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentTutorialBinding.inflate(inflater, container, false)

    override fun initViews() {
        viewModel.currentStepTutorial.observe(viewLifecycleOwner) {
            setUpTutorialView(it)
        }
    }

    override fun initActions() {
        binding.txtSkipTutorial.setOnClickListener {
            when (viewModel.currentStepTutorial.value) {
                0 -> requireActivity().onBackPressed()
                1, 2, 3 -> viewModel.setCurrentStepTutorial(viewModel.currentStepTutorial.value!! - 1)
            }
        }
        binding.btnNext.setOnClickListener {
            if (viewModel.currentStepTutorial.value!! < 3) {
                viewModel.setCurrentStepTutorial(viewModel.currentStepTutorial.value!! + 1)
            }
        }
        binding.btnGo.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun setUpTutorialView(currentStep: Int = 0) {
        binding.btnNext.visibility = View.INVISIBLE
        binding.btnGo.visibility = View.INVISIBLE
        when (currentStep) {
            0 -> {
                binding.txtGuildTutorial.text =
                    requireActivity().resources.getString(R.string.tutorial_guild1)
                binding.txtSkipTutorial.text = requireActivity().resources.getString(R.string.skip)
                binding.btnNext.visibility = View.VISIBLE
            }
            1 -> {
                binding.txtGuildTutorial.text =
                    requireActivity().resources.getString(R.string.tutorial_guild2)
                binding.txtSkipTutorial.text =
                    requireActivity().resources.getString(R.string.previous)
                binding.btnNext.visibility = View.VISIBLE
            }
            2 -> {
                binding.txtGuildTutorial.text =
                    requireActivity().resources.getString(R.string.tutorial_guild3)
                binding.txtSkipTutorial.text =
                    requireActivity().resources.getString(R.string.previous)
                binding.btnNext.visibility = View.VISIBLE
            }
            else -> {
                binding.txtGuildTutorial.text =
                    requireActivity().resources.getString(R.string.tutorial_guild4)
                binding.txtSkipTutorial.text =
                    requireActivity().resources.getString(R.string.previous)
                binding.btnGo.visibility = View.VISIBLE
            }
        }
    }

}