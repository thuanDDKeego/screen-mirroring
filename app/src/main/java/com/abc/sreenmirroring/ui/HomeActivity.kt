package com.abc.sreenmirroring.ui

import com.abc.sreenmirroring.base.BaseActivity
import com.abc.sreenmirroring.databinding.ActivityHomeBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeActivity : BaseActivity<ActivityHomeBinding>() {

    override fun initBinding() = ActivityHomeBinding.inflate(layoutInflater)

    override fun initViews() {
    }

    override fun initActions() {
        binding.constraintBrowserMirror.setOnClickListener {
            
        }
    }
}