package com.abc.sreenmirroring.ui.tutorial

import android.view.LayoutInflater
import android.view.ViewGroup
import com.abc.sreenmirroring.base.BaseFragment
import com.abc.sreenmirroring.databinding.FragmentTutorialBinding

class TutorialFragment : BaseFragment<FragmentTutorialBinding>() {

    override fun initBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentTutorialBinding.inflate(inflater, container, false)

    override fun initViews() {

    }

    override fun initActions() {
    }

}