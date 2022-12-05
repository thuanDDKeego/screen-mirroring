package com.abc.mirroring.cast.screen.guideline_

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dev.sofi.extentions.SofiBinding
import com.abc.mirroring.cast.GlobalVimel
import com.abc.mirroring.R
import com.abc.mirroring.cast.shared.ui.component.small_top_bar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Destination
fun guideline_(
    nav: DestinationsNavigator,
    @SofiBinding main: GlobalVimel
) {
    val items = listOf(
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
    Scaffold(
        topBar = {
            small_top_bar(navigator = nav, stringResource(id = R.string.guideline))
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedButton(
                    onClick = {
                        nav.popBackStack()
                    },
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .fillMaxWidth(0.7f)
                ) {
                    Text(text = stringResource(R.string.i_understand))
                }
                // TODO add ads
            }
        }
    ) { it ->
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
        ) {
            main.ads.native?.small()

            LazyColumn {
                items(items.size, key = { items[it].title }) {
                    _guideline_item(items[it])
                }
            }

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