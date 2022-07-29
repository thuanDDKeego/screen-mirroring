package com.abc.sreenmirroring.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

abstract class BaseFragment<V : ViewBinding> : Fragment() {
    protected lateinit var binding: V


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = initBinding(inflater, container)
        initViews()
        initActions()
        showAds()
        return binding.root
    }

    abstract fun initBinding(inflater: LayoutInflater, container: ViewGroup?): V
    abstract fun initViews()
    abstract fun initActions()
    open fun showAds() {}
}