package com.abc.mirroring.ui.splash

import AdType
import android.content.Intent
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.AnimationUtils
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
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
import com.android.billingclient.api.*
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {
    @Inject
    lateinit var appConfigRemote: AppConfigRemote
    private lateinit var billingClient: BillingClient

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
        checkSubscription()
        initializeMobileAds()
        if(AppPreferences().isPremiumActive == false) AdmobHelper().loadGeneralAdInterstitial(this)
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
                admobHelper.showGeneralAdInterstitial(
                    this@SplashActivity
                ) {
                    goToHome()
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

    private fun checkSubscription() {
        billingClient = BillingClient.newBuilder(this).enablePendingPurchases()
            .setListener { _: BillingResult?, _: List<Purchase?>? -> }
            .build()
        val finalBillingClient: BillingClient = billingClient
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {}
            override fun onBillingSetupFinished(@NonNull billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Timber.d("On billing finish")
                    finalBillingClient.queryPurchasesAsync(
                        QueryPurchasesParams.newBuilder()
                            .setProductType(BillingClient.ProductType.SUBS).build()
                    ) { billingResult1: BillingResult, list: List<Purchase> ->
                        if (billingResult1.responseCode == BillingClient.BillingResponseCode.OK) {
                            if (list.isNotEmpty()) {
                                Timber.d("Free Premium is active")
                                for ((i, purchase) in list.withIndex()) {
                                    //Here you can manage each product, if you have multiple subscription
                                    // Get to see the order information

                                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                                        AppPreferences().isPremiumActive =
                                            true // set 0 to de-activate premium feature
                                        AppPreferences().purchaseDate = purchase.purchaseTime

                                        break
                                    }
                                    Timber.d("testOffer", " index$i")
                                    AppPreferences().isPremiumActive =
                                        false // set 0 to de-activate premium feature
                                }
                            } else {
                                Timber.d("Free Premium isn't active")
                                AppPreferences().isPremiumActive =
                                    false // set 0 to de-activate premium feature
                            }
                        }
                    }
//                    billingClient.queryPurchasesAsync(BillingClient.ProductType.SUBS){
//                            responseCode, purchasesList ->
//                        if(purchasesList.isNullOrEmpty()){
//                            Timber.d("Purchase App","history for SUBS is empty")
//                        }else{
//                            Timber.d("Purchase App","history subs has ${purchasesList.size} items : ${purchasesList.toString()}")
//                        }
//                    }
                }
            }
        })
    }

    private fun initializeMobileAds() {
        MobileAds.initialize(this) { initializationStatus ->
            val statusMap =
                initializationStatus.adapterStatusMap
            for (adapterClass in statusMap.keys) {
                val status = statusMap[adapterClass]
                Timber.d(
                    "MyApp" + String.format(
                        "Adapter name: %s, Description: %s, Latency: %d",
                        adapterClass, status!!.description, status.latency
                    )
                )
            }

            // Start loading ads here...
        }
    }

    private fun goToHome() {
        startActivity(Intent(this@SplashActivity, HomeActivity::class.java))
        finish()
    }

    override fun onBackPressed() {
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::billingClient.isInitialized) {
            billingClient.endConnection()
        }
    }
}