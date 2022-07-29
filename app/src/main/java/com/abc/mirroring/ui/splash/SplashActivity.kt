package com.abc.sreenmirroring.ui.splash

import android.os.Bundle
import android.view.animation.AnimationSet
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.abc.mirroring.R
import com.abc.mirroring.config.AppPreferences
import com.abc.mirroring.utils.FirebaseTracking
import com.soft.slideshow.ads.AppOpenManager
import kotlinx.coroutines.Job


class SplashActivity : AppCompatActivity() {
    private lateinit var job: Job
    private lateinit var binding: ActivitySplashBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (AppPreferences().isTheFirstTimeUseApp == true) {
            setTheme(R.style.OnboardTheme)
        }
        else {
            binding = ActivitySplashBinding.inflate(layoutInflater)
            setContentView(binding.root)
            setupSplashView()
        }
        AppOpenManager.instance?.showAdAtSplash(this@SplashActivity)
            FirebaseTracking.logOnBoardingShowed()
        } else {
            FirebaseTracking.logSplashShowed()
            AppOpenManager.instance?.showAdAtSplash(this@SplashActivity)
        }
    }

    private fun setupSplashView() {
        val  animMoveDown = AnimationUtils.loadAnimation(applicationContext,
            R.anim.move_down)
        val  animFadeIn = AnimationUtils.loadAnimation(applicationContext,
            R.anim.fade_in)
        val animationSet = AnimationSet(false)
        animationSet.addAnimation(animMoveDown)
        animationSet.addAnimation(animFadeIn)
        binding.cardVieLogo.startAnimation(animationSet)
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}