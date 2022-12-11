package com.abc.mirroring.ui.premium.billing

import android.app.Activity
import android.content.Context

interface IBillingConnection {
    fun getProductPurchases(context: Context, callback: (List<ProductPurchase>) -> Unit)
    fun subscribeProduct(activity: Activity, product: ProductPurchase, onError:() -> Unit)
    fun checkPremiumUser(context: Context, callback: (Boolean) -> Unit)
    fun onDestroy()
}