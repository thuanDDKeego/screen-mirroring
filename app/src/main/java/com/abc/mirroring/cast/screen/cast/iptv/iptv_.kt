package com.abc.mirroring.cast.screen.cast.iptv

import android.app.Activity
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.rounded.Cast
import androidx.compose.material.icons.rounded.CastConnected
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import com.abc.mirroring.R
import com.abc.mirroring.cast.GlobalState
import com.abc.mirroring.cast.GlobalVimel
import com.abc.mirroring.cast.screen.cast.audible.AudibleParameter
import com.abc.mirroring.cast.screen.cast.iptv.component.dialog_iptv_url
import com.abc.mirroring.cast.section.MediaType
import com.abc.mirroring.cast.section.SourceType
import com.abc.mirroring.cast.setup.graphs.IPTVNavGraph
import com.abc.mirroring.cast.shared.ui.component._dialog
import com.abc.mirroring.cast.shared.ui.component.small_top_bar
import com.abc.mirroring.destinations.audible_player_Destination
import com.abc.mirroring.destinations.m3u8_picker_Destination
import com.abc.mirroring.ui.dialog.DialogCenter
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dev.sofi.ads.AdCenter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@IPTVNavGraph(start = true)
@Composable
@Destination
fun iptv_(
    navigator: DestinationsNavigator,
    vm: IPTVVimel
) {
    val activity = LocalContext.current as Activity
    val state by vm.state.collectAsState()
    val caster = vm.caster
    //visibility state of dialog add m3u url
    var isDialogAddIPTVShow by remember { mutableStateOf(false) }

    LaunchedEffect(true) {
        //if we back from channels_picker, we need reset channels to empty )
        vm.resetChannels()
        vm.resetState()
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
                            activity.finish()
                        }) {
                            Icon(
                                Icons.Filled.ArrowBack,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    title = stringResource(id = R.string.iptv),
                    actions = {
                        _iptv_actions_top_bar {
                            isDialogAddIPTVShow = true
                        }
                    }
                )
            },

            ) { padding ->
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                //if have no m3us item, show background and add button
                if (state.m3us.isEmpty()) {
                    Image(
                        painter = painterResource(id = R.mipmap.bg_iptv), contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(36.dp),
                        contentScale = ContentScale.FillWidth
                    )
                    Box(modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0091EA))
                        .clickable {
                            isDialogAddIPTVShow = true
//                            caster.cast(caster.discovery.device.value!!) {}
//                            state.currentM3U = "index.m3u"
//                            navigator.navigate(m3u8_picker_Destination())
                        }
                        .padding(vertical = 8.dp, horizontal = 36.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.add), textAlign = TextAlign.Center, color = Color.White, fontSize = 14.sp
                        )
                    }
                }
                //if m3us has any item, show lazy-list
                else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        state = rememberLazyListState(),
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(state.m3us, key = { it.url + it.name }) {
                            _m3u_item(item = it) {
                                vm.updateCurrentM3U(it)
                                navigator.navigate(m3u8_picker_Destination())
                            }
                        }
                    }
                }
            }
            if (isDialogAddIPTVShow) {
                dialog_iptv_url(onHide = {
                    isDialogAddIPTVShow = false
                }) {
                    vm.addM3U(it)
                }
            }
        }
        AdCenter.getInstance().native?.medium()
    }
}

@Composable
private fun _iptv_actions_top_bar(
    onAddIPTV: () -> Unit
) {
    val context = LocalContext.current
    val dialogCenter = DialogCenter(context as Activity)
    val globalVimel = GlobalState.current as GlobalVimel
    val globalState by GlobalState.current.state.collectAsState()

    // show / hide disconnect device confirmation
    var dialogVisibility by remember { mutableStateOf(false) }
    if (!dialogCenter.isIgnoringBatteryOptimizations(context)) {
        IconButton(onClick = {
            dialogCenter.showDialog(DialogCenter.DialogType.StopOptimizeBattery)
        }) {
            Image(
                painter = painterResource(id = R.drawable.ic_battery_warning),
                contentDescription = "Battery optimize"
            )
        }
    }

    IconButton(onClick = {
        //TODO tracking add iptv
//        FirebaseTracking.log(FirebaseLogEvent.SmallTopBar_Click_Tutorial)
        onAddIPTV()
    }) {
        Image(
            imageVector = Icons.Filled.AddCircle,
            contentDescription = "Help"
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

@Composable
private fun _m3u_item(modifier: Modifier = Modifier, item: M3UItem, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically, modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable {
                onClick()
            }
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(color = Color(0x6629B6F6), RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            Icon(painter = painterResource(id = R.drawable.ic_television_iptv), contentDescription = "iptv", tint = Color(0xFF29B6F6), modifier = Modifier.fillMaxSize())
        }
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



