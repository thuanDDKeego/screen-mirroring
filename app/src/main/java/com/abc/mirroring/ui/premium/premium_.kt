package com.abc.mirroring.ui.premium

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterEnd
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import com.abc.mirroring.R
import com.abc.mirroring.cast.shared.utils.PremiumUtils.getBenefits
import com.abc.mirroring.ui.premium.billing.ProductPurchase
import com.abc.mirroring.utils.FirebaseLogEvent
import com.abc.mirroring.utils.FirebaseTracking
import com.ramcosta.composedestinations.annotation.Destination

@Destination
@Composable
fun premium_(
    vm: PremiumVimel = hiltViewModel(),
) {
    val activity = LocalContext.current as Activity

    val state by vm.state.collectAsState()

    LaunchedEffect(key1 = true) {
        vm.createConnectionAndFetchData(activity)
    }

    BackHandler {
        FirebaseTracking.log(FirebaseLogEvent.Premium_Click_Back)
        activity.finish()
    }

//    val isPremiumActive = remember { mutableStateOf(AppPreferences.isPremiumSubscribed) }
    Surface(
        modifier = Modifier.fillMaxSize(), color = Color(0xff2F8C4B)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // set background for premium screen
            Image(
                painter = painterResource(id = R.mipmap.bg_premium_xmas),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.fillMaxSize()
            )
            Column(Modifier.fillMaxSize()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_crown),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth(0.4f)
                            .padding(top = 24.dp),
                        contentScale = ContentScale.FillWidth
                    )
                    Text(
                        text = stringResource(id = R.string.premium_upgrade),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(top = 12.dp, bottom = 12.dp)
                    )
                    getBenefits(activity).forEach { benefit ->
                        benefit_item(benefit)
                    }

                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        text = "Try 3 days for free",
                        fontSize = 24.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        text = "Then ${priceToString(state.yearlySubscription.price)}/year (-30%)",
                        fontSize = 16.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )


                    _purchases_section(vm)
                    Spacer(modifier = Modifier.height(36.dp))
                    Text(
                        text = "Term of Services & Privacy Policy",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                            .clickable {
                                activity.startActivity(Intent(Intent.ACTION_VIEW).apply {
                                    data = Uri.parse("https://sofigo.net/policy/")
                                })
                            },
                        textAlign = TextAlign.Center,
                        style = TextStyle(textDecoration = TextDecoration.Underline)
                    )

                    Text(
                        text = stringResource(id = R.string.premium_privacy_des),
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp, bottom = 12.dp, start = 8.dp, end = 8.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
            // button back
            IconButton(modifier = Modifier
                .padding(24.dp)
                .size(24.dp), onClick = {
                FirebaseTracking.log(FirebaseLogEvent.Premium_Click_Back)
                activity.finish()
            }) {
                Icon(
                    imageVector = Icons.Rounded.Close, contentDescription = null, tint = Color.White
                )
            }
        }
    }
}

@Composable
internal fun benefit_item(label: String) {
    Row(
        verticalAlignment = CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 8.dp, start = 42.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.CheckCircle,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(28.dp)
        )
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.White,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}

@Composable
internal fun _purchases_section(vm: PremiumVimel) {
    val state by vm.state.collectAsState()
    val activity = LocalContext.current as Activity
    state.monthlySubscription.let { product ->
        _normal_product_item(product) {
            FirebaseTracking.log(FirebaseLogEvent.Premium_Click_Monthly)
            vm.subscribeProduct(activity, product) {
                Toast.makeText(
                    activity, "Check your internet and try again!", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    state.oneTimePayment.let { product ->
        _best_offer_product_item(product) {
            FirebaseTracking.log(FirebaseLogEvent.Premium_Click_Life_Time)
            vm.subscribeProduct(activity, product) {
                Toast.makeText(
                    activity, "Check your internet and try again!", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    state.yearlySubscription.let { product ->
        _sale_off_product_item(product = product) {
            FirebaseTracking.log(FirebaseLogEvent.Premium_Click_Trial)
            vm.subscribeProduct(activity, product) {
                Toast.makeText(
                    activity, "Check your internet and try again!", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}

@Composable
fun _sale_off_product_item(product: ProductPurchase, onclick: () -> Unit) {
    _product_item(
        background = Color(0xFFC51313),
        hasCrown = true,
        hasSweepLight = true,
        lightSweepTween = 3400,
        content = {

            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(id = R.string.save_30),
                    color = Color.Black,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(bottomEnd = 12.dp))
                        .background(Color(0xFFFCC73F))
                        .padding(vertical = 4.dp, horizontal = 12.dp)
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.start_free_trial),
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }) {
        onclick.invoke()
    }
}

@Composable
fun _best_offer_product_item(product: ProductPurchase, onclick: () -> Unit) {
    9
    _product_item(background = Color(0xFFFFB422), hasSweepLight = true, content = {
        Box(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(id = R.string.best_offer),
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(bottomEnd = 12.dp))
                    .background(Color(0xFFC51313))
                    .padding(vertical = 4.dp, horizontal = 12.dp)
            )
            Column(
                horizontalAlignment = Alignment.Start, modifier = Modifier.padding(
                    top = 24.dp, bottom = 8.dp, start = 16.dp, end = 32.dp
                )
            ) {
                Text(
                    text = stringResource(id = R.string.life_time),
                    color = Color.Black,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(id = R.string.one_time_payment),
                    color = Color.Black,
                    fontSize = 14.sp,
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .align(CenterEnd)
                    .padding(end = 16.dp)
            ) {
                Text(
                    text = priceToString(
                        getInitialPrice(product.price, discount = 0.5),
                        format = product.formatPrice
                    ),
                    fontSize = 14.sp,
                    color = Color.DarkGray,
                    textDecoration = TextDecoration.LineThrough,
                    modifier = Modifier.padding(end = 12.dp)
                )
                Text(
                    text = priceToString(product.price, format = product.formatPrice),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    modifier = Modifier
                )
            }
        }
    }) {
        onclick.invoke()
    }
}


@Composable
internal fun _normal_product_item(product: ProductPurchase, onClick: () -> Unit) {
    _product_item(background = Color.White, content = {
        Row(
            verticalAlignment = CenterVertically,
            modifier = Modifier.padding(vertical = 18.dp, horizontal = 16.dp)
        ) {
            Text(
                text = stringResource(id = R.string.monthly),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = priceToString(product.price, format = product.formatPrice),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }) {
        onClick()
    }
}

@Composable
internal fun _product_item(
    modifier: Modifier = Modifier,
    background: Color = Color(0xFFFFFFFF),
    hasCrown: Boolean = false,
    hasSweepLight: Boolean = false,
    lightSweepTween: Int = 4000,
    content: @Composable () -> Unit = @Composable {},
    onClick: () -> Unit = {},
) {
    ConstraintLayout {
        // Create references for the composables to constrain
        val (cardView, snow, crown) = createRefs()
        Card(modifier = modifier
            .constrainAs(cardView) {}
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp), colors = CardDefaults.cardColors(
            containerColor = Color.White,
        ), elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ), shape = MaterialTheme.shapes.medium) {

            Box(modifier = modifier
                .fillMaxWidth()
                .background(background)
                .clickable { onClick() }) {
                content.invoke()
                if (hasSweepLight) {
                    _light_sweep(modifier = Modifier.fillMaxHeight(), tween = lightSweepTween)
                }
            }
        }
        Image(painter = painterResource(id = R.drawable.img_snow),
            contentDescription = null,
            contentScale = ContentScale.FillHeight,
            modifier = Modifier
                .size(48.dp)
                .constrainAs(snow) {
                    top.linkTo(cardView.top)
                    end.linkTo(cardView.end, margin = 8.dp)
                })
        if (hasCrown) {
            crown_rotate_image(modifier.constrainAs(crown) {
                top.linkTo(cardView.top, margin = (-5).dp)
                start.linkTo(cardView.start, margin = (-3).dp)
            })
        }
    }
}

@Composable
internal fun _light_sweep(modifier: Modifier = Modifier, tween: Int = 4000) {
    val infiniteTransition = rememberInfiniteTransition()

    val paddingStart by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1600F, animationSpec = infiniteRepeatable(
            animation = tween(tween, easing = LinearEasing), repeatMode = RepeatMode.Restart
        )
    )


    Image(
        painter = painterResource(id = R.drawable.img_light),
        contentDescription = null,
        modifier = modifier
            .fillMaxHeight()
            .padding(start = paddingStart.dp),
        contentScale = ContentScale.FillHeight
    )
}

@Composable
internal fun crown_rotate_image(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition()

    val angleAnimate by infiniteTransition.animateFloat(
        initialValue = -70F, targetValue = -20F, animationSpec = infiniteRepeatable(
            animation = tween(150, easing = LinearEasing), repeatMode = RepeatMode.Reverse
        )
    )
    var startTime by remember { mutableStateOf(System.currentTimeMillis()) }
    var angle by remember { mutableStateOf(-45f) }
    LaunchedEffect(key1 = angleAnimate) {
        val current = System.currentTimeMillis()
        if (current - startTime < 2000) {
            angle = angleAnimate
        } else if (current - startTime < 4000) {
            return@LaunchedEffect
        } else {
            startTime = current
        }
    }
    Image(painter = painterResource(id = R.drawable.ic_crown),
        contentDescription = null,
        modifier = modifier
            .size(36.dp)
            .graphicsLayer {
                rotationZ = angle
            })
}

private val PRICE_UNIT = 1000000L
internal fun priceToString(price: Long, format: String = "USD"): String {
    var price = price
    val priceStr = StringBuilder()
    //tail is after dot
    var tail = price % PRICE_UNIT
    while (tail > 0L && tail % 10 == 0L) tail /= 10
    if (tail > 0L) priceStr.append(".$tail")
    price /= PRICE_UNIT
    var count = 0
    // if temp
    if (price == 0L) priceStr.insert(0, "0")
    //check every each 3 number, we put a ","
    while (price != 0L) {
        if (count == 3) {
            count = 0
            priceStr.insert(0, ",")

        } else {
            count++
        }
        priceStr.insert(0, price % 10)
        price /= 10
    }
    priceStr.append(format)
    return priceStr.toString()
}

internal fun getInitialPrice(price: Long, discount: Double = 0.5): Long {
    //biến này dùng để kiểm tra giá sẽ có dạng 120000, hay 1.2, hay 2.45 (số làm tròn sau dấu chấm)
    val numberMustRoundedAfterDot =
        if (price % PRICE_UNIT == 0L) 1L else if (price % (PRICE_UNIT / 10L) == 0L) 10L else 100L
    val initialPrice = (price.toDouble() / (1 - discount)).toLong()/* công thức này dùng để làm tròn lên:
    ex: 120012345678 -> 120012345678 - 345678 + 1000000 = 1200124000000 (làm tròn lên)

    * */
    if (initialPrice % (PRICE_UNIT / numberMustRoundedAfterDot) != 0L) return initialPrice + PRICE_UNIT / numberMustRoundedAfterDot - initialPrice % (PRICE_UNIT / numberMustRoundedAfterDot)
    return initialPrice
}


@Preview
@Composable
fun premium_preview() {
    premium_()
}
