package com.abc.mirroring.ui.policy

import android.app.Activity
import android.content.Intent
import com.abc.mirroring.base.BaseActivity
import com.abc.mirroring.databinding.ActivityPolicyBinding
import com.abc.mirroring.utils.Global

class PolicyActivity : BaseActivity<ActivityPolicyBinding>() {

    companion object {
        fun gotoActivity(activity: Activity) {
            val intent = Intent(activity, PolicyActivity::class.java)
            activity.startActivity(intent)
        }
    }

    override fun initBinding() = ActivityPolicyBinding.inflate(layoutInflater)

    override fun initViews() {
        binding.webview.loadUrl(Global.POLICY)
    }

    override fun initActions() {
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }
    }
}