package com.abc.mirroring.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.abc.mirroring.R
import com.abc.mirroring.ads.AdmobHelper
import com.abc.mirroring.config.AppConfigRemote
import com.abc.mirroring.config.AppPreferences
import com.abc.mirroring.databinding.ActivitySplashBinding
import com.abc.mirroring.ui.home.HomeActivity
import com.abc.mirroring.ui.premium.billing.BillingConnection
import com.abc.mirroring.utils.FirebaseTracking
import com.android.billingclient.api.*
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import one.shot.haki.ads.AdCenter
import timber.log.Timber
import javax.inject.Inject

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {
    @Inject
    lateinit var appConfigRemote: AppConfigRemote
    private lateinit var billingClient: BillingClient

    @Inject
    lateinit var admobHelper: AdmobHelper
    private lateinit var binding: ActivitySplashBinding
    private val TIME_DISPLAY_ONBOARD = 2000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkSubscription()
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
        setupAnimation()
        showAds()
        FirebaseTracking.logSplashShowed()

    }

    fun showAds() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val flowResultJob: Flow<Boolean?> = channelFlow {
                    launch {
                        Timber.i("Loading Splash with App Open Ads")
                        send(AdCenter.getInstance().appOpen?.hardload(this@SplashActivity))
                    }
                    launch {
                        Timber.i("Loading interstitial with App Open Ads")
                        send(AdCenter.getInstance().interstitial?.hardload(this@SplashActivity))
                    }
                }
                withTimeout(30000) {
                    flowResultJob.collect {
                        if (it == true) {
                            cancel()
                        }
                    }
                }
            } finally {
                val goToHomeActivity = {
                    goToHome()
                }
                if (AdCenter.getInstance().appOpen?.isAdAvailable() == true) {
                    Timber.d("splash available")
                    AdCenter.getInstance().appOpen?.enableAddWithActivity(SplashActivity::class.java)
                    AdCenter.getInstance().appOpen?.show(this@SplashActivity) {
                        goToHomeActivity()
                    }
                } else if (AdCenter.getInstance().interstitial?.isAvailable() == true) {
                    Timber.d("interstitial available")
                    AdCenter.getInstance().interstitial?.show(this@SplashActivity) {
                        goToHomeActivity()
                    }
                } else {
                    Toast.makeText(this@SplashActivity, "Timeout", Toast.LENGTH_SHORT).show()
                    Timber.d("splash timeout")
                    Firebase.analytics.logEvent("splash_TIMEOUT") {}
                    goToHomeActivity()
                }
            }
        }
    }

    private fun setupAnimation() {
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

        binding.apply {
            cardViewLogo.startAnimation(animationIconSet)
            txtTitleSplash.startAnimation(animationTitleSet)
            txtContentSplash.startAnimation(animFadeIn)
            viewLoadBar.startAnimation(animMoveRightLoadBar)
        }
    }

    private fun clearAnimation() {
        binding.apply {
            cardViewLogo.clearAnimation()
            txtTitleSplash.clearAnimation()
            txtContentSplash.clearAnimation()
            viewLoadBar.clearAnimation()
        }
    }

    private fun checkSubscription() {
        val billingConnection = BillingConnection()
        billingConnection.checkPremiumUser(this) {
            AppPreferences().isPremiumSubscribed = it
        }
    }

    private fun goToHome() {
        startActivity(Intent(this@SplashActivity, HomeActivity::class.java))
        finish()
    }

    override fun onDestroy() {
        if (::billingClient.isInitialized) {
            billingClient.endConnection()
        }
        clearAnimation()
        super.onDestroy()
    }
}