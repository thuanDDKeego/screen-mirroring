package com.abc.sreenmirroring.ui.tutorial

import android.view.LayoutInflater
import android.view.ViewGroup
import com.abc.sreenmirroring.base.BaseFragment
import com.abc.sreenmirroring.databinding.FragmentFAQBinding
import com.abc.sreenmirroring.ui.tutorial.adapter.FAQItemAdapter
import com.abc.sreenmirroring.utils.Constant

class FAQFragment : BaseFragment<FragmentFAQBinding>() {

    private lateinit var adapter: FAQItemAdapter

    override fun initBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentFAQBinding.inflate(inflater, container, false)

    override fun initViews() {
        adapter = FAQItemAdapter()
        binding.recyclerViewFAQ.adapter = adapter
        adapter.setItemList(Constant.LIST_fAQ)
    }

    override fun initActions() {
    }

}