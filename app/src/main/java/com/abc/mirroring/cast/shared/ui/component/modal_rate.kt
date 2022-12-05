package com.abc.mirroring.cast.shared.ui.component


import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.gowtham.ratingbar.RatingBar
import com.gowtham.ratingbar.RatingBarConfig
import com.gowtham.ratingbar.RatingBarStyle
import net.sofigo.cast.tv.BuildConfig
import net.sofigo.cast.tv.MainActivity
import net.sofigo.cast.tv.R
import com.abc.mirroring.cast.setup.config.AppPreferences
import timber.log.Timber
import kotlin.math.roundToInt

@Composable
fun modal_rate(
    context: Context,
    hideDialog: (Boolean) -> Unit,
    onRated: (Int) -> Unit
) {
    var stars by remember { mutableStateOf(5) }
    val gifEmoij = if (stars < 2) {
        R.raw.animation_2star
    } else if (stars < 3) {
        R.raw.animation_3star
    } else if (stars < 4) {
        R.raw.animation_4star
    } else {
        R.raw.animation_5star
    }

    Dialog(
        onDismissRequest = {
            hideDialog(false)
        }
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .background(color = MaterialTheme.colorScheme.background, shape = RoundedCornerShape(12.dp))
                .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                GifLottie(
                    modifier = Modifier
                        .size(125.dp)
                        .align(Alignment.Center),
                    gifSource = gifEmoij
                )

                Icon(
                    Icons.Filled.Close, contentDescription = null,
                    modifier = Modifier
                        .size(36.dp)
                        .padding(2.dp)
                        .clickable {
                            hideDialog(false)
                        }
                        .align(Alignment.TopEnd),
                    tint = Color.DarkGray

                )

            }
            Spacer(modifier = Modifier.size(24.dp))
            RatingBar(
                value = stars.toFloat(),
                config = RatingBarConfig()
                    .style(RatingBarStyle.HighLighted),
                onValueChange = {
                    stars = it.roundToInt()
                },
                onRatingChanged = {
                }
            )
            Spacer(modifier = Modifier.size(16.dp))
            Text(
                text = stringResource(id = R.string.rating_dialog_description),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.size(24.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    hideDialog(true)
                    onRated(stars)
//                    if (stars <= 2) {
//                    } else {
//                        AppPreferences.isRated = true
//                        rateInStore(context)
//                    }


                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(text = stringResource(id = R.string.rate), color = MaterialTheme.colorScheme.background)
            }
            Spacer(modifier = Modifier.size(8.dp))
            if (AppPreferences.isRated == false) {
                Text(
                    text = stringResource(id = R.string.no_ask_again),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                        .clickable {
                            AppPreferences.isRated = true
                            hideDialog(true)
                        },
                    textAlign = TextAlign.Center,
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
fun GifLottie(
    modifier: Modifier = Modifier,
    gifSource: Int
) {
    val composition by rememberLottieComposition(spec = LottieCompositionSpec.RawRes(gifSource))
    LottieAnimation(modifier = modifier, composition = composition, iterations = Int.MAX_VALUE)
}

fun Context.getActivity(): MainActivity? = when (this) {
    is MainActivity -> this
    is ContextWrapper -> baseContext.getActivity()
    else -> null
}

fun rateInStore(context: Context) {
    val url =
        "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID
    val intent = Intent(Intent.ACTION_VIEW).apply {
        data = Uri.parse(url)
    }
    context.startActivity(intent)
}

fun Activity.openAppInStore() {
    try {
        val intent = packageManager.getLaunchIntentForPackage("com.android.vending")
        if (intent != null) {
            intent.action = Intent.ACTION_VIEW
            intent.data = Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
            startActivity(intent)
        }
    } catch (e: java.lang.Exception) {
        Timber.tag("exceptionGgPlay:").d(e.message.toString())
    }
}