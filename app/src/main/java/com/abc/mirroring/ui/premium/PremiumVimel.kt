package com.abc.mirroring.ui.premium

import android.app.Activity
import android.content.Context
import android.widget.Toast
import com.abc.mirroring.cast.State
import com.abc.mirroring.cast.VimelStateHolder
import com.abc.mirroring.config.AppPreferences
import com.abc.mirroring.ui.premium.billing.BillingConnection
import com.abc.mirroring.ui.premium.billing.ProductPurchase
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.PurchasesUpdatedListener
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.sofi.ads.AdCenter
import javax.inject.Inject


@HiltViewModel
class PremiumVimel @Inject constructor() :
    VimelStateHolder<PremiumVimel.PremiumVimelState>(PremiumVimelState()) {
//    private val TAG = "SUBSCRIPTION test"

    data class PremiumVimelState(
        /*list products has 3 item:
            - monthly
            - life-time - sale 50%
            - yearly - best option
         */
        var monthlySubscription: ProductPurchase = ProductPurchase("", "", "0 $"),
        var yearlySubscription: ProductPurchase = ProductPurchase("", "", "0 $"),
        var oneTimePayment: ProductPurchase = ProductPurchase("", "", "0 $"),
    ) : State

    private lateinit var billingConnection: BillingConnection

    fun createConnectionAndFetchData(activity: Activity) {
        billingConnection = createBillingConnection(activity)
        fetchProducts(activity)
    }

    private fun fetchProducts(context: Context) {
        billingConnection.getProductPurchases(context) { products ->
            if (products.isNotEmpty()) {
                products.forEach { product ->
                    //check if the product is in-app product (onetime)
                    if (product.id == BillingConnection.PRODUCT_ID) {
                        update { state -> state.copy(oneTimePayment = product) }
                    } else {
                        if (product.basePlanId == BillingConnection.MONTHLY_BASE_PLAN_ID) {
                            update { state -> state.copy(monthlySubscription = product) }
                        } else if (product.basePlanId == BillingConnection.YEARLY_BASE_PLAN_ID) {
                            update { state -> state.copy(yearlySubscription = product) }
                        }
                    }
                }
            }
        }
    }

    private fun createBillingConnection(activity: Activity): BillingConnection {
        val listener =
            PurchasesUpdatedListener { billingResult, purchases ->
                // To be implemented in a later section.
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                    AppPreferences().isPremiumSubscribed = true
                    AdCenter.getInstance().enable.value = false
                    billingConnection.checkPremiumUser(activity) {
                    }
                    activity.finish()
                }
//if item already subscribed then check and reflect changes
                //...
//if Purchase canceled
                else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
//            dismissLoadingBarDialog()
                }
// Handle any other error msgs
                else {
//            dismis sLoadingBarDialog()
                    Toast.makeText(
                        activity,
                        "Error: " + billingResult.debugMessage,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        return BillingConnection(listener)
    }


    fun subscribeProduct(activity: Activity, product: ProductPurchase) {
        if(!::billingConnection.isInitialized) return
        billingConnection.subscribeProduct(activity, product)
    }

    override fun onCleared() {
        super.onCleared()
        billingConnection.onDestroy()
    }
}
