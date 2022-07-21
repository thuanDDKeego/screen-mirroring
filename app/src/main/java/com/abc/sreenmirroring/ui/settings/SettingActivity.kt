package com.abc.sreenmirroring.ui.settings

import android.app.Activity
import android.content.Intent
import com.abc.sreenmirroring.base.BaseActivity
import com.abc.sreenmirroring.databinding.ActivitySettingBinding

class SettingActivity : BaseActivity<ActivitySettingBinding>() {

    companion object {
        fun gotoActivity(activity: Activity) {
            val intent = Intent(activity, SettingActivity::class.java)
            activity.startActivity(intent)
        }
    }

    override fun initBinding() = ActivitySettingBinding.inflate(layoutInflater)

    override fun initViews() {
    }

    override fun initActions() {

    }

}