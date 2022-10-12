package com.abc.mirroring.ui.premium

import android.text.format.DateUtils

class PremiumUtils {
    companion object {
        fun getExpiryTime (
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
    }
}