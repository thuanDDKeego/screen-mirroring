package com.abc.mirroring.cast.screen.cast.web

import android.app.Activity
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetState
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AudioFile
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.HighQuality
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.VideoCall
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.abc.mirroring.cast.GlobalVimel
import com.abc.mirroring.cast.screen.cast.audible.AudibleParameter
import com.abc.mirroring.cast.screen.cast.image.ImageParameter
import com.abc.mirroring.cast.section.MediaType
import com.abc.mirroring.cast.section.SourceType
import com.abc.mirroring.cast.section.Streamable
import com.abc.mirroring.cast.shared.cast.Command
import com.abc.mirroring.cast.shared.ui.component.intercepted_browser
import com.abc.mirroring.cast.shared.ui.component.top_bar_webview
import com.abc.mirroring.destinations.audible_player_Destination
import com.abc.mirroring.destinations.image_player_Destination
import com.abc.mirroring.utils.FirebaseLogEvent
import com.abc.mirroring.utils.FirebaseTracking
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dev.sofi.extentions.SofiBinding
import dev.sofi.extentions.SofiComponent
import kotlinx.coroutines.launch


const val TRACKING_URL = "tracking_url"
const val CAST_URL = "cast_url"
@OptIn(ExperimentalMaterialApi::class)
@Destination
@Composable
fun web_cast_(
    @SofiBinding navigator: DestinationsNavigator,
    @SofiBinding main: GlobalVimel,
    vm: WebVimel = hiltViewModel()
) {
    val activity = LocalContext.current as Activity

    val state by vm.state.collectAsState()

    //value state of search bar (text-field)
    val txtSearch = remember { mutableStateOf("") }
    val bottomSheetScaffoldState =
        rememberBottomSheetScaffoldState(bottomSheetState = BottomSheetState(BottomSheetValue.Collapsed))
    val scope = rememberCoroutineScope()

    fun toggleSheet(expand: Boolean) {
        scope.launch {
            if (expand) bottomSheetScaffoldState.bottomSheetState.expand()
            else bottomSheetScaffoldState.bottomSheetState.collapse()
        }
    }

    fun customBackPressed() {
        if (vm.isOriginalUrl()) (activity).finish() else vm.onControl(Command.Previous)
        toggleSheet(false)
    }

    LaunchedEffect(state.url) {
        txtSearch.value = state.url
        val bundle = Bundle().also { it.putString(TRACKING_URL, state.url) }
        FirebaseTracking.log(FirebaseLogEvent.WebCast_Track_URL, bundle)
    }

    LaunchedEffect(state.medias) {
//        if (state.medias.isNotEmpty()) toggleSheet(expand = true) //TODO show size of medias noti, or shake button, or both
    }

    BackHandler {
        customBackPressed()
    }


    BottomSheetScaffold(
        sheetElevation = 4.dp,
        sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        sheetPeekHeight = 0.dp,
        sheetBackgroundColor = Color(0xFFF3F3F3),
        sheetContent = {
            _bottom_sheet_part(medias = state.medias,
                onCollapse = {
                    scope.launch {
                        bottomSheetScaffoldState.bottomSheetState.collapse()
                    }
                }) { medias, current ->
                if (main.state.value.isDeviceConnected) {
                    //log firebase
                    val bundle = Bundle().also { it.putString(CAST_URL, current.url()) }
                    FirebaseTracking.log(FirebaseLogEvent.WebCast_Cast_URL, bundle)

                    when (current.mediaType()) {
                        MediaType.Image -> navigator.navigate(
                            image_player_Destination(
                                params = ImageParameter(
                                    current.mediaType(),
                                    SourceType.External,
                                    medias,
                                    current
                                )
                            )
                        )

                        else -> navigator.navigate(
                            audible_player_Destination(
                                params = AudibleParameter(
                                    current.mediaType(),
                                    SourceType.External,
                                    medias,
                                    current
                                )
                            )
                        )
                    }
                } else main.caster.discovery.picker(activity)
            }
        },
        backgroundColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
        },
        scaffoldState = bottomSheetScaffoldState
    ) {
        Column(
            Modifier
                .padding(it)
                .fillMaxSize()
        ) {
            //header: contains back button, premium, cast icon and searchbar
            top_bar_webview(
                modifier = Modifier.fillMaxWidth(),
                navigator = navigator,
                onBack = { customBackPressed() },
                txtSearch = txtSearch,
                onSearch = { vm.search(txtSearch.value) })
            //region Webview section
            intercepted_browser(
                modifier = Modifier.weight(1f),
                url = state.url,
                onPageFinished = vm::onPageFinished,
                onLoadResource = vm::onLoadResource
            )
            //endregion

            _footer(
                onHomePressed = { activity.finish() },
                vm = vm,
                onQualityPressed = { toggleSheet(expand = true) }
            )
        }
    }
}

@SofiComponent(useFor = ["web_view_"])
@Composable
private fun _footer(
    onHomePressed: () -> Unit = {},
    vm: WebVimel,
    onQualityPressed: () -> Unit = {}
) {
    val state by vm.state.collectAsState()
    Row(
        Modifier
            .fillMaxWidth()
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        IconControl(icon = Icons.Rounded.Home,
            size = 28.dp,
            modifier = Modifier
                .clickable {
                    onHomePressed.invoke()
                })
        Row(verticalAlignment = Alignment.Top) {
            IconControl(icon = Icons.Rounded.HighQuality,
                size = 28.dp,
                modifier = Modifier
                    .clickable {
                        onQualityPressed.invoke()
                    })
            if (state.medias.isNotEmpty()) {
                Text(
                    text = "${state.medias.size}",
                    modifier = Modifier
                        .size(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Red)
                        .aspectRatio(1f),
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun _bottom_sheet_part(
    modifier: Modifier = Modifier,
    medias: List<Streamable>,
    onCollapse: () -> Unit,
    onItemPressed: (List<Streamable>, Streamable) -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    Box(
        modifier = modifier
            .heightIn(max = screenHeight*0.6f)
            .fillMaxWidth()
    ) {
        IconControl(icon = Icons.Rounded.Close,
            size = 24.dp,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopEnd)
                .clickable {
                    onCollapse.invoke()
                })
        Column(
            modifier = Modifier
                .wrapContentHeight()
        ) {
            Text(modifier = Modifier.padding(8.dp), text = "Quality options")
            //for each 3 type of media
            Column(
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            ) {
                listOf(MediaType.Video, MediaType.Image, MediaType.Audio).forEach { type ->
                    //label follow media type
                    Text(
                        modifier = Modifier.padding(8.dp),
                        text = if (type == MediaType.Video) "Video" else if (type == MediaType.Image) "Image" else "Audio"
                    )
                    medias.filter { media -> media.mediaType() == type }.apply {
                        //list media after filter follow media type
                        forEach { item ->
                            _option_item(media = item) {
                                onItemPressed(this, item)
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun _option_item(
    modifier: Modifier = Modifier,
    media: Streamable,
    onPressItem: (Streamable) -> Unit
) {

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                onPressItem.invoke(media)
            }
            .padding(vertical = 8.dp, horizontal = 16.dp),
    ) {
        Row(
            modifier = Modifier
        ) {
            if (media.mediaType() == MediaType.Image) {
                Image(
                    painter = rememberAsyncImagePainter(model = media.url().trim()),
                    contentDescription = "",
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(percent = 10))
                        .background(color = Color.LightGray, RoundedCornerShape(percent = 10)),
                    contentScale = ContentScale.Fit
                )
            } else {
                Image(
                    imageVector =
                    if (media.mediaType() == MediaType.Video) Icons.Rounded.VideoCall else Icons.Rounded.AudioFile,
                    contentDescription = "",
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(percent = 10))
                        .background(color = Color.LightGray, RoundedCornerShape(percent = 10)),
                    contentScale = ContentScale.Fit
                )
            }
            Column(
                modifier = Modifier.padding(start = 15.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(modifier = Modifier.size(16.dp))
                    Text(text = media.url(), color = Color(0xFF313131), maxLines = 1)
                }
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = media.url(),
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












