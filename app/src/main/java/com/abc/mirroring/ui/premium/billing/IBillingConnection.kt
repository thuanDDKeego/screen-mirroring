package net.sofigo.cast.tv.section.billing

import android.app.Activity
import android.content.Context
import com.abc.mirroring.ui.premium.billing.Subscription

interface IBillingConnection {
    fun getSubscription(context: Context, callback: (List<Subscription>) -> Unit)
    fun subscribePremium(activity: Activity)
    fun getTimeSubscribe(context: Context, callback: (Long) -> Unit)
    fun onDestroy()
}