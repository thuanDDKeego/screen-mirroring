package com.abc.mirroring.ui.premium

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.abc.mirroring.ui.premium.billing.BillingConnection
import com.abc.mirroring.ui.premium.billing.ProductPurchase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class PremiumActivity2 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val billingConnection = BillingConnection()

        setContent {
            premium_()
//            var product by remember { mutableStateOf(listOf<ProductPurchase>()) }
//            LaunchedEffect(key1 = true) {
//                billingConnection.getProductPurchases(this@PremiumActivity2) { subscriptions ->
//                    if (subscriptions.isNotEmpty()) {
//                        subscriptions.forEach {
//                            Timber.d("price product purchase: ${it.price} ${it.offerToken}")
//                        }
//                        product = subscriptions
//                    }
//                }
//            }
//            Column(
//                verticalArrangement = Arrangement.SpaceAround,
//                horizontalAlignment = Alignment.CenterHorizontally,
//                modifier = Modifier.fillMaxSize()
//            ) {
//                product.forEach {
//                    Text(modifier = Modifier
//                        .background(Color.Blue)
//                        .clip(RoundedCornerShape(16.dp))
//                        .clickable {
//                            billingConnection.subscribeProduct(
//                                this@PremiumActivity2,
//                                it
//                            )
//                        }
//                        .padding(15.dp), text = it.title)
//                }
//
//                Button(onClick = {
//                    billingConnection.checkPremiumUser(this@PremiumActivity2) {
//                        CoroutineScope(Dispatchers.Main).launch {
//                            Toast.makeText(
//                                this@PremiumActivity2,
//                                "is premium user: $it",
//                                Toast.LENGTH_LONG
//                            ).show()
//                        }
//                    }
//                }) {
//                    Text(text = "check premium user", modifier = Modifier.clip(RoundedCornerShape(16.dp)).background(Color.Blue).padding(12.dp))
//                }
//            }
        }
    }
}