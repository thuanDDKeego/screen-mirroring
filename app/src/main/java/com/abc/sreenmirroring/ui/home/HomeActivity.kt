package com.abc.sreenmirroring.ui.home

import com.abc.sreenmirroring.base.BaseActivity
import com.abc.sreenmirroring.databinding.ActivityHomeBinding
import com.abc.sreenmirroring.ui.browsermirror.BrowserMirrorActivity
import com.abc.sreenmirroring.ui.tutorial.TutorialActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeActivity : BaseActivity<ActivityHomeBinding>() {

    override fun initBinding() = ActivityHomeBinding.inflate(layoutInflater)

    override fun initViews() {
    }

    override fun initActions() {
        binding.constraintBrowserMirror.setOnClickListener {
            BrowserMirrorActivity.gotoActivity(this@HomeActivity)
        }
        binding.imgHelp.setOnClickListener {
            TutorialActivity.gotoActivity(this@HomeActivity)
        }
    }
}