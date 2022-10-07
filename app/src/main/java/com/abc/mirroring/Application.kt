package com.abc.mirroring

import AdType
import android.app.Application
import androidx.annotation.NonNull
import com.abc.mirroring.ads.AppOpenManager
import com.abc.mirroring.config.AppPreferences
import com.abc.mirroring.config.Preferences
import com.abc.mirroring.config.ReleaseTree
import com.abc.mirroring.ui.splash.SplashActivity
import com.android.billingclient.api.*
import com.elvishew.xlog.LogLevel
import com.elvishew.xlog.XLog
import com.google.android.gms.ads.AdActivity
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber


@HiltAndroidApp
class Application : Application() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(ReleaseTree())
        }
        Preferences.init(this)
        initLogger()
        checkSubscription()
        AppOpenManager.instance?.init(this, this.getString(AdType.APP_OPEN.adsId))
        AppOpenManager.instance?.disableAddWithActivity(AdActivity::class.java)
        AppOpenManager.instance?.disableAddWithActivity(SplashActivity::class.java)
    }

    private fun initLogger() {
        if (BuildConfig.DEBUG) {
            XLog.init(LogLevel.ALL)
        } else {
            XLog.init(LogLevel.ERROR)
        }
    }

    fun checkSubscription() {
        var billingClient = BillingClient.newBuilder(this).enablePendingPurchases()
            .setListener { _: BillingResult?, _: List<Purchase?>? -> }
            .build()
        val finalBillingClient: BillingClient = billingClient
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {}
            override fun onBillingSetupFinished(@NonNull billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    finalBillingClient.queryPurchasesAsync(
                        QueryPurchasesParams.newBuilder()
                            .setProductType(BillingClient.ProductType.SUBS).build()
                    ) { billingResult1: BillingResult, list: List<Purchase> ->
                        if (billingResult1.responseCode == BillingClient.BillingResponseCode.OK) {
                            Timber.d("testOffer" + list.size.toString() + " size")
                            if (list.isNotEmpty()) {
                                AppPreferences().isPremiumActive = true
                                for ((i, purchase) in list.withIndex()) {
                                    //Here you can manage each product, if you have multiple subscription
                                    Timber.d(
                                        "testOffer",
                                        purchase.originalJson
                                    ) // Get to see the order information
                                    Timber.d("testOffer", " index$i")
                                }
                            } else {
                                AppPreferences().isPremiumActive = false // set 0 to de-activate premium feature
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
}
