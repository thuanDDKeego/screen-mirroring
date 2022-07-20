package com.abc.sreenmirroring.ui.browsermirror

import android.app.Activity
import android.content.Intent
import com.abc.sreenmirroring.base.BaseActivity
import com.abc.sreenmirroring.databinding.ActivityBrowserMirrorBinding

class BrowserMirrorActivity : BaseActivity<ActivityBrowserMirrorBinding>() {

    companion object {
        fun gotoActivity(activity: Activity) {
            val intent = Intent(activity, BrowserMirrorActivity::class.java)
            activity.startActivity(intent)
        }
    }

    override fun initBinding() = ActivityBrowserMirrorBinding.inflate(layoutInflater)

    override fun initViews() {

        }

    override fun initActions() {
    }
}