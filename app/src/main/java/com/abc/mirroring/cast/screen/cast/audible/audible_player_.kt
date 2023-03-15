package com.abc.mirroring.cast.screen.cast.audible

import android.annotation.SuppressLint
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.SettingsRemote
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material.icons.rounded.MusicVideo
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.VolumeOff
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.abc.mirroring.R
import com.abc.mirroring.cast.GlobalVimel
import com.abc.mirroring.cast.LocalState
import com.abc.mirroring.cast.section.Audible
import com.abc.mirroring.cast.section.M3U8File
import com.abc.mirroring.cast.section.MediaPicker
import com.abc.mirroring.cast.section.MediaType
import com.abc.mirroring.cast.section.SourceType
import com.abc.mirroring.cast.section.Streamable
import com.abc.mirroring.cast.section.Youtube
import com.abc.mirroring.cast.setup.graphs.VideoNavGraph
import com.abc.mirroring.cast.setup.theme.DarkGrayBg
import com.abc.mirroring.cast.setup.theme.Purple500
import com.abc.mirroring.cast.shared.cast.Command
import com.abc.mirroring.cast.shared.ui.component.small_top_bar
import com.abc.mirroring.cast.shared.utils.FileUtils
import com.abc.mirroring.config.AppPreferences
import com.abc.mirroring.utils.FirebaseLogEvent
import com.abc.mirroring.utils.FirebaseTracking
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dev.sofi.extentions.SofiBinding
import dev.sofi.extentions.SofiComponent
import dev.sofi.extentions.SofiScreen
import kotlinx.coroutines.launch
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@SuppressLint("UnrememberedMutableState")
@VideoNavGraph
@Destination
@Composable
@SofiScreen
fun audible_player_(
    /* <auto biding> */
    @SofiBinding navigator: DestinationsNavigator,
    @SofiBinding main: GlobalVimel,
    @SofiBinding vm: AudibleVimel,
    /* <auto biding/> */

    /* arguments */
    params: AudibleParameter
) {
    val context = LocalContext.current

    LaunchedEffect(params) {
        vm.fetch(context, params)
    }

    /**
     * observe dynamic state
     */
    val scope = rememberCoroutineScope()
    val state by vm.state.collectAsState()

    var ratingVisibility by remember { mutableStateOf(false) }

    /**
     * React to the change
     */
    LaunchedEffect(state.counter) {
        if (state.counter >= 5) main.ads.interstitial?.show(context as Activity) {
            vm.resetCounter()
        }
    }

    LaunchedEffect(key1 = state.isFinished) {
        ratingVisibility = state.isFinished && AppPreferences().isRated == false
    }

    /**
     * React to the change video
     */
    if (state.current != null) {
        LaunchedEffect(state.current!!.id()) {
            vm.cast(context, state.current!!)
        }
    }

    val modalState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = false,
        confirmStateChange = { it != ModalBottomSheetValue.Expanded }
    )

    /**
     * CAUTIONS: only provides VM for the private/internal composable function!!
     */
    CompositionLocalProvider(LocalState provides vm) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                Scaffold(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    topBar = {
                        small_top_bar(
                            navigator,
                            stringResource(id = if (params.type == MediaType.Audio) R.string.audio else R.string.video)
                        )
                    },
                ) { padding ->
                    Column(
                        modifier = Modifier
                            .padding(padding)
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                    ) {

                        main.ads.natives["general"]?.small()

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f)
                                .background(MaterialTheme.colorScheme.background)
                                .verticalScroll(rememberScrollState())
                        ) {

                            //region @section Thumbnail Section
                            /**
                             * Thumbnail
                             */
                            _thumbnail_part(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            )
                            //endregion

                            //region @section video/audio controller section
                            Column(
                                modifier = Modifier.wrapContentSize()
                            ) {

                                Text(
                                    modifier = Modifier.padding(
                                        horizontal = 16.dp,
                                        vertical = 8.dp
                                    ),
                                    text = state.current?.name() ?: "",
                                    fontSize = 18.sp
                                )


//                        _remote_part(
//                            onStop = { vm.onControl(Command.Stop) {} },
//                            onPlaylist = { scope.launch { modalState.show() } },
//                        )

                                _volume_part(
                                    onControl = vm::onControl
                                )

                                _controller_part(
                                    onControl = vm::onControl,
                                    onPlaylist = { scope.launch { modalState.show() } },
                                )
                            }
                            //endregion
                        }

                    }

                    _playlist_bottom_dialog(
                        playlists = vm.playlists,
                        modalState = modalState,
                        onClick = { vm.moveTo(it) }
                    )
                }
            }
//            if (ratingVisibility) {
//                modal_rate(context = context,
//                    hideDialog =
//                    { askAgain ->
//                        AppPreferences.isRated = askAgain == true
//                        ratingVisibility = false
//                    }) {
//                    AppPreferences.isRated = true
//                    if (it <= 2) {
//                        FeedbackUtils.sendFeedback(context)
//                    } else {
//                        RatingUtils.rateInStore(context as Activity)
//                    }
//                }
//            }
        }
    }
}

/**
 *
 */
@SofiComponent(private = true, useFor = ["audible_player_"])
@Composable
private fun _thumbnail_part(modifier: Modifier = Modifier) {
    val state by LocalState.current.stateOrPreview(AudibleVimel.AudibleVimelState())
        .collectAsState()
    if (state.current == null) return

    val streamable = state.current!!

    Box(
        modifier = modifier
    ) {

        Column(
            modifier = Modifier
                .padding(vertical = 8.dp, horizontal = 16.dp)
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            _thumbnail(streamable)
        }

        Row(
            modifier = Modifier
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                modifier = Modifier.background(Color.White),
                text = "Your media is playing on the TV Screen"
            )
        }
    }
}

/**
 * stop video, playlist, remote -> First part of video controller
 */
@SofiComponent(
    private = true,
    useFor = ["audible_player_"],
)
@Composable
@Preview(showBackground = true)
private fun _remote_part(
    onStop: () -> Unit = {},
    onPlaylist: () -> Unit = {},
    onRemote: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        _player_button(Icons.Filled.Stop, stringResource(id = R.string.stop), onStop)
        _player_button(Icons.Filled.PlaylistAdd, stringResource(id = R.string.playlist), onPlaylist)
        _player_button(Icons.Filled.SettingsRemote, stringResource(id = R.string.remote), onRemote)
    }
}

/**
 * the simple button use for [_remote_part]
 */
@SofiComponent(private = true, useFor = ["audible_player_"])
@Composable
private fun _player_button(image: ImageVector, title: String, onClick: () -> Unit) {
    val shape = RoundedCornerShape(8.dp)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .background(Color.White, shape = shape)
                .clip(shape)
                .clickable {
                    onClick.invoke()
                }
        ) {
            Icon(
                modifier = Modifier
                    .padding(8.dp)
                    .size(36.dp),
                imageVector = image,
                contentDescription = title,
                tint = DarkGrayBg
            )
        }
        Text(
            text = title,
            modifier = Modifier.padding(vertical = 4.dp),
            style = MaterialTheme.typography.labelLarge
        )
    }
}

/**
 * control volume -> Second part of video controller
 */
@SofiComponent(useFor = ["audible_player_"], requires = [AudibleVimel.AudibleVimelState::class])
@Composable
@Preview(showBackground = true)
private fun _volume_part(
    onControl: (Command, (Any?) -> Unit) -> Unit = { _, _ -> }
) {
    val state by LocalState.current.stateOrPreview(AudibleVimel.AudibleVimelState())
        .collectAsState()

    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .background(Color.White, shape = RoundedCornerShape(8.dp))
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 32.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = {
            FirebaseTracking.log(FirebaseLogEvent.Video_Click_Volume_Down)
            onControl(Command.VolumeDown) {}
        }) {
            Icon(
                modifier = Modifier.size(32.dp),
                imageVector = Icons.Rounded.Remove,
                contentDescription = stringResource(id = R.string.volume_down),
                tint = DarkGrayBg
            )
        }
        IconButton(onClick = {
            FirebaseTracking.log(FirebaseLogEvent.Video_Click_Volume_Mute)
            onControl(Command.Mute) {}
        }) {
            if (state.isMute) {
                Icon(
                    modifier = Modifier.size(32.dp),
                    imageVector = Icons.Rounded.VolumeOff,
                    contentDescription = stringResource(id = R.string.volume_mute),
                    tint = DarkGrayBg
                )
            } else {
                Icon(
                    modifier = Modifier.size(32.dp),
                    imageVector = Icons.Rounded.VolumeUp,
                    contentDescription = stringResource(id = R.string.volume_mute),
                    tint = DarkGrayBg
                )
            }
        }
        IconButton(onClick = {
            FirebaseTracking.log(FirebaseLogEvent.Video_Click_Volume_Up)
            onControl(Command.VolumeUp) {}
        }
        ) {
            Icon(
                modifier = Modifier.size(32.dp),
                imageVector = Icons.Rounded.Add,
                contentDescription = stringResource(id = R.string.volume_up),
                tint = DarkGrayBg
            )
        }
    }
}

/**
 * Control next, pause, play
 */
@SofiComponent(useFor = ["audible_player_"], requires = [AudibleVimel.AudibleVimelState::class])
@Preview(showBackground = true)
@Composable
private fun _controller_part(
    onControl: (Command, (Any?) -> Unit) -> Unit = { _, _ -> },
    onPlaylist: () -> Unit = {}
) {
    val state by LocalState.current.stateOrPreview(AudibleVimel.AudibleVimelState())
        .collectAsState()
    val position = state.mPosition

    /* thuộc tính duration của internal và youtube đang chính xác, nên chỉ có các file external còn lại
    * cần sử dụng hàm getDuration() của Connect-sdk
    * */
    val duration = state.duration.toFloat()
    Timber.d("duration of media: $duration")
    /*
    refer to position picked by user on Mobile.
    we need to remember it, then send it to TV
    */
    var uiPosition by remember { mutableStateOf(0f) }
    var onSliding by remember { mutableStateOf(false) }


    /**
     * when TV change position, update it to phone
     */
    LaunchedEffect(position) {
        if (!onSliding) uiPosition = position.toFloat()
    }


    Column(
        modifier = Modifier
            .padding(16.dp)
            .background(Color.White, shape = RoundedCornerShape(8.dp))
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = FileUtils.millisToTime(position))
            Text(text = FileUtils.millisToTime(duration.toLong()))
        }

        Slider(
            value = uiPosition,
            onValueChange = {
                uiPosition = it
                // pause before it turn true
                if (!onSliding) onControl(Command.Pause) {}
                // this sliding turning into TRUE until user release hand
                onSliding = true

            },
            valueRange = 0f..duration,
            onValueChangeFinished = {
                onControl(Command.Seek(uiPosition)) {
                    onSliding = false
                    onControl(Command.Play) {}
                }
            }
        )

        Row(modifier = Modifier.fillMaxWidth()) {
//            IconButton(onClick = { }) {
//                Icon(
//                    modifier = Modifier.size(36.dp),
//                    imageVector = Icons.Filled.SettingsRemote,
//                    contentDescription = stringResource(id = R.string.remote),
//                    tint = DarkGrayBg
//                )
//            }
            Spacer(Modifier.size(36.dp))


            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.Center,
            ) {
                IconButton(onClick = {
                    FirebaseTracking.log(FirebaseLogEvent.Video_Click_Previous)
                    onControl(Command.Previous) {}
                }) {
                    Icon(
                        modifier = Modifier.size(36.dp),
                        imageVector = Icons.Rounded.SkipPrevious,
                        contentDescription = stringResource(id = R.string.previous_video),
                        tint = DarkGrayBg
                    )
                }
                IconButton(onClick = {
                    FirebaseTracking.log(FirebaseLogEvent.Video_Click_Play)
                    if (state.isPlaying) {
                        onControl(Command.Pause) {}
                    } else {
                        onControl(
                            Command.Play
                        ) {}
                    }
                }) {
                    Icon(
                        modifier = Modifier.size(48.dp),
                        imageVector = if (state.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayCircle,
                        contentDescription = stringResource(id = R.string.play),
                        tint = DarkGrayBg
                    )
                }
//                IconButton(onClick = { onControl(Command.Stop) {} }) {
//                    Icon(
//                        modifier = Modifier.size(48.dp),
//                        imageVector = Icons.Filled.Stop,
//                        contentDescription = stringResource(id = R.string.stop),
//                        tint = DarkGrayBg
//                    )
//                }
                IconButton(onClick = {
                    FirebaseTracking.log(FirebaseLogEvent.Video_Click_Next)
                    onControl(Command.Next) {}
                }) {
                    Icon(
                        modifier = Modifier.size(36.dp),
                        imageVector = Icons.Rounded.SkipNext,
                        contentDescription = stringResource(id = R.string.next_video),
                        tint = DarkGrayBg
                    )
                }
            }

            IconButton(onClick = {
                FirebaseTracking.log(FirebaseLogEvent.Video_Click_Playlist)
                onPlaylist()
            }) {
                Icon(
                    modifier = Modifier.size(36.dp),
                    imageVector = Icons.Filled.PlaylistAdd,
                    contentDescription = stringResource(id = R.string.playlist),
                    tint = DarkGrayBg
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@SofiComponent(useFor = ["audible_player_"], requires = [AudibleVimel.AudibleVimelState::class])
@Composable
private fun _playlist_bottom_dialog(
    playlists: List<Streamable> = emptyList(),
    modalState: ModalBottomSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden),
    onClick: (Streamable) -> Unit = {}
) {

    val state by LocalState.current.stateOrPreview(AudibleVimel.AudibleVimelState())
        .collectAsState()
    val scope = rememberCoroutineScope()
//    fix later
//    val scrollState = LazyListState(state.index)

    ModalBottomSheetLayout(
        sheetState = modalState,
        sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        sheetContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.5f)
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    modifier = Modifier.padding(vertical = 8.dp),
                    imageVector = Icons.Rounded.DragHandle,
                    contentDescription = "Drag"
                )

                if (playlists.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
//                        state = scrollState
                    ) {
                        items(playlists) {
                            _playlist_item(it, it == state.current, onClick)
                        }
                    }
                }
            }
        }
    ) {
        // you can also opt to put screen composable / scaffold here
    }

    BackHandler(enabled = modalState.isVisible) {
        scope.launch {
            modalState.hide() // will trigger the LaunchedEffect
        }
    }
}

@SofiComponent(private = true, useFor = ["_playlist_bottom_dialog"])
@Composable
fun _playlist_item(
    media: Streamable,
    isSelect: Boolean,
    onClick: (Streamable) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick(media)
            }
            .background(if (isSelect) Color.LightGray else Color.White)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(64.dp)) {
            _thumbnail(media)
        }

        Text(
            modifier = Modifier.padding(horizontal = 4.dp),
            maxLines = 1,
            text = media.name()
        )
//        Row(
//            modifier = Modifier.weight(0.1f)
//        ) {
//            if (!isSelect) {
//                Icon(
//                    modifier = Modifier
//                        .padding(vertical = 8.dp)
//                        .clickable {
//                            vm.videos.removeAt(vm.videos.indexOf(video))
//                        },
//                    imageVector = Icons.Rounded.Delete,
//                    contentDescription = "Delete"
//                )
//            }
//        }
    }
}

@Composable
private fun _thumbnail(
    streamable: Streamable
) {

    val context = LocalContext.current

    if (streamable.source() == SourceType.Internal) {
        when (streamable.mediaType()) {
            MediaType.Audio -> {
                Icon(
                    modifier = Modifier
                        .size(64.dp)
                        .fillMaxSize(),
                    imageVector = Icons.Rounded.MusicVideo,
                    contentDescription = stringResource(id = R.string.audio),
                    tint = Purple500
                )
            }

            MediaType.Image -> {
                Image(
                    painter = rememberAsyncImagePainter(model = streamable.uri()),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp)),
//                        .aspectRatio(4f / 3f),
                    contentScale = ContentScale.Crop,
                    contentDescription = streamable.name()
                )
            }

            MediaType.Video -> {
                // TODO: Get video thumbnail ? when it comes from url?
                MediaPicker.getImage(context, streamable as Audible)
                    ?.asImageBitmap()
                    ?.let {
                        Image(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp)),
//                                .aspectRatio(4f / 3f),
                            contentScale = ContentScale.Crop,
                            bitmap = it,
                            contentDescription = streamable.name()
                        )
                    }
            }

            else -> {}
        }
    } else {
        val thumbnail =
            when (streamable.mediaType()) {
                MediaType.Youtube -> (streamable as Youtube).thumbnail
                MediaType.M3U8File -> (streamable as M3U8File).thumbnail
                else -> null
            }
        Image(
            painter = rememberAsyncImagePainter(model = thumbnail),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp)),
//                    .aspectRatio(4f / 3f),
            contentScale = ContentScale.Crop,
            contentDescription = streamable.name()
        )
    }
}
