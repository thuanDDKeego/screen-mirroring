package com.abc.mirroring.ui.premium

import android.app.Activity
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
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import com.abc.mirroring.R
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
        Image(
            painter = painterResource(id = R.mipmap.bg_premium_xmas),
            contentDescription = null,
            contentScale = ContentScale.FillBounds, modifier = Modifier.fillMaxSize()
        )
        // button back
        Icon(
            imageVector = Icons.Rounded.Close, contentDescription = null, modifier = Modifier
                .padding(24.dp)
                .size(32.dp)
                .clickable {
                    activity.finish()
                }, tint = Color.White
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_crown), contentDescription = null,
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
internal fun benefit_item(label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically, modifier = Modifier
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
        _normal_product_item(product) { vm.subscribeProduct(activity, product) }
    }
    state.oneTimePayment.let { product ->
        _best_offer_product_item(product) { vm.subscribeProduct(activity, product) }
    }
    state.yearlySubscription.let { product ->
        _sale_off_product_item(product = product) { vm.subscribeProduct(activity, product) }
    }
}

@Composable
fun _sale_off_product_item(product: ProductPurchase, onclick: () -> Unit) {
    _product_item(
        background = Color(0xFFC51313),
        hasCrown = true,
        content = {

            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(id = R.string.sale_50),
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
                        .padding(
                            top = 24.dp,
                            bottom = 12.dp,
                            start = 16.dp,
                            end = 16.dp
                        )
                ) {
                    Text(
                        text = stringResource(id = R.string.get_3_days_free_trial),
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${product.price}/${stringResource(id = R.string.year)}",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    ) {
        onclick.invoke()
    }
}

@Composable
fun _best_offer_product_item(product: ProductPurchase, onclick: () -> Unit) {
    _product_item(
        background = Color(0xFFFFB422),
        content = {
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
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier.padding(
                        top = 24.dp,
                        bottom = 8.dp,
                        start = 16.dp,
                        end = 32.dp
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
                Text(
                    text = product.price,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    modifier = Modifier
                        .align(CenterEnd)
                        .padding(end = 16.dp)
                )
            }
        }
    ) {
        onclick.invoke()
    }
}


@Composable
internal fun _normal_product_item(product: ProductPurchase, onClick: () -> Unit) {
    _product_item(
        background = Color.White,
        content = {
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
                Text(text = product.price, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    ) {
        onClick()
    }
}

@Composable
internal fun _product_item(
    modifier: Modifier = Modifier,
    background: Color = Color(0xFFFFFFFF),
    hasCrown: Boolean = false,
    content: @Composable () -> Unit = @Composable {},
    onClick: () -> Unit = {},
) {
    ConstraintLayout {
        // Create references for the composables to constrain
        val (cardView, snow, crown) = createRefs()
        Card(
            modifier = modifier
                .constrainAs(cardView) {}
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White,
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp
            ),
            shape = MaterialTheme.shapes.medium
        ) {

            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .background(background)
                    .clickable { onClick() }
            ) {
                content.invoke()
            }
        }
        Image(
            painter = painterResource(id = R.drawable.img_snow),
            contentDescription = null,
            modifier = Modifier
                .size(48.dp)
                .constrainAs(snow) {
                    top.linkTo(cardView.top)
                    end.linkTo(cardView.end, margin = 8.dp)
                })
        if (hasCrown) {
            crown_rotate_image(
                modifier.constrainAs(crown) {
                    top.linkTo(cardView.top, margin = (-5).dp)
                    start.linkTo(cardView.start, margin = (-3).dp)
                })
        }
    }
}

@Composable
fun crown_rotate_image(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition()

    val angleAnimate by infiniteTransition.animateFloat(
        initialValue = -70F,
        targetValue = -20F,
        animationSpec = infiniteRepeatable(
            animation = tween(150, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    var startTime by remember { mutableStateOf(System.currentTimeMillis()) }
    var angle by remember { mutableStateOf(-45f) }
    LaunchedEffect(key1 = angleAnimate) {
        val current = System.currentTimeMillis()
        val a = current - startTime
        if (a < 2000) {
            angle = angleAnimate
        } else if (a < 4000) {
        } else {
            startTime = current
        }
    }
    Image(painter = painterResource(id = R.drawable.ic_crown), contentDescription = null,
        modifier = modifier
            .size(36.dp)
            .graphicsLayer {
                rotationZ = angle
            })
}

@Preview
@Composable
fun premium_preview() {
    premium_()
}
