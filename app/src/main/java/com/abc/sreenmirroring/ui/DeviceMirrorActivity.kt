package com.abc.sreenmirroring.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.abc.sreenmirroring.base.BaseActivity
import com.abc.sreenmirroring.databinding.ActivityDeviceMirrorBinding

class DeviceMirrorActivity : BaseActivity<ActivityDeviceMirrorBinding>() {
    override fun initBinding()= ActivityDeviceMirrorBinding.inflate(layoutInflater)

    override fun initViews() {}

    override fun initActions() {
    }

}