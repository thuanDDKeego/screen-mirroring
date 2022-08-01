package com.abc.sreenmirroring.ui.splash

import android.content.Intent
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.abc.mirroring.R
import com.abc.mirroring.config.AppPreferences
import com.abc.mirroring.databinding.ActivitySplashBinding
import com.abc.mirroring.ui.home.HomeActivity
import com.abc.mirroring.utils.FirebaseTracking
import com.soft.slideshow.ads.AppOpenManager
import kotlinx.coroutines.*


class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    private var jobTimeOut: Job? = null
    private var jobLoadAd: Job? = null
    private val SPLASH_TIME_OUT = 8000L
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (AppPreferences().isTheFirstTimeUseApp == true) {
            setTheme(R.style.OnboardTheme)
            FirebaseTracking.logOnBoardingShowed()
            lifecycleScope.launchWhenStarted {
                delay(3000)
                startActivity(Intent(this@SplashActivity, HomeActivity::class.java))
                finish()
            }
        } else {
            binding = ActivitySplashBinding.inflate(layoutInflater)
            setContentView(binding.root)
            FirebaseTracking.logSplashShowed()
            jobTimeOut = CoroutineScope(Dispatchers.Main).launch {
                delay(SPLASH_TIME_OUT)
                startActivity(Intent(this@SplashActivity, HomeActivity::class.java))
                finish()
            }
            val startTime = System.currentTimeMillis()
            setupSplashView()
            jobLoadAd = CoroutineScope(Dispatchers.Main).launch {
                AppOpenManager.instance?.fetchAd {
                    var timeFromStart = System.currentTimeMillis() - startTime
                    CoroutineScope(Dispatchers.Main).launch {
                        if (timeFromStart < 1600) {
                            delay(1600L - timeFromStart)
                        }
                        AppOpenManager.instance?.showAdAtSplash(this@SplashActivity) {
                            jobTimeOut?.cancel()
                        }
                    }
                }
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
        val animationIconSet = AnimationSet(false)
        animationIconSet.addAnimation(animMoveDown)
        animationIconSet.addAnimation(animFadeIn)

        val animationTitleSet = AnimationSet(false)
        animationTitleSet.addAnimation(animMoveRight)
        animationTitleSet.addAnimation(animFadeIn)

        val animMoveRightLoadBar = AnimationUtils.loadAnimation(
            applicationContext,
            R.anim.move_right_load_bar
        )
        animMoveRightLoadBar.repeatCount = Animation.INFINITE
        animMoveRightLoadBar.repeatMode = Animation.RESTART

        binding.cardViewLogo.startAnimation(animationIconSet)
        binding.txtTitleSplash.startAnimation(animationTitleSet)
        binding.txtContentSplash.startAnimation(animFadeIn)
        binding.viewLoadBar.startAnimation(animMoveRightLoadBar)
    }

}