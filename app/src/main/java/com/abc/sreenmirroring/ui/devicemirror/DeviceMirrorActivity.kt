package com.abc.sreenmirroring.ui.devicemirror

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import com.abc.sreenmirroring.base.BaseActivity
import com.abc.sreenmirroring.databinding.ActivityDeviceMirrorBinding
import com.abc.sreenmirroring.utils.Global

class DeviceMirrorActivity : BaseActivity<ActivityDeviceMirrorBinding>() {

    companion object {
        fun gotoActivity(activity: Activity) {
            val intent = Intent(activity, DeviceMirrorActivity::class.java)
            activity.startActivity(intent)
        }
    }

    override fun initBinding() = ActivityDeviceMirrorBinding.inflate(layoutInflater)

    override fun initViews() {}

    override fun initActions() {
        binding.btnSelectDevice.setOnClickListener {
            selectDeviceMirror()
        }
    }

    private fun selectDeviceMirror() {
        try {
            Global.SELECT_FROM_SETTING = true
            startActivity(Intent("android.settings.WIFI_DISPLAY_SETTINGS"))
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
            try {
                Global.SELECT_FROM_SETTING = true
                startActivity(Intent("com.samsung.wfd.LAUNCH_WFD_PICKER_DLG"))
            } catch (e2: java.lang.Exception) {
                try {
                    Global.SELECT_FROM_SETTING = true
                    startActivity(Intent("android.settings.CAST_SETTINGS"))
                } catch (e3: java.lang.Exception) {
                    Toast.makeText(
                        this@DeviceMirrorActivity,
                        "not support device",
                        Toast.LENGTH_LONG
                    )
                        .show()
                }
            }
        }
    }

}

