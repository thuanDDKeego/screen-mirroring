package com.abc.mirroring.cast.screen.cast.audible

import android.annotation.SuppressLint
import android.app.Activity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope.*
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
import com.abc.mirroring.R
import com.abc.mirroring.cast.GlobalVimel
import com.abc.mirroring.cast.screen.cast.audible.component.audibles
import com.abc.mirroring.cast.section.MediaType
import com.abc.mirroring.cast.section.SourceType
import com.abc.mirroring.cast.setup.graphs.VideoNavGraph
import com.abc.mirroring.cast.shared.Permissionary
import com.abc.mirroring.cast.shared.ui.component.small_top_bar
import com.abc.mirroring.destinations.audible_player_Destination
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dev.sofi.extentions.SofiBinding
import dev.sofi.extentions.SofiScreen

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnrememberedMutableState")
@VideoNavGraph(start = true)
@Destination
@Composable
@SofiScreen
fun video_picker(
    @SofiBinding navigator: DestinationsNavigator,
    @SofiBinding vm: AudibleVimel,
    @SofiBinding main: GlobalVimel,
    type: MediaType = MediaType.Video
) {
    /**
     * TODO: UI Wifi checking and loadings the devices
     */

    val context = LocalContext.current
    val globalState by main.state.collectAsState()

    Permissionary.require(Permissionary.getPermission(type)) {
        if (it) vm.fetch(
            context,
            AudibleParameter(source = SourceType.Internal, type = type)
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            topBar = {
                small_top_bar(
                    navigator = navigator,
                    navigatorIcon = {
                        IconButton(onClick = {
                            (context as Activity).finish()
                        }) {
                            Icon(
                                Icons.Filled.ArrowBack,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    title = stringResource(id =if(type == MediaType.Video) R.string.video_directory else R.string.audio_directory)
                )
            },

            ) { padding ->
            Column(modifier = Modifier.padding(padding)) {
                audibles(type, vm.playlists) {
                    if (globalState.isDeviceConnected) {
                        main.ads.interstitial?.show(context as Activity) {
                            navigator.navigate(
                                audible_player_Destination(
                                    params = AudibleParameter(
                                        current = it,
                                        type = type
                                    )
                                )
                            )
                        }
                    } else {
                        vm.caster.discovery.picker(context as Activity)
                    }
                }
            }
        }
        main.ads.native?.small()
        // main.ads.banner?.render(Modifier.wrapContentSize())
    }
}
