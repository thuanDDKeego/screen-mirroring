package com.abc.mirroring.ui.premium

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.abc.mirroring.ui.premium.billing.BillingConnection

class PremiumActivity2 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            premium_()
        }
    }
}