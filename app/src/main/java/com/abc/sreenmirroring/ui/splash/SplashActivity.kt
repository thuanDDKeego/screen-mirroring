package com.abc.sreenmirroring.ui.splash

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.abc.sreenmirroring.R
import com.abc.sreenmirroring.config.AppPreferences
import com.soft.slideshow.ads.AppOpenManager
import kotlinx.coroutines.Job


class SplashActivity : AppCompatActivity() {
    private lateinit var job: Job
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (AppPreferences().isTheFirstTimeUseApp == true) {
            setTheme(R.style.OnboardTheme)
        }
        AppOpenManager.instance?.showAdAtSplash(this@SplashActivity)
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}