package com.abc.mirroring.ui.premium.billing

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.abc.mirroring.BuildConfig
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class BillingConnection(mListener: PurchasesUpdatedListener? = null) : IBillingConnection {

    companion object {

        private const val SUBSCRIPTION_ID: String = "subscription_premium_no_ads"
        private const val PRODUCT_ID: String = "premium_life_time"

        private const val SUBSCRIPTION_URL =
            "http://play.google.com/store/account/subscriptions?package=${BuildConfig.APPLICATION_ID}&sku=$SUBSCRIPTION_ID"

        fun openProductPurchaseInfoGooglePlay(context: Context, onError: () -> Unit) {
            try {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(SUBSCRIPTION_URL)))
            } catch (e: ActivityNotFoundException) {
                e.printStackTrace()
                onError.invoke()
            }
        }
    }

    private var billingClient: BillingClient? = null
    private var listProductDetails: MutableList<ProductDetails> = mutableListOf()
    private var listener = mListener ?: PurchasesUpdatedListener { _, _ ->
    }

    private fun createConnection(context: Context, onSuccess: () -> Unit, onError: () -> Unit) {
        if (billingClient != null && billingClient?.isReady == true) {
            onSuccess.invoke()
        } else {
            billingClient = BillingClient.newBuilder(context)
                .enablePendingPurchases()
                .setListener(listener)
                .build()
            billingClient?.startConnection(object : BillingClientStateListener {

                override fun onBillingServiceDisconnected() {
                    onError.invoke()
                    createConnection(context, onSuccess) {
                        //don't invoke onError again when trying reconnect
                    }
                }

                override fun onBillingSetupFinished(p0: BillingResult) {
                    if (p0.responseCode == BillingClient.BillingResponseCode.OK) {
                        //
                        onSuccess.invoke()
                    } else {
                        onError.invoke()
                    }
                }
            })
        }
    }

    override fun getProductPurchases(context: Context, callback: (List<ProductPurchase>) -> Unit) {
        createConnection(
            context,
            onSuccess = {
                CoroutineScope(Dispatchers.Default).launch {
                    val products = async { getAllProductPurchaseFollowTypeAndID(BillingClient.ProductType.INAPP, PRODUCT_ID) }
                    val subscription = async { getAllProductPurchaseFollowTypeAndID(BillingClient.ProductType.SUBS, SUBSCRIPTION_ID) }
                    val allProductPurchases = mutableListOf<ProductPurchase>()
                    allProductPurchases.addAll(products.await())
                    allProductPurchases.addAll(subscription.await())
                    callback.invoke(allProductPurchases)
                }
            },
            onError = {
                callback(listOf())
            }
        )
    }
//
//    private suspend fun getProduct(): List<ProductPurchase> {
//        val products = arrayListOf<ProductPurchase>()
//        val queryProductDetailsParams =
//            QueryProductDetailsParams.newBuilder()
//                .setProductList(
//                    listOf(
//                        QueryProductDetailsParams.Product.newBuilder()
//                            .setProductId(PRODUCT_ID)
//                            .setProductType(BillingClient.ProductType.INAPP)
//                            .build()
//                    )
//                )
//                .build()
//        val productDetailsResult = withContext(Dispatchers.IO) {
//            billingClient?.queryProductDetails(queryProductDetailsParams)
//        }
//        productDetailsResult?.let {
//            it.productDetailsList?.forEach { prod ->
//                listProductDetails.add(prod)
//                prod.oneTimePurchaseOfferDetails?.let { subsOffer ->
//                    prod.subscriptionOfferDetails?.let { subsOffers ->
////                    subsOffer.formattedPrice.let { price ->
//                        subsOffers.forEach { subsOffer ->
//                            subsOffer.pricingPhases.pricingPhaseList.last().let { price ->
//                                products.add(
//                                    ProductPurchase(
//                                        id = prod.productId,
//                                        title = prod.name,
//                                        price = price.formattedPrice,
//                                        type = BillingClient.ProductType.INAPP,
//                                        offerToken = subsOffer.offerToken
//                                    )
//                                )
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        return products.toList()
//    }

    private suspend fun getAllProductPurchaseFollowTypeAndID(
        type: String,
        productId: String
    ): List<ProductPurchase> {
        val subscriptions = arrayListOf<ProductPurchase>()
        val queryProductDetailsParams =
            QueryProductDetailsParams.newBuilder()
                .setProductList(
                    listOf(
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(productId)
                            .setProductType(type)
                            .build()
                    )
                )
                .build()
        val productDetailsResult = withContext(Dispatchers.IO) {
            billingClient?.queryProductDetails(queryProductDetailsParams)
        }
        productDetailsResult?.let {
            it.productDetailsList?.forEach { prod ->
                listProductDetails.add(prod)
                prod.subscriptionOfferDetails?.let { subsOffers ->
                    subsOffers.forEach { subsOffer ->
                        subsOffer.pricingPhases.pricingPhaseList.last().let { price ->
                            subscriptions.add(
                                ProductPurchase(
                                    id = prod.productId,
                                    title = prod.name,
                                    price = price.formattedPrice,
                                    type = BillingClient.ProductType.SUBS,
                                    offerToken = subsOffer.offerToken
                                )
                            )
                        }
                    }
                }
            }
        }
        return subscriptions.toList()
    }

    private suspend fun checkPremiumUser(): Boolean {

        val subscriptionParams = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
        billingClient?.queryPurchasesAsync(subscriptionParams.build())?.let { result ->
            result.purchasesList.forEach {
                if (it.products.contains(SUBSCRIPTION_ID) && it.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    return true
                }
            }
        }
        val inAppProductParams = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
        billingClient?.queryPurchasesAsync(inAppProductParams.build())?.let { result ->
            result.purchasesList.forEach {
                if (it.products.contains(PRODUCT_ID) && it.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    return true
                }
            }
        }
        return false
    }

    override fun subscribeProduct(activity: Activity, product: ProductPurchase) {
        createConnection(
            activity.baseContext,
            onSuccess = {
                listProductDetails.first { it.productId == product.id }.let {
                    val productDetailsParamsList = listOf(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(it)
                            .setOfferToken(
                                product.offerToken
                            )
                            .build()
                    )

                    val billingFlowParams = BillingFlowParams.newBuilder()
                        .setProductDetailsParamsList(productDetailsParamsList)
                        .build()
                    billingClient?.launchBillingFlow(activity, billingFlowParams)
                }
            },
            onError = {
            }
        )
    }

    override fun checkPremiumUser(context: Context, callback: (Boolean) -> Unit) {
        createConnection(
            context,
            onSuccess = {
                CoroutineScope(Dispatchers.Default).launch {
                    callback.invoke(checkPremiumUser())
                }
            },
            onError = {
            }
        )
    }

    override fun onDestroy() {
        if (billingClient != null && billingClient?.isReady == true) {
            billingClient?.endConnection()
            billingClient = null
        }
    }
}