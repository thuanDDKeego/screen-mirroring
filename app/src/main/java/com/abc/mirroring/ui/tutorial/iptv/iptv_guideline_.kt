package com.abc.mirroring.ui.tutorial.iptv

import android.content.Intent
import android.net.Uri
import com.abc.mirroring.cast.screen.guideline_.GuildelineData

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.abc.mirroring.R
import one.shot.haki.ads.AdCenter

const val PUBLIC_IPTV_ADDRESS = "https://sofigo.net/public-iptv-address"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun iptv_guideline_(
) {
    val ads = AdCenter.getInstance()
    val context = LocalContext.current
    Scaffold {
        Column(
            modifier = Modifier
                .padding(it)
                .padding(horizontal = 12.dp)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .padding(vertical = 8.dp, horizontal = 16.dp)
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ){
                AdCenter.getInstance().natives["general"]?.render()
                Text(text = stringResource(id = R.string.public_iptv_address_description))
                Text(text = PUBLIC_IPTV_ADDRESS, color = Color.Blue, textDecoration = TextDecoration.Underline, modifier = Modifier.clickable {
                    context.startActivity(Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse(PUBLIC_IPTV_ADDRESS)
                    })
                })
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold)) {
                            append("1. ")
                        }
                        append(stringResource(id = R.string.iptv_guideline_step1))
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold)) {
                            append("2. ")
                        }
                        append(stringResource(id = R.string.iptv_guideline_step2))
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
                Image(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally),
                    painter = painterResource(id = R.drawable.img_iptv_tutorial),
                    contentDescription = "",
                    contentScale = ContentScale.Crop
                )
                Text(
                    buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold)) {
                            append("3. ")
                        }
                        append(stringResource(id = R.string.iptv_guideline_step3))
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
                Image(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally),
                    painter = painterResource(id = R.drawable.img_iptv_tutorial_2),
                    contentDescription = "",
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = stringResource(id = R.string.iptv_guideline_step4))

            }
            AdCenter.getInstance().banner?.render()
        }
    }
}

@Composable
fun _guideline_item(data: GuildelineData) {
    Column(
        modifier = Modifier
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .fillMaxWidth()
    ) {
        Text(text = data.title)
        Image(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally),
            painter = painterResource(id = data.imageResource),
            contentDescription = "",
            contentScale = ContentScale.Crop
        )
    }
}