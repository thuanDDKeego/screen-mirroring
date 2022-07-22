package com.abc.sreenmirroring.ui.tutorial

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.abc.sreenmirroring.base.BaseFragment
import com.abc.sreenmirroring.databinding.FragmentFAQBinding
import com.abc.sreenmirroring.ui.tutorial.adapter.FAQItemAdapter
import com.abc.sreenmirroring.utils.Constant
import javax.inject.Inject

class FAQFragment : BaseFragment<FragmentFAQBinding>() {

    private lateinit var adapter: FAQItemAdapter
    private val viewModel by viewModels<TutorialViewModel>()

    override fun initBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentFAQBinding.inflate(inflater, container, false)

    override fun initViews() {
        adapter = FAQItemAdapter(viewModel.getFAQItem(requireActivity()))
        Log.i("binding FAQ", binding.toString())
        binding.recyclerViewFAQ.adapter = adapter
    }

    override fun initActions() {
    }

}