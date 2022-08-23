package com.abc.mirroring.ui.tutorial

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import com.abc.mirroring.ads.AdmobHelper
import com.abc.mirroring.base.BaseFragment
import com.abc.mirroring.databinding.FragmentFAQBinding
import com.abc.mirroring.ui.tutorial.adapter.FAQItemAdapter
import com.abc.mirroring.utils.FirebaseTracking
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FAQFragment : BaseFragment<FragmentFAQBinding>() {

    @Inject lateinit var admobHelper: AdmobHelper
    private lateinit var adapter: FAQItemAdapter
    private val viewModel by viewModels<TutorialViewModel>()

    override fun initBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentFAQBinding.inflate(inflater, container, false)

    override fun initViews() {
        FirebaseTracking.logHelpFAQShowed()
        adapter = FAQItemAdapter(requireActivity() as AppCompatActivity, viewModel.getFAQItem(requireActivity()))
        adapter.admobHelper = admobHelper
        Log.i("binding FAQ", binding.toString())
        binding.recyclerViewFAQ.adapter = adapter
    }

    override fun initActions() {
    }

}