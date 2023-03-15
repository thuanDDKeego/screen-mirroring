package com.abc.mirroring.cast.screen.cast.image

import android.annotation.SuppressLint
import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.abc.mirroring.R
import com.abc.mirroring.cast.GlobalVimel
import com.abc.mirroring.cast.screen.cast.image.component.image
import com.abc.mirroring.cast.section.MediaType
import com.abc.mirroring.cast.section.SourceType
import com.abc.mirroring.cast.setup.graphs.ImageNavGraph
import com.abc.mirroring.cast.shared.Permissionary
import com.abc.mirroring.cast.shared.ui.component.small_top_bar
import com.abc.mirroring.destinations.image_player_Destination
import com.abc.mirroring.utils.FirebaseLogEvent
import com.abc.mirroring.utils.FirebaseTracking
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dev.sofi.extentions.SofiBinding
import dev.sofi.extentions.SofiScreen


@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnrememberedMutableState")
@ImageNavGraph(start = true)
@Destination
@Composable
@SofiScreen("Image Picker Screen")
fun image_picker_(
    @SofiBinding navigator: DestinationsNavigator,
    @SofiBinding main: GlobalVimel,
    @SofiBinding vm: ImageVimel
) {
    /**
     * TODO: UI Wifi checking and loadings the devices
     */
    val context = LocalContext.current as Activity

    Permissionary.require(Permissionary.getPermission(MediaType.Image)) {
        if (it) vm.fetch(context, ImageParameter(source = SourceType.Internal))
    }

    // initial state
    val device by main.caster.device().collectAsState()

    BackHandler {
        FirebaseTracking.log(FirebaseLogEvent.Image_Picker_Click_Back)
        context.finish()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            topBar = {
                small_top_bar(
                    navigator = navigator,
                    title = stringResource(id = R.string.cast_image),
                    navigatorIcon = {
                        IconButton(onClick = {
                            FirebaseTracking.log(FirebaseLogEvent.Image_Picker_Click_Back)
                            (context as Activity).finish()
                        }) {
                            Icon(
                                Icons.Filled.ArrowBack,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    })
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                main.ads.natives["general"]?.small()
                LazyVerticalGrid(
                    modifier = Modifier.weight(1f),
                    columns = GridCells.Fixed(count = 3),
                    state = rememberLazyGridState(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = vm.images,
                        key = { it.id() })
                    { streamable ->
                        image(image = streamable) {
                            if (device?.isConnected == true) {
                                main.ads.interstitial?.show(context as Activity) {
                                    navigator.navigate(
                                        image_player_Destination(
                                            params = ImageParameter(
                                                current = streamable
                                            )
                                        )
                                    )
                                }
                            } else {
                                main.caster.discovery.picker(context as Activity)
                            }
                        }
                    }
                }
            }
        }
        main.ads.banner?.render(Modifier.fillMaxWidth())
    }
}