package com.abc.mirroring.ui.splash

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.abc.mirroring.R
import com.abc.mirroring.config.AppPreferences
import com.abc.mirroring.utils.FirebaseTracking
import com.soft.slideshow.ads.AppOpenManager
import kotlinx.coroutines.Job


class SplashActivity : AppCompatActivity() {
    private lateinit var job: Job
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (AppPreferences().isTheFirstTimeUseApp == true) {
            setTheme(R.style.OnboardTheme)
            FirebaseTracking.logOnBoardingShowed()
        } else {
            FirebaseTracking.logSplashShowed()
        }
        AppOpenManager.instance?.showAdAtSplash(this@SplashActivity)
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}