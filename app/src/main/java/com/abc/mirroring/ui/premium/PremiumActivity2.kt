package com.abc.mirroring.ui.premium

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class PremiumActivity2 : ComponentActivity() {
    companion object {
        fun gotoActivity(activity: Activity) {
            val intent = Intent(activity, PremiumActivity2::class.java)
            activity.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            premium_()
        }
    }
}