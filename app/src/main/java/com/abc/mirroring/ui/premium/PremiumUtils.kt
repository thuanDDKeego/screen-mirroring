package com.abc.mirroring.ui.premium

import android.text.format.DateUtils
import com.abc.mirroring.utils.Global
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.QueryProductDetailsParams

class PremiumUtils {
    companion object {
        const val THREE_MONTHS_IN_MILLIS = DateUtils.YEAR_IN_MILLIS / 4
        fun getExpiryTime(
            purchaseTime: Long,
            subscriptionPeriod: Long = DateUtils.YEAR_IN_MILLIS
        ): Long {
            val currentTime = System.currentTimeMillis()
            var yearFromPurchase: Double =
                (currentTime - purchaseTime).toDouble() / subscriptionPeriod.toDouble()
            val yearsInInt =
                if ((currentTime - purchaseTime) % subscriptionPeriod != 0L) yearFromPurchase.toInt() + 1 else yearFromPurchase.toInt()
            val expiryTime = purchaseTime + yearsInInt * subscriptionPeriod
            return expiryTime
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
}