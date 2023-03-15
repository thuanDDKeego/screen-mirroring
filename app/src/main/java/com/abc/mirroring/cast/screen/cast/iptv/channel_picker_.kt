package com.abc.mirroring.cast.screen.cast.iptv

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.Cast
import androidx.compose.material.icons.rounded.CastConnected
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.abc.mirroring.R
import com.abc.mirroring.cast.GlobalState
import com.abc.mirroring.cast.GlobalVimel
import com.abc.mirroring.cast.screen.cast.audible.AudibleParameter
import com.abc.mirroring.cast.screen.cast.iptv.component._custom_edit_text
import com.abc.mirroring.cast.section.M3U8File
import com.abc.mirroring.cast.section.MediaType
import com.abc.mirroring.cast.section.SourceType
import com.abc.mirroring.cast.setup.graphs.IPTVNavGraph
import com.abc.mirroring.cast.shared.ui.component._dialog
import com.abc.mirroring.destinations.audible_player_Destination
import com.abc.mirroring.utils.FirebaseLogEvent
import com.abc.mirroring.utils.FirebaseTracking
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import one.shot.haki.ads.AdCenter

@Destination
@IPTVNavGraph(start = false)
@Composable
fun channel_picker_(
    navigator: DestinationsNavigator,
    vm: IPTVVimel
) {
    val activity = LocalContext.current as Activity
    val state by vm.state.collectAsState()
    var txtSearch by remember { mutableStateOf("") }

    LaunchedEffect(true) {
        vm.fetchChannels()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        _iptv_picker_actions_top_bar(onBackPressed = { navigator.popBackStack() }) {
            txtSearch = it
        }
//        Column(
//            verticalArrangement = Arrangement.Center,
//            horizontalAlignment = Alignment.CenterHorizontally,
//            modifier = Modifier
//                .fillMaxSize()
//        ) {
//        }
        if (state.isLoading) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(50.dp),
                    color = Color(0xFF0091EA),
                    strokeWidth = 4.dp
                )
            }
        } else {
            if (state.isChannelScreenError) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Image(
                        painter = painterResource(id = R.drawable.img_error), contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(64.dp),
                        contentScale = ContentScale.FillWidth
                    )
                    Text(
                        text = stringResource(id = R.string.get_channels_error_message), fontSize = 14.sp, color = Color.Red, textAlign = TextAlign.Center, fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    state = rememberLazyListState(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    itemsIndexed(state.channels.filter { it.name.contains(txtSearch.trim(), ignoreCase = true) }, key = { _, it -> it.url }) { index, it ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {

                            _channel_item(item = it) {
                                if (vm.caster.isConnected()) {
                                    FirebaseTracking.log(FirebaseLogEvent.IPTVChannel_Click_Channel)
                                    navigator.navigate(audible_player_Destination(AudibleParameter(type = MediaType.M3U8File, source = SourceType.External, urls = state.channels, current = it)))
                                } else {
                                    vm.caster.discovery.picker(activity)
                                }
                            }
                            if (index % 5 == 0) {
                                Spacer(modifier = Modifier.height(12.dp))
                                AdCenter.getInstance().natives["general"]?.medium()
                            }
                        }
                    }
                }
            }
        }
        AdCenter.getInstance().banner?.render()
    }
}

@Composable
private fun _channel_item(modifier: Modifier = Modifier, item: M3U8File, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically, modifier = modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            }
    ) {
        Image(
            painter = rememberAsyncImagePainter(item.thumbnail),
            contentDescription = "",
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(color = Color.LightGray, CircleShape),
            contentScale = ContentScale.Fit
        )
        Column(
            verticalArrangement = Arrangement.SpaceAround,
            horizontalAlignment = Alignment.Start, modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            Text(text = item.name, color = Color.Black, fontSize = 14.sp, maxLines = 1, modifier = Modifier.fillMaxWidth(), fontWeight = FontWeight.SemiBold)
            Text(text = item.url, color = Color.Black, fontSize = 12.sp, maxLines = 1, modifier = Modifier.fillMaxWidth(), fontWeight = FontWeight.Light)
        }
    }
}

@Composable
private fun _iptv_picker_actions_top_bar(
    onBackPressed: () -> Unit,
    onSearch: (String) -> Unit
) {
    val context = LocalContext.current
    val globalVimel = GlobalState.current as GlobalVimel
    val globalState by GlobalState.current.state.collectAsState()

    var isSearching by remember { mutableStateOf(false) }

    // show / hide disconnect device confirmation
    var dialogVisibility by remember { mutableStateOf(false) }

    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(
            onClick = {
                if (isSearching) {
                    isSearching = false
                } else {
                    onBackPressed()
                    FirebaseTracking.log(FirebaseLogEvent.IPTVChannel_Click_Back)
                }
            },
//            modifier = Modifier.padding(12.dp)
        ) {
            Icon(
                Icons.Filled.ArrowBack,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        if (isSearching) {
            _custom_edit_text(
                modifier = Modifier.padding(end = 12.dp),
                hint = stringResource(id = R.string.enter_channel_name_here)
            ) {
                onSearch(it)
            }
        } else {
            Text(text = stringResource(id = R.string.iptv_channel), fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.Black, modifier = Modifier)
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = {
                FirebaseTracking.log(FirebaseLogEvent.IPTVChannel_Click_Search)
                isSearching = true
            }) {
                Image(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "search"
                )
            }
            IconButton(onClick = {
                if (globalState.isDeviceConnected) {
                    dialogVisibility = true
                } else {
                    globalVimel.caster.discovery.picker(context = context as Activity)
                }
            }) {
                if (globalState.isDeviceConnected) {
                    Icon(
                        imageVector = Icons.Rounded.CastConnected,
                        contentDescription = "Cast Connected"
                    )
                    _dialog(
                        visible = dialogVisibility,
                        title = "Connected device",
                        text = "Disconnect with ${globalState.deviceName}",
                        onDismiss = { dialogVisibility = false }
                    ) {
                        globalVimel.caster.disconnect()
                    }
                } else {
                    Icon(
                        imageVector = Icons.Rounded.Cast,
                        contentDescription = "Cast"
                    )
                }
            }
        }
    }
}