package com.abc.mirroring.ui.premium

import android.app.Activity
import android.content.Context
import android.widget.Toast
import com.abc.mirroring.cast.State
import com.abc.mirroring.cast.VimelStateHolder
import com.abc.mirroring.config.AppPreferences
import com.abc.mirroring.ui.premium.billing.BillingConnection
import com.abc.mirroring.ui.premium.billing.ProductPurchase
import com.abc.mirroring.utils.FirebaseLogEvent
import com.abc.mirroring.utils.FirebaseTracking
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.PurchasesUpdatedListener
import dagger.hilt.android.lifecycle.HiltViewModel
import one.shot.haki.ads.AdCenter
import timber.log.Timber
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
        var monthlySubscription: ProductPurchase = ProductPurchase("", "", 0L, "$"),
        var yearlySubscription: ProductPurchase = ProductPurchase("", "", 0L, "$"),
        var oneTimePayment: ProductPurchase = ProductPurchase("", "", 0L, "$"),
    ) : State

    private lateinit var billingConnection: BillingConnection

    fun createConnectionAndFetchData(activity: Activity) {
        billingConnection = createBillingConnection(activity)
        fetchProducts(activity)
    }

    private fun fetchProducts(context: Context) {
        billingConnection.getProductPurchases(context) { products ->
            if (products.isNotEmpty()) {
                var freeTrialProduct: ProductPurchase? = null
                if(products.isNullOrEmpty()) return@getProductPurchases
                products.forEach { product ->
                    //check if the product is in-app product (onetime)
                    if (product.id == BillingConnection.PRODUCT_ID) {
                        update { state -> state.copy(oneTimePayment = product) }
                    } else {
                        if (product.basePlanId == BillingConnection.MONTHLY_BASE_PLAN_ID) {
                            update { state -> state.copy(monthlySubscription = product) }
                        } else if (product.basePlanId == BillingConnection.YEARLY_BASE_PLAN_ID) {
                            if (product.offerTags.contains(BillingConnection.FREE_TRIAL_TAG)) {
                                freeTrialProduct = product
                            } else {
                                if (freeTrialProduct == null) {
                                    freeTrialProduct = product
                                }
                            }
                        }
                    }
                }
                update { state ->
                    state.copy(
                        yearlySubscription = freeTrialProduct ?: ProductPurchase("", "",0L, "$")
                    )
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
                    FirebaseTracking.log(FirebaseLogEvent.Premium_Click_Cancel)
//            dismissLoadingBarDialog()
                }
// Handle any other error msgs
                else {
//            dismis sLoadingBarDialog()
                    Toast.makeText(
                        activity,
                        "Has an Error, check your internet and try again!",
                        Toast.LENGTH_SHORT
                    ).show()
                    Timber.e(billingResult.debugMessage)
                }
            }
        return BillingConnection(listener)
    }


    fun subscribeProduct(activity: Activity, product: ProductPurchase, onError: () -> Unit) {
        if (!::billingConnection.isInitialized) return
        billingConnection.subscribeProduct(activity, product, onError)
    }

    override fun onCleared() {
        super.onCleared()
        billingConnection.onDestroy()
    }
}
