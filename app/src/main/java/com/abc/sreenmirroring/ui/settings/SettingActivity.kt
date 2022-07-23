package com.abc.sreenmirroring.ui.settings

import android.app.Activity
import android.content.Intent
import android.widget.CompoundButton
import com.abc.sreenmirroring.base.BaseActivity
import com.abc.sreenmirroring.config.AppPreferences
import com.abc.sreenmirroring.databinding.ActivitySettingBinding
import timber.log.Timber

class SettingActivity : BaseActivity<ActivitySettingBinding>() {

    companion object {
        fun gotoActivity(activity: Activity) {
            val intent = Intent(activity, SettingActivity::class.java)
            activity.startActivity(intent)
        }
    }

    override fun initBinding() = ActivitySettingBinding.inflate(layoutInflater)

    override fun initViews() {
        binding.switchOnOffPinCode.isChecked = AppPreferences().isTurnOnPinCode == true

    }

    override fun initActions() {
        binding.switchOnOffPinCode.setOnCheckedChangeListener { _, isChecked ->
            AppPreferences().isTurnOnPinCode = isChecked
        }
        binding.llChangePinCode.setOnClickListener {

        }
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }
    }

}