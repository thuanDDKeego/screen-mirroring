package com.abc.sreenmirroring.ui.splash

import android.os.Bundle
import android.view.animation.AnimationSet
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.abc.mirroring.R
import com.abc.mirroring.config.AppPreferences
import com.abc.mirroring.databinding.ActivitySplashBinding
import com.abc.mirroring.utils.FirebaseTracking
import com.soft.slideshow.ads.AppOpenManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay


class SplashActivity : AppCompatActivity() {
    private lateinit var job: Job
    private lateinit var binding: ActivitySplashBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (AppPreferences().isTheFirstTimeUseApp == true) {
            setTheme(R.style.OnboardTheme)
            FirebaseTracking.logOnBoardingShowed()
        } else {
            binding = ActivitySplashBinding.inflate(layoutInflater)
            setContentView(binding.root)
            FirebaseTracking.logSplashShowed()
            setupSplashView()
            lifecycleScope.launchWhenStarted { delay(1600)
                AppOpenManager.instance?.showAdAtSplash(this@SplashActivity)
            }
        }
    }

    private fun setupSplashView() {
        val animMoveDown = AnimationUtils.loadAnimation(
            applicationContext,
            R.anim.move_down
        )
        val animFadeIn = AnimationUtils.loadAnimation(
            applicationContext,
            R.anim.fade_in
        )

        val animMoveRight = AnimationUtils.loadAnimation(
            applicationContext,
            R.anim.move_right
        )
        val animationSet = AnimationSet(false)
        animationSet.addAnimation(animMoveDown)
        animationSet.addAnimation(animFadeIn)
        binding.cardViewLogo.startAnimation(animationSet)
        binding.txtTitleSplash.startAnimation(animMoveRight)
        binding.txtContentSplash.startAnimation(animFadeIn)
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}