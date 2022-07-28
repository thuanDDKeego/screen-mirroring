package com.abc.sreenmirroring.ui.splash

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.abc.sreenmirroring.R
import com.abc.sreenmirroring.config.AppPreferences
import com.abc.sreenmirroring.ui.home.HomeActivity
import kotlinx.coroutines.*


class SplashActivity : AppCompatActivity() {
    private lateinit var job: Job
    private var delayTime = 500L
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (AppPreferences().isTheFirstTimeUseApp == true) {
            setTheme(R.style.OnboardTheme)
            delayTime = 3000L
        }
        job = CoroutineScope(Dispatchers.Main).launch {
            delay(delayTime)
            startActivity(Intent(this@SplashActivity, HomeActivity::class.java))
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}