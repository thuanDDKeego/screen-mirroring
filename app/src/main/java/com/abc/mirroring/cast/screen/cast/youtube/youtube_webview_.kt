package com.abc.mirroring.cast.screen.cast.youtube

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetState
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.rounded.HighQuality
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.abc.mirroring.R
import com.abc.mirroring.cast.GlobalVimel
import com.abc.mirroring.cast.LocalState
import com.abc.mirroring.cast.screen.cast.audible.AudibleParameter
import com.abc.mirroring.cast.screen.cast.youtube.YoutubeVimel.Companion.YOUTUBE_URL
import com.abc.mirroring.cast.section.Youtube
import com.abc.mirroring.cast.setup.graphs.YoutubeNavGraph
import com.abc.mirroring.cast.shared.cast.Command
import com.abc.mirroring.cast.shared.ui.component.intercepted_browser
import com.abc.mirroring.cast.shared.ui.component.small_top_bar
import com.abc.mirroring.destinations.audible_player_Destination
import com.abc.mirroring.utils.FirebaseLogEvent
import com.abc.mirroring.utils.FirebaseTracking
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dev.sofi.extentions.SofiBinding
import dev.sofi.extentions.SofiComponent
import dev.sofi.extentions.SofiScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Destination
@YoutubeNavGraph(start = true)
@Composable
@SofiScreen("Youtube Web View Screen")
fun youtube_webview_(
    @SofiBinding navigator: DestinationsNavigator,
    @SofiBinding main: GlobalVimel,
    @SofiBinding vm: YoutubeVimel
) {
    val context = LocalContext.current

    val state by vm.state.collectAsState()

    val bottomSheetScaffoldState =
        rememberBottomSheetScaffoldState(bottomSheetState = BottomSheetState(BottomSheetValue.Collapsed))
    val scope = rememberCoroutineScope()

    fun toggleSheet(expand: Boolean) {
        scope.launch {
            if (expand) bottomSheetScaffoldState.bottomSheetState.expand()
            else bottomSheetScaffoldState.bottomSheetState.collapse()
        }
    }

    LaunchedEffect(state.ytbOptions) {
        if (state.ytbOptions.isNotEmpty() && state.url.contains("watch?v=")) toggleSheet(expand = true)
    }

    fun customBackPressed() {
        FirebaseTracking.log(FirebaseLogEvent.Youtube_Click_Back)
        if (vm.isOriginalUrl()) (context as Activity).finish() else vm.onControl(Command.Previous)
//        vm.onControl(Command.Previous)
//        var a = vm.state.value.url
//        if (vm.state.value.url != YOUTUBE_URL) vm.onControl(Command.Previous) else navigator.navigateUp()
        toggleSheet(false)
    }

    BackHandler {
        customBackPressed()
    }

    CompositionLocalProvider(LocalState provides vm) {

        BottomSheetScaffold(
            sheetElevation = 4.dp,
            sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            sheetPeekHeight = 0.dp,
            sheetContent = {
                _bottom_sheet_part {
                    if (main.state.value.isDeviceConnected) {
                        main.ads.interstitial?.show(context as Activity) {
                            navigator.navigate(
                                audible_player_Destination(
                                    params = AudibleParameter(
                                        type = it.mediaType(),
                                        source = it.source(),
                                        urls = listOf(it),
                                        current = it
                                    )
                                )
                            )
                        }
                    } else main.caster.discovery.picker(context as Activity)
                }
            },
            backgroundColor = MaterialTheme.colorScheme.background,
            scaffoldState = bottomSheetScaffoldState
        ) {
            Column(Modifier.fillMaxSize()) {
                /**
                 * Header
                 */
                /**
                 * Header
                 */
//                _header(
//                    onPrev = { vm.onControl(Command.Previous) },
//                    onNext = { vm.onControl(Command.Next) },
//                    onCast = { vm.caster.discovery.picker(context as Activity) },
//                    onDisconnect = {vm.caster.disconnect()},
//                    onUrlChange = { /* do nothing, because we disable user interaction */ }
//                )

                small_top_bar(
                    navigator = navigator,
                    navigatorIcon = {
                        IconButton(onClick = {
                            customBackPressed()
                        }) {
                            Icon(
                                Icons.Filled.ArrowBack,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    })

                //region Webview
                intercepted_browser(
                    modifier = Modifier.weight(1f),
                    url = YOUTUBE_URL,
                    onPageFinished = vm::onPageFinished
                )

                //endregion

                _footer(
                    onHomePressed = {
                        FirebaseTracking.log(FirebaseLogEvent.Youtube_Click_Home)
                        navigator.popBackStack()
                    },
                    onQualityPressed = {
                        FirebaseTracking.log(FirebaseLogEvent.Youtube_Click_Quality_Option)
                        toggleSheet(expand = true)
                    }
                )
            }
        }
    }
}

@SofiComponent(useFor = ["youtube_web_view_"])
@Composable
@Preview(showBackground = true)
private fun _footer(
    onHomePressed: () -> Unit = {},
    onQualityPressed: () -> Unit = {}
) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        IconControl(icon = Icons.Rounded.Home,
            size = 24.dp,
            modifier = Modifier
                .clickable {
                    onHomePressed.invoke()
                })
        IconControl(icon = Icons.Rounded.HighQuality,
            size = 24.dp,
            modifier = Modifier
                .clickable {
                    onQualityPressed.invoke()
                })
    }
}

@Composable
fun _bottom_sheet_part(
    modifier: Modifier = Modifier,
    onItemPressed: (Youtube) -> Unit
) {
    val state by LocalState.current.stateOrPreview(YoutubeVimel.YoutubeVimelState())
        .collectAsState()

    Column(
        modifier = modifier
            .wrapContentHeight()
            .fillMaxWidth()
    ) {
        Text(modifier = Modifier.padding(8.dp), text = "Quality options")
        state.ytbOptions.forEachIndexed { index, item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                _option_item(item = item) {
                    onItemPressed(item)
                }
            }
            if (index < state.ytbOptions.size - 1) Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.LightGray)
            )
        }

    }
}

@Composable
fun _option_item(modifier: Modifier = Modifier, item: Youtube, onPressItem: () -> Unit) {
    val isVideo = item.format != -1

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                FirebaseTracking.log(if(item.format == 720) FirebaseLogEvent.Quality_Option_Click_720_Video else FirebaseLogEvent.Quality_Option_Click_360_Video)
                onPressItem.invoke()
            }
            .padding(vertical = 8.dp, horizontal = 16.dp),
    ) {
        Row(
            modifier = Modifier
        ) {
            Image(
                painter = if (isVideo) rememberAsyncImagePainter(item.thumbnail) else painterResource(
                    R.drawable.ic_headphone
                ),
                contentDescription = "",
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(percent = 10))
                    .background(color = Color.LightGray, RoundedCornerShape(percent = 10)),
                contentScale = ContentScale.Fit
            )
            Column(
                modifier = Modifier.padding(start = 15.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isVideo) {
                        Box(
                            modifier = Modifier
                                .width(44.dp)
                                .height(25.dp)
                                .background(
                                    shape = RoundedCornerShape(5.dp),
                                    color = Color.Blue
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "${item.format}p", color = Color.White)
                        }
                        Spacer(modifier = Modifier.size(16.dp))
                    }
                    Text(text = if (isVideo) "video" else "audio", color = Color(0xFF313131))
                }
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = item.name,
                    color = Color(0xff000000),
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }
        }
    }
}


@Composable
fun IconControl(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    color: Color = Color.Gray,
    size: Dp = 24.dp
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(size),
            tint = color
        )
    }
}
