package com.abc.sreenmirroring.base

import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding

abstract class BaseActivity<V : ViewBinding> : AppCompatActivity() {
    protected lateinit var binding: V

    open var isFullScreen: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = initBinding()
        if (isFullScreen) {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        setContentView(binding.root)
        initViews()
        initActions()
    }

    abstract fun initBinding(): V
    abstract fun initViews()
    abstract fun initActions()
}