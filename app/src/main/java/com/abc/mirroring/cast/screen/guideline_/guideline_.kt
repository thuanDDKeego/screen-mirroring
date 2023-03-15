package com.abc.mirroring.cast.screen.guideline_

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.abc.mirroring.R
import one.shot.haki.ads.AdCenter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun guideline_(
) {
    val ads = AdCenter.getInstance()
    val items = mutableListOf(
        GuildelineData(
            "1. Make sure your phone and TV are connected to the same Wi-Fi network",
            R.drawable.tutorial_cast_1
        ),
        GuildelineData(
            "2. Click the connect button",
            R.drawable.tutorial_cast_2
        ),
        GuildelineData(
            "3. Look for the device you want to connect to",
            R.drawable.tutorial_cast_3
        ),
        GuildelineData(
            "4. Select the feature you want to use: cast photos or cast video,...",
            R.drawable.tutorial_cast_4
        ),
        GuildelineData(
            "5. Select the image or video you want to show on your TV and start enjoying.",
            R.drawable.tutorial_cast_1
        ),
    )
    Scaffold { it ->
        Column(
            modifier = Modifier
                .padding(it)
                .padding(horizontal = 12.dp)
                .fillMaxSize()
        ) {

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(items.also { it.add(0, GuildelineData("banner", R.drawable.tutorial_cast_1)) }, key = { item -> item.title }) {
                    if (it.title == "banner") {
                        ads.natives["general"]?.render()
                    } else {
                        _guideline_item(it)
                    }
                }
//                items(items.size, key = { items[it].title }) {
//                    _guideline_item(items[it])
//                }
            }
            ads.banner?.render()
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