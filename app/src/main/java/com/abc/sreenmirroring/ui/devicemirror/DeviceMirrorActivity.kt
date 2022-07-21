package com.abc.sreenmirroring.ui.devicemirror

import android.app.Activity
import android.content.Intent
import com.abc.sreenmirroring.base.BaseActivity
import com.abc.sreenmirroring.databinding.ActivityDeviceMirrorBinding
class DeviceMirrorActivity : BaseActivity<ActivityDeviceMirrorBinding>() {

    companion object {
        fun gotoActivity(activity: Activity) {
            val intent = Intent(activity, DeviceMirrorActivity::class.java)
            activity.startActivity(intent)
        }
    }

    override fun initBinding()= ActivityDeviceMirrorBinding.inflate(layoutInflater)

    override fun initViews() {}

    override fun initActions() {
    }

}