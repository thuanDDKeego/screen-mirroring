package com.abc.mirroring.ui.premium

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.NonNull
import com.abc.mirroring.BuildConfig
import com.abc.mirroring.R
import com.abc.mirroring.base.BaseActivity
import com.abc.mirroring.config.AppConfigRemote
import com.abc.mirroring.config.AppPreferences
import com.abc.mirroring.databinding.ActivitySubscriptionsBinding
import com.abc.mirroring.ui.policy.PolicyActivity
import com.abc.mirroring.ui.premium.PremiumUtils.Companion.THREE_MONTHS_IN_MILLIS
import com.abc.mirroring.utils.Global
import com.android.billingclient.api.*
import java.text.SimpleDateFormat
import java.util.*

class SubscriptionsActivity : BaseActivity<ActivitySubscriptionsBinding>() {
    private val SUBSCRIPTION_URL = "http://play.google.com/store/account/subscriptions?package=${BuildConfig.APPLICATION_ID}&sku=${Global.SUB_PURCHASE_ID}"
    private lateinit var billingClient: BillingClient
    companion object {
        fun gotoActivity(activity: Activity) {
            val intent = Intent(activity, SubscriptionsActivity::class.java)
            activity.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (AppConfigRemote().isHalloweenTheme == true) {
            setTheme(R.style.Theme_Halloween_NoActionBar)
        }
        super.onCreate(savedInstanceState)
    }

    override fun initBinding(): ActivitySubscriptionsBinding =
        ActivitySubscriptionsBinding.inflate(layoutInflater)

    override fun initViews() {
        if(AppConfigRemote().isHalloweenTheme == true) {
            binding.constrSubscription.background = resources.getDrawable(R.mipmap.bg_premium_halloween)
        }
        getPrice()
        val expiryDate = PremiumUtils.getExpiryTime(AppPreferences().purchaseDate!!, THREE_MONTHS_IN_MILLIS)
        val date = Date(expiryDate)
        val expiryDateFormat = SimpleDateFormat("MM/dd/yyyy")
        binding.txtExpiryDate.text = getString(R.string.expire_on, expiryDateFormat.format(date))

    }

    override fun initActions() {
        binding.apply {
            btnClose.setOnClickListener {
                onBackPressed()
            }
            txtCancel.setOnClickListener {
                openPlaystoreAccount()
            }
            txtTermAndPolicy.setOnClickListener {
                PolicyActivity.gotoActivity(this@SubscriptionsActivity)
            }
        }
    }

    private fun getPrice() {
        billingClient = BillingClient.newBuilder(this).enablePendingPurchases()
            .setListener { _: BillingResult?, _: List<Purchase?>? -> }
            .build()
        val finalBillingClient: BillingClient = billingClient
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {}
            override fun onBillingSetupFinished(@NonNull billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    PremiumUtils.showProducts(finalBillingClient) { productDetailsList ->
                        for (productDetails in productDetailsList) {
                            if (productDetails.productId == Global.SUB_PURCHASE_ID) {
                                val subDetails =
                                    productDetails.subscriptionOfferDetails!!
//                    Log.d("testOffer", subDetails[0])
                                binding.apply {
                                    txtSubscriptionPrice.text = "${
                                        subDetails[0].pricingPhases.pricingPhaseList[0]
                                            .formattedPrice
                                    }/${getString(R.string.three_months)}"
                                }
                            }
                        }
                    }
                } else {
                    binding.txtSubscriptionPrice.text = getString(R.string.price_and_period,"1.99$" )
                }
            }
        })
    }


    private fun openPlaystoreAccount() {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(SUBSCRIPTION_URL)))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "Cant open the browser", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::billingClient.isInitialized) {
            billingClient.endConnection()
        }
    }

}