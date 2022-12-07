package com.abc.mirroring.ui.premium.billing

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.abc.mirroring.BuildConfig
import com.abc.mirroring.utils.Global
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import timber.log.Timber


class BillingConnection(private val activity: Activity, mListener: PurchasesUpdatedListener? = null) {

    companion object {

        private const val SUBSCRIPTION_ID: String = "subs_no_ads"

        private const val SUBSCRIPTION_URL =
            "http://play.google.com/store/account/subscriptions?package=${BuildConfig.APPLICATION_ID}&sku=$SUBSCRIPTION_ID"

        fun openSubscriptionGooglePlay(context: Context, onError: () -> Unit) {
            try {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(SUBSCRIPTION_URL)))
            } catch (e: ActivityNotFoundException) {
                e.printStackTrace()
                onError.invoke()
            }
        }
    }

    private lateinit var billingClient: BillingClient
    private var productDetails: ProductDetails? = null
    private var listener = mListener ?: PurchasesUpdatedListener { _, _ ->
    }

    init {
        initGoogleBilling()
    }

    private fun initGoogleBilling() {
        billingClient = BillingClient.newBuilder(activity)
            .setListener(listener)
            .enablePendingPurchases()
            .build()
    }

    fun establishConnection( onError: () -> Unit, onSuccess: (List<ProductDetails>) -> Unit) {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    showProducts(billingClient) { listProductDetails ->
                        onSuccess(listProductDetails)
//                        for (productDetail in productDetails) {
//                            if (productDetails.productId == Global.SUB_PURCHASE_ID) {
//                                val subDetails =
//                                    productDetails.subscriptionOfferDetails!!
////                    Log.d("testOffer", subDetails[0])
//                                binding.apply {
//                                    txtPurchaseState.text = "${
//                                        subDetails[0].pricingPhases.pricingPhaseList[0]
//                                            .formattedPrice
//                                    }"
//                                    btnUpgrade.setOnClickListener {
//                                        dialogCenter.showLoadingProgressBar()
//                                        launchPurchaseFlow(productDetails)
//                                    }
//                                }
//                            }
//                        }
//                    }
                    }
                } else {
                    onError()
                }
            }

            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                establishConnection(onSuccess) {
                    //Don't invoke onError when trying reconnect
                }
            }
        })
    }

    fun launchPurchaseFlow(productDetails: ProductDetails) {
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
        val billingResult =
            billingClient.launchBillingFlow(activity, billingFlowParams)
    }

    private fun verifySubPurchase(purchases: Purchase, onSuccess: () -> Unit) {
        val acknowledgePurchaseParams = AcknowledgePurchaseParams
            .newBuilder()
            .setPurchaseToken(purchases.purchaseToken)
            .build()
        billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                //user prefs to set premium
                onSuccess()
//                Toast.makeText(
//                    this@PremiumActivity,
//                    getString(R.string.you_are_premium_user),
//                    Toast.LENGTH_SHORT
//                )
//                    .show()
//                //Setting premium to 1
//                // 1 - premium
//                // 0 - no premium
////                prefs.setPremium(1)
            }
        }
        Timber.d("Purchase Token: " + purchases.purchaseToken)
        Timber.d("Purchase Time: " + purchases.purchaseTime)
        Timber.d("Purchase OrderID: " + purchases.orderId)
    }
}

fun showProducts(
    billingClient: BillingClient,
    setPriceCallback: ((List<ProductDetails>) -> Unit)? = null,
) {
    val productList = mutableListOf(
        //Product 1 = index is 0
        QueryProductDetailsParams.Product.newBuilder()
            .setProductId(Global.SUB_PURCHASE_ID)
            .setProductType(BillingClient.ProductType.SUBS)
            .build()
    )
    val params = QueryProductDetailsParams.newBuilder()
        .setProductList(productList)
        .build()
    billingClient.queryProductDetailsAsync(
        params
    ) { billingResult: BillingResult?, productDetailsList: List<ProductDetails> ->
        setPriceCallback?.invoke(productDetailsList)
        // Process the result
//                for (productDetails in productDetailsList) {
//                    if (productDetails.productId == Global.SUB_PURCHASE_ID) {
//                        val subDetails =
//                            productDetails.subscriptionOfferDetails!!
////                    Log.d("testOffer", subDetails[0])
//                        binding.apply {
//                            txtPurchaseState.text = "${
//                                subDetails[0].pricingPhases.pricingPhaseList[0]
//                                    .formattedPrice
//                            }"
//                            btnUpgrade.setOnClickListener {
//                                showLoadingProgressBar()
//                                launchPurchaseFlow(productDetails)
//                            }
//                        }
//                    }
//                }
    }
}

}