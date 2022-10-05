package com.abc.mirroring.ui.premium

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.abc.mirroring.databinding.ActivityPremiumBinding
import com.android.billingclient.api.*
import timber.log.Timber


class PremiumActivity : AppCompatActivity() {
    private lateinit var billingClient: BillingClient
    private lateinit var binding: ActivityPremiumBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPremiumBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initGoogleBilling()
        establishConnection()
    }

    private fun initGoogleBilling() {

        val purchasesUpdatedListener =
            PurchasesUpdatedListener { billingResult, purchases ->
                // To be implemented in a later section.
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                    for (purchase in purchases) {
                        verifySubPurchase(purchase)
                    }
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
                    showProducts()
                }
            }

            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                establishConnection()
            }
        })
    }

    fun showProducts() {
        val productList = mutableListOf(
            //Product 1 = index is 0
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("sub_premium")
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()
        billingClient.queryProductDetailsAsync(
            params
        ) { billingResult: BillingResult?, productDetailsList: List<ProductDetails> ->
            // Process the result
            for (productDetails in productDetailsList) {
                if (productDetails.productId == "sub_premium") {
                    val subDetails: List<*> =
                        productDetails.subscriptionOfferDetails!!
//                    Log.d("testOffer", subDetails[0])
//                    txt_price.setText(
//                        subDetails[0].getPricingPhases().getPricingPhaseList().get(0)
//                            .getFormattedPrice() + " Per Month"
//                    )
//                    txt_price.setOnClickListener { view -> launchPurchaseFlow(productDetails) }
                    binding.test.setOnClickListener {
                        launchPurchaseFlow(productDetails)
                    }
                }
//                if (productDetails.productId == "test_id_shar") {
//                    val subDetails: List<*> =
//                        productDetails.subscriptionOfferDetails!!
////                    Log.d("testOffer", subDetails[1].getOfferToken())
////                    offer_btn.setText(
////                        subDetails[1].getPricingPhases().getPricingPhaseList().get(0)
////                            .getFormattedPrice() + " Per Month"
////                    )
////                    offer_btn.setOnClickListener { view -> launchPurchaseFlow(productDetails) }
//                }
            }
        }
    }

    fun launchPurchaseFlow(productDetails: ProductDetails) {
        assert(productDetails.subscriptionOfferDetails != null)
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

    fun verifySubPurchase(purchases: Purchase) {
        val acknowledgePurchaseParams = AcknowledgePurchaseParams
            .newBuilder()
            .setPurchaseToken(purchases.purchaseToken)
            .build()
        billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
            if (billingResult.getResponseCode() === BillingClient.BillingResponseCode.OK) {
                //user prefs to set premium
                Toast.makeText(
                    this@PremiumActivity,
                    "You are a premium user now",
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
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()
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
