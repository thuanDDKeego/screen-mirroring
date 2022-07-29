package com.abc.mirroring.ui.tutorial

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.abc.mirroring.base.BaseFragment
import com.abc.mirroring.databinding.FragmentFAQBinding
import com.abc.mirroring.ui.tutorial.adapter.FAQItemAdapter
import com.abc.mirroring.utils.FirebaseTracking

class FAQFragment : BaseFragment<FragmentFAQBinding>() {

    private lateinit var adapter: FAQItemAdapter
    private val viewModel by viewModels<TutorialViewModel>()

    override fun initBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentFAQBinding.inflate(inflater, container, false)

    override fun initViews() {
        FirebaseTracking.logHelpFAQShowed()
        adapter = FAQItemAdapter(viewModel.getFAQItem(requireActivity()))
        Log.i("binding FAQ", binding.toString())
        binding.recyclerViewFAQ.adapter = adapter
    }

    override fun initActions() {
    }

}