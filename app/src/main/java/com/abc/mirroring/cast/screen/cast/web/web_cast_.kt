package com.abc.mirroring.cast.screen.cast.web

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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetState
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AudioFile
import androidx.compose.material.icons.rounded.HighQuality
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.VideoCall
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dev.sofi.extentions.SofiBinding
import dev.sofi.extentions.SofiComponent
import kotlinx.coroutines.launch
import com.abc.mirroring.cast.GlobalVimel
import com.abc.mirroring.cast.screen.cast.audible.AudibleParameter
import com.abc.mirroring.cast.screen.cast.image.ImageParameter
import com.abc.mirroring.cast.section.MediaType
import com.abc.mirroring.cast.section.SourceType
import com.abc.mirroring.cast.section.Streamable
import net.sofigo.cast.tv.shared.cast.Command
import com.abc.mirroring.cast.shared.ui.component.intercepted_browser
import com.abc.mirroring.cast.shared.ui.component.top_bar_webview

@OptIn(ExperimentalMaterialApi::class)
@Destination
@Composable
fun web_cast_(
    @SofiBinding navigator: DestinationsNavigator,
    @SofiBinding main: GlobalVimel,
    vm: WebVimel = hiltViewModel()
) {
    val context = LocalContext.current

    val state by vm.state.collectAsState()

    //value state of search bar (text-field)
    val txtSearch = remember { mutableStateOf("") }
    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = BottomSheetState(BottomSheetValue.Collapsed))
    val scope = rememberCoroutineScope()

    fun toggleSheet(expand: Boolean) {
        scope.launch {
            if (expand) bottomSheetScaffoldState.bottomSheetState.expand()
            else bottomSheetScaffoldState.bottomSheetState.collapse()
        }
    }

    fun customBackPressed() {
        if (vm.isOriginalUrl()) navigator.navigateUp() else vm.onControl(Command.Previous)
        toggleSheet(false)
    }

    LaunchedEffect(state.url) {
        txtSearch.value = state.url
    }

    LaunchedEffect(state.medias) {
        if (state.medias.isNotEmpty()) toggleSheet(expand = true)
    }

    BackHandler {
        customBackPressed()
    }


    BottomSheetScaffold(
        sheetElevation = 4.dp,
        sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        sheetPeekHeight = 0.dp,
        sheetContent = {
            _bottom_sheet_part(medias = state.medias) { medias, current ->
                if (main.state.value.isDeviceConnected) {
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
                } else main.caster.discovery.picker(context as Activity)
            }
        },
        backgroundColor = MaterialTheme.colorScheme.background,
        scaffoldState = bottomSheetScaffoldState
    ) {
        Column(
            Modifier
                .padding(it)
                .fillMaxSize()
        ) {
            //header: contains back button, premium, cast icon and searchbar
            top_bar_webview(modifier = Modifier.fillMaxWidth(), navigator = navigator, onBack = { customBackPressed() }, txtSearch = txtSearch, onSearch = { vm.search(txtSearch.value) })
            //region Webview section
            intercepted_browser(
                modifier = Modifier.weight(1f),
                url = WebVimel.GOOGLE_URL,
                onPageFinished = vm::onPageFinished
            )
            //endregion

            _footer(
                onHomePressed = { navigator.popBackStack() },
                onQualityPressed = { toggleSheet(expand = true) }
            )
        }
    }
}

@SofiComponent(useFor = ["web_view_"])
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
    medias: List<Streamable>,
    onItemPressed: (List<Streamable>, Streamable) -> Unit
) {

    Column(
        modifier = modifier
            .wrapContentHeight()
            .fillMaxWidth()
    ) {
        Text(modifier = Modifier.padding(8.dp), text = "Quality options")
        //for each 3 type of media
        listOf(MediaType.Video, MediaType.Image, MediaType.Audio).forEach { type ->
            //label follow media type
            Text(modifier = Modifier.padding(8.dp), text = if (type == MediaType.Video) "Video" else if (type == MediaType.Image) "Image" else "Audio")
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


@Composable
fun _option_item(modifier: Modifier = Modifier, media: Streamable, onPressItem: (Streamable) -> Unit) {

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












