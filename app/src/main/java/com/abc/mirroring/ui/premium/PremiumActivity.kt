package com.abc.mirroring.ui.premium

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import com.abc.mirroring.R
import com.abc.mirroring.base.BaseActivity
import com.abc.mirroring.config.AppConfigRemote
import com.abc.mirroring.config.AppPreferences
import com.abc.mirroring.databinding.ActivityPremiumBinding
import com.abc.mirroring.ui.dialog.DialogCenter
import com.abc.mirroring.ui.premium.PremiumUtils.Companion.showProducts
import com.abc.mirroring.utils.Global
import com.android.billingclient.api.*
import dev.sofi.ads.AdCenter
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

enum class ScreenState { HAS_SUBSCRIBED, HASNT_SUBSCRIBED }

class PremiumActivity : BaseActivity<ActivityPremiumBinding>() {
    private var isPremiumActive = AppPreferences().isPremiumSubscribed == true
    private lateinit var billingClient: BillingClient
    private lateinit var screenState: ScreenState
    private lateinit var dialogCenter: DialogCenter

    companion object {
        fun gotoActivity(activity: Activity) {
            val intent = Intent(activity, PremiumActivity2::class.java)
            activity.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (AppConfigRemote().isHalloweenTheme == true) {
            setTheme(R.style.Theme_Halloween_NoActionBar)
            dialogCenter = DialogCenter(this)
        }
        super.onCreate(savedInstanceState)
    }

    override fun initBinding() = ActivityPremiumBinding.inflate(layoutInflater)

    override fun initViews() {
        if (AppConfigRemote().isHalloweenTheme == true) {
            binding.constrPremium.background = resources.getDrawable(R.mipmap.bg_premium_halloween)
        }
        val animBtnUpgrade = AnimationUtils.loadAnimation(
            applicationContext,
            R.anim.alpha_scale_upgrade
        )
        binding.btnUpgrade.startAnimation(animBtnUpgrade)
        if (isPremiumActive) {
            screenState = ScreenState.HAS_SUBSCRIBED
            updateView(screenState)
        } else {
            initGoogleBilling()
            establishConnection()
            screenState = ScreenState.HASNT_SUBSCRIBED
            updateView(screenState)

        }
    }

    @SuppressLint("SimpleDateFormat")
    fun updateView(screenState: ScreenState) {
        this.screenState = screenState
        when (this.screenState) {
            ScreenState.HASNT_SUBSCRIBED -> {
                binding.apply {
                    btnUpgrade.visibility = View.VISIBLE
                    txtExpiryDate.visibility = View.INVISIBLE
                    txtPurchaseState.visibility = View.VISIBLE
                    imgCrown.setImageResource(R.drawable.ic_crown)
                }
            }
            ScreenState.HAS_SUBSCRIBED -> {
                val expiryDate = PremiumUtils.getExpiryTime(
                    AppPreferences().purchaseDate!!,
                    PremiumUtils.THREE_MONTHS_IN_MILLIS
                )
                val date = Date(expiryDate)
                val expiryDateFormat = SimpleDateFormat("MM/dd/yyyy")
                binding.apply {
                    btnUpgrade.clearAnimation()
                    btnUpgrade.visibility = View.INVISIBLE
                    txtExpiryDate.visibility = View.VISIBLE
                    txtExpiryDate.text =
                        getString(R.string.expire_on, expiryDateFormat.format(date))
                    txtPurchaseState.visibility = View.GONE
                    txtPurchaseDes.text = getString(R.string.thanks_for_using_app)
                    imgCrown.setImageResource(R.drawable.ic_success)

                }
            }
//            ScreenState.SUCCESS -> {
//                binding.apply {
//                    btnUpgrade.clearAnimation()
//                    btnUpgrade.visibility = View.INVISIBLE
//                    txtExpiryDate.visibility = View.VISIBLE
//                    txtPurchaseState.visibility = View.GONE
//                    txtPurchaseDes.text = getString(R.string.thanks_for_using_app)
//                    imgCrown.setImageResource(R.drawable.ic_success)
//                }
//            }
        }
    }

    override fun initActions() {
        binding.btnClose.setOnClickListener {
            finish()
        }
    }

    private fun initGoogleBilling() {
        val purchasesUpdatedListener =
            PurchasesUpdatedListener { billingResult, purchases ->
                // To be implemented in a later section.
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                    dialogCenter.dismissLoadingBarDialog()
                    AppPreferences().isPremiumSubscribed = true
                    AdCenter.getInstance().enable.value = false
                    AppPreferences().purchaseDate = purchases[0].purchaseTime
                    updateView(ScreenState.HAS_SUBSCRIBED)
                    for (purchase in purchases) {
                        verifySubPurchase(purchase)
                    }
                }
//if item already subscribed then check and reflect changes
                //...
//if Purchase canceled
                else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
                    dialogCenter.dismissLoadingBarDialog()
                    Toast.makeText(
                        this@PremiumActivity,
                        getString(R.string.purchase_cancel),
                        Toast.LENGTH_SHORT
                    ).show()
                }
// Handle any other error msgs
                else {
                    dialogCenter.dismissLoadingBarDialog()
                    Toast.makeText(
                        this@PremiumActivity,
                        "Error: " + billingResult.debugMessage,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        billingClient = BillingClient.newBuilder(this@PremiumActivity)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()
    }

    fun establishConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    showProducts(billingClient) { productDetailsList ->
                        for (productDetails in productDetailsList) {
                            if (productDetails.productId == Global.SUB_PURCHASE_ID) {
                                val subDetails =
                                    productDetails.subscriptionOfferDetails!!
//                    Log.d("testOffer", subDetails[0])
                                binding.apply {
                                    txtPurchaseState.text = "${
                                        subDetails[0].pricingPhases.pricingPhaseList[0]
                                            .formattedPrice
                                    }"
                                    btnUpgrade.setOnClickListener {
                                        dialogCenter.showLoadingProgressBar()
                                        launchPurchaseFlow(productDetails)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                establishConnection()
            }
        })
    }


    private fun launchPurchaseFlow(productDetails: ProductDetails) {
        assert(productDetails.subscriptionOfferDetails != null && productDetails.subscriptionOfferDetails!!.isNotEmpty())
        val productDetailsParamsList = mutableListOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .setOfferToken(productDetails.subscriptionOfferDetails!![0].offerToken)
                .build()
        )
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()
        val billingResult = billingClient.launchBillingFlow(this@PremiumActivity, billingFlowParams)
    }

    private fun verifySubPurchase(purchases: Purchase) {
        val acknowledgePurchaseParams = AcknowledgePurchaseParams
            .newBuilder()
            .setPurchaseToken(purchases.purchaseToken)
            .build()
        billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                //user prefs to set premium
                Toast.makeText(
                    this@PremiumActivity,
                    getString(R.string.you_are_premium_user),
                    Toast.LENGTH_SHORT
                )
                    .show()
                //Setting premium to 1
                // 1 - premium
                // 0 - no premium
//                prefs.setPremium(1)
            }
        }
        Timber.d("Purchase Token: " + purchases.purchaseToken)
        Timber.d("Purchase Time: " + purchases.purchaseTime)
        Timber.d("Purchase OrderID: " + purchases.orderId)
    }

    override fun onResume() {
        super.onResume()
        updateView(screenState)
        if (!isPremiumActive) {
            billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS)
                    .build()
            ) { billingResult: BillingResult, list: List<Purchase> ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    for (purchase in list) {
                        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged) {
                            verifySubPurchase(purchase)
                        }
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        if(dialogCenter.mLoadingProgressBarShowing) return
        super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::billingClient.isInitialized) {
            billingClient.endConnection()
        }
    }
}
