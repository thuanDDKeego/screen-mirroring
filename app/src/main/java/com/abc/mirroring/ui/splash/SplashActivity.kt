package com.abc.mirroring.ui.splash

import AdType
import android.content.Intent
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.abc.mirroring.R
import com.abc.mirroring.ads.AdmobHelper
import com.abc.mirroring.config.AppConfigRemote
import com.abc.mirroring.config.AppPreferences
import com.abc.mirroring.databinding.ActivitySplashBinding
import com.abc.mirroring.ui.home.HomeActivity
import com.abc.mirroring.utils.FirebaseTracking
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.abc.mirroring.ads.AppOpenManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {
    @Inject
    lateinit var appConfigRemote: AppConfigRemote

    @Inject
    lateinit var admobHelper: AdmobHelper
    private lateinit var binding: ActivitySplashBinding
    private val TIME_DISPLAY_ONBOARD = 3000L
    private var jobTimeOutOpenApp: Job? = null
    private var showOpenAds = true

    //    private var jobTimeOut: Job? = null
    private var jobLoadAd: Job? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (AppPreferences().isTheFirstTimeUseApp == true) {
            setTheme(R.style.OnboardTheme)
            FirebaseTracking.logOnBoardingShowed()
            lifecycleScope.launchWhenStarted {
                delay(TIME_DISPLAY_ONBOARD)
                goToHome()
            }
        } else {
            setUpSplashAction()
        }
    }

    private fun setUpSplashAction() {
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AppOpenManager.instance?.resetAdOpenAd()
        val startTime = System.currentTimeMillis()
        setupSplashView()
        FirebaseTracking.logSplashShowed()
        if (AppPreferences().isPremiumActive == true) {
            CoroutineScope(Dispatchers.Main).launch {
                delay(2000L)
                startActivity(
                    Intent(
                        this@SplashActivity,
                        HomeActivity::class.java
                    )
                )
                finish()
            }
        } else {
            if (appConfigRemote.isUsingAdsOpenApp == true) {
                jobTimeOutOpenApp = CoroutineScope(Dispatchers.Main).launch {
                    delay(appConfigRemote.adsTimeout!!.toLong())
                    startActivity(Intent(this@SplashActivity, HomeActivity::class.java))
                    showOpenAds = false
                    finish()  /*set timeout for splash*/
                }
                jobLoadAd = CoroutineScope(Dispatchers.Main).launch {
                    AppOpenManager.instance?.fetchAd {
                        val timeFromStart = System.currentTimeMillis() - startTime
                        CoroutineScope(Dispatchers.Main).launch {
                            jobTimeOutOpenApp?.cancel()
                            if (timeFromStart < 1600) {
                                delay(1600L - timeFromStart)
                            }
                            if (showOpenAds) {
                                AppOpenManager.instance?.showAdAtSplash(this@SplashActivity) {
                                    startActivity(
                                        Intent(
                                            this@SplashActivity,
                                            HomeActivity::class.java
                                        )
                                    )
                                    finish()
                                }
                            }
                        }
                    }
                }
            } else {
                admobHelper.loadAdInterstitial(
                    this@SplashActivity,
                    AdType.SPLASH_INTERSTITIAL
                ) { interstitialAd ->
                    interstitialAd?.fullScreenContentCallback =
                        object : FullScreenContentCallback() {
                            override fun onAdDismissedFullScreenContent() {
                                super.onAdDismissedFullScreenContent()
                                admobHelper.resetInterstitialAd(AdType.SPLASH_INTERSTITIAL)
                                goToHome()
                            }

                            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                                super.onAdFailedToShowFullScreenContent(p0)
                                goToHome()
                                admobHelper.resetInterstitialAd(AdType.SPLASH_INTERSTITIAL)
                                admobHelper.loadAdInterstitial(
                                    this@SplashActivity,
                                    AdType.SPLASH_INTERSTITIAL
                                ) {}

                            }
                        }
                    jobLoadAd = CoroutineScope(Dispatchers.Main).launch {
                        val timeFromStart = System.currentTimeMillis() - startTime
                        CoroutineScope(Dispatchers.Main).launch {
                            if (timeFromStart < 1600) {
                                delay(1600L - timeFromStart)
                            }
//                            jobTimeOut?.cancel()
                            if (interstitialAd != null) {
                                interstitialAd.show(this@SplashActivity)
                            } else {
                                goToHome()
                            }
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

    private fun goToHome() {
        startActivity(Intent(this@SplashActivity, HomeActivity::class.java))
        finish()
    }

    override fun onBackPressed() {
    }
}