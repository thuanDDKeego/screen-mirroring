package com.abc.sreenmirroring.ui.home

import android.view.View
import com.abc.sreenmirroring.base.BaseActivity
import com.abc.sreenmirroring.databinding.ActivityHomeBinding
import com.abc.sreenmirroring.databinding.LayoutDialogBrowserMirrorBinding
import com.abc.sreenmirroring.ui.browsermirror.BrowserMirrorActivity
import com.abc.sreenmirroring.ui.tutorial.TutorialActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeActivity : BaseActivity<ActivityHomeBinding>() {
    private var browserDialog = false
    private lateinit var dialogBinding: LayoutDialogBrowserMirrorBinding

    override fun initBinding() = ActivityHomeBinding.inflate(layoutInflater)

    override fun initViews() {
    }

    override fun initActions() {
        binding.constraintBrowserMirror.setOnClickListener {
            showRatingDialog()

        }
        binding.imgHelp.setOnClickListener {
            TutorialActivity.gotoActivity(this@HomeActivity)
        }
    }


    protected fun showRatingDialog(autoShow: Boolean = true) {
//        if (browserDialog) return
//        browserDialog = true
        dialogBinding = LayoutDialogBrowserMirrorBinding.inflate(layoutInflater, binding.root, true)
        dialogBinding.txtClose.setOnClickListener {
            dialogBinding.root.visibility = View.GONE
        }
        CoroutineScope(Dispatchers.Main).launch {
            delay(5000)
            BrowserMirrorActivity.gotoActivity(this@HomeActivity)
        }


    }
}