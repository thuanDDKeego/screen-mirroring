package com.abc.mirroring.ui.premium

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIos
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.abc.mirroring.cast.shared.utils.PremiumUtils
import com.abc.mirroring.cast.shared.utils.PremiumUtils.getBenefits
import com.abc.mirroring.ui.premium.billing.ProductPurchase
import com.ramcosta.composedestinations.annotation.Destination

@Destination
@Composable
fun premium_(
    vm: PremiumVimel = hiltViewModel(),
) {
    val state by vm.state.collectAsState()
    val activity = LocalContext.current as Activity

    LaunchedEffect(key1 = true) {
        vm.createConnectionAndFetchData(activity)
    }


//    val isPremiumActive = remember { mutableStateOf(AppPreferences.isPremiumSubscribed) }
    Box(modifier = Modifier.fillMaxSize()) {
        // set background for premium screen
//        Image(
//            painter = painterResource(id = R.mipmap.bg_premium),
//            contentDescription = null,
//            contentScale = ContentScale.FillBounds, modifier = Modifier.fillMaxSize()
//        )
        // button back
        Icon(
            imageVector = Icons.Rounded.ArrowBackIos, contentDescription = null, modifier = Modifier
                .padding(24.dp)
                .size(24.dp)
                .clickable { }, tint = MaterialTheme.colorScheme.onBackground
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .wrapContentHeight()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 24.dp, horizontal = 36.dp)
        ) {
//        _purchases_section(subsState.value)
            if (!state.isSubscribed) {
                // price info
            } else {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Success!",
                    fontSize = 64.sp,
                    color = Color.Green,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp, bottom = 4.dp)
                )
                Text(
                    text = "Thanks for using Cast TV!",
                    fontSize = 14.sp,
                    color = Color.Blue,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                )
            }
            getBenefits(activity).forEach { benefit ->
                benefit_item(benefit)
            }
            _purchases_section(vm)
            Text(
                text = "term of service & privacy and policy",
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 14.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp), textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
internal fun benefit_item(item: PremiumUtils.Benefit) {
    Row(
        verticalAlignment = Alignment.CenterVertically, modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Image(
            painter = painterResource(id = item.icon),
            contentDescription = null,
            Modifier.size(24.dp)
        )
        Text(
            text = item.label,
            fontSize = 14.sp,
            color = Color.Blue,
            fontWeight = FontWeight.Light,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}

@Composable
internal fun _purchases_section(vm: PremiumVimel) {
    val state by vm.state.collectAsState()
    val activity = LocalContext.current as Activity
    listOf(
        state.monthlySubscription,
        state.oneTimePayment,
        state.yearlySubscription
    ).forEach { product ->
        _product_item(
            content = { Text(text = product.price) }
        ) {
            vm.subscribeProduct(activity, product)
        }
    }
}

@Composable
fun _product_item(
    modifier: Modifier = Modifier,
    background: Color = Color(0xFF8b7ffc),
    content: @Composable () -> Unit = @Composable {},
    onClick: () -> Unit = {},
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
        shape = MaterialTheme.shapes.extraLarge
    ) {

        Box(
            modifier = modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(horizontal = 8.dp, vertical = 16.dp),
        ) {
            content.invoke()
        }
    }
}

@Composable
fun _label_section(product: ProductPurchase) {
    Text(
        buildAnnotatedString {
            withStyle(
                style = SpanStyle(
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Blue,
                    fontSize = 48.sp
                )
            ) {
                append(product.price)
            }
            withStyle(
                style = SpanStyle(
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Blue,
                    fontSize = 20.sp
                )
            ) {

                append("/year")
            }
        }
    )
}