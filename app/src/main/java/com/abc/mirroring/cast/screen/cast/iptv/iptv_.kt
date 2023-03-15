package com.abc.mirroring.cast.screen.cast.iptv

import android.app.Activity
import android.os.Bundle
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.rounded.Cast
import androidx.compose.material.icons.rounded.CastConnected
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import com.abc.mirroring.cast.screen.cast.iptv.component.dialog_delete_m3u
import com.abc.mirroring.cast.screen.cast.iptv.component.dialog_iptv_url
import com.abc.mirroring.cast.section.data.iptv.db.M3U
import com.abc.mirroring.cast.setup.graphs.IPTVNavGraph
import com.abc.mirroring.cast.shared.ui.component._dialog
import com.abc.mirroring.cast.shared.ui.component.small_top_bar
import com.abc.mirroring.destinations.channel_picker_Destination
import com.abc.mirroring.ui.dialog.DialogCenter
import com.abc.mirroring.ui.tutorial.TutorialActivity
import com.abc.mirroring.utils.FirebaseLogEvent
import com.abc.mirroring.utils.FirebaseTracking
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import one.shot.haki.ads.AdCenter
import timber.log.Timber

const val DEFAULT_CHANNELS_URL = "https://iptv-org.github.io/iptv/index.m3u"

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
    //visibility state of dialog add m3u url
    var isDialogAddIPTVShow by remember { mutableStateOf(false) }
    var isDialogDeleteM3uShow by remember { mutableStateOf(false) }
    var isDialogUpdateM3uShow by remember { mutableStateOf(false) }

    LaunchedEffect(true) {
        //if we back from channels_picker, we need reset channels to empty )
        vm.resetState()
        vm.fetchM3Us()
    }

//    //if m3uWantToDelete Changed, show delete m3u dialog
//    LaunchedEffect(state.m3uWantToDelete) {
//        if(state.m3uWantToDelete != null) isDialogDeleteM3uShow = true
//    }
//    //if m3uWantToUpdate Changed, show update m3u dialog
//    LaunchedEffect(state.m3uWantToUpdate) {
//        if(state.m3uWantToUpdate != null) isDialogUpdateM3uShow = true
//    }

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
                            FirebaseTracking.log(FirebaseLogEvent.IPTV_Click_Back)
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
                        _iptv_actions_top_bar(
                            onHelp = {
                                TutorialActivity.gotoActivity(activity)
                            }
                        ) {
                            FirebaseTracking.log(FirebaseLogEvent.IPTV_Click_Add_TopBar)
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
                            FirebaseTracking.log(FirebaseLogEvent.IPTV_Click_Add)
                            isDialogAddIPTVShow = true
                        }
                        .padding(vertical = 8.dp, horizontal = 36.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.add), textAlign = TextAlign.Center, color = Color.White, fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
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
                            _m3u_item(item = it, onClick = {
                                //log firebase
                                val bundle = Bundle().also { bundle -> bundle.putString("M3U_URL", it.url) }
                                FirebaseTracking.log(FirebaseLogEvent.IPTV_Click_M3U, bundle)

                                vm.updateCurrentM3U(it)
                                AdCenter.getInstance().interstitial?.show(activity) {
                                    navigator.navigate(channel_picker_Destination())
                                }
                            }, onOptionClick = object : OnOptionClick {
                                override fun onDelete() {
                                    FirebaseTracking.log(FirebaseLogEvent.IPTV_Click_DeleteFile)
                                    vm.updateM3uWantToDelete(it)
                                    isDialogDeleteM3uShow = true
                                }

                                override fun onUpdate() {
                                    FirebaseTracking.log(FirebaseLogEvent.IPTV_Click_EditFile)
                                    vm.updateM3uWantToUpdate(it)
                                    isDialogUpdateM3uShow = true
                                }

                            })
                        }
                    }
                }
                AdCenter.getInstance().natives["general"]?.medium()
            }
        }
        if (isDialogAddIPTVShow) {
            dialog_iptv_url(onHide = {
                isDialogAddIPTVShow = false
                FirebaseTracking.log(FirebaseLogEvent.AddIPTV_Click_Cancel)

            }) {
                FirebaseTracking.log(FirebaseLogEvent.AddIPTV_Click_Add)
                vm.addM3U(it)
            }
        }

        if (isDialogUpdateM3uShow) {
            dialog_iptv_url(
                isAddAction = false,
                onHide = {
                    isDialogUpdateM3uShow = false
                }) {
                val itemNeedUpdate = it
                itemNeedUpdate.id = state.m3uWantToUpdate!!.id
                vm.update(itemNeedUpdate)
                Timber.d("----m3u ${itemNeedUpdate.id} ${itemNeedUpdate.name}")
            }
        }
        if (isDialogDeleteM3uShow && state.m3uWantToDelete != null) {
            dialog_delete_m3u(
                title = "Delete \"${state.m3uWantToDelete!!.name}\"",
                onCancel = {
                    FirebaseTracking.log(FirebaseLogEvent.DeleteFile_Click_Cancel)
                    isDialogDeleteM3uShow = false
                }) {
                Timber.d("---m3u delete ${state.m3uWantToDelete!!.id}")
                FirebaseTracking.log(FirebaseLogEvent.DeleteFile_Click_Delete)
                vm.delete(state.m3uWantToDelete!!)
                state.m3uWantToDelete = null
            }
        }
    }
}

@Composable
private fun _iptv_actions_top_bar(
    onHelp: () -> Unit,
    onAddIPTV: () -> Unit
) {
    val context = LocalContext.current
    val dialogCenter = DialogCenter(context as Activity)
    val globalVimel = GlobalState.current as GlobalVimel
    val globalState by GlobalState.current.state.collectAsState()

    // show / hide disconnect device confirmation
    var dialogVisibility by remember { mutableStateOf(false) }
    //optimize battery
//    if (!dialogCenter.isIgnoringBatteryOptimizations(context)) {
//        IconButton(onClick = {
//            dialogCenter.showDialog(DialogCenter.DialogType.StopOptimizeBattery)
//        }) {
//            Image(
//                painter = painterResource(id = R.drawable.ic_battery_warning),
//                contentDescription = "Battery optimize"
//            )
//        }
//    }

    IconButton(onClick = {
        onHelp()
    }) {
        Image(
            imageVector = Icons.Filled.Help,
            contentDescription = "help"
        )
    }

    IconButton(onClick = {
        onAddIPTV()
    }) {
        Image(
            imageVector = Icons.Filled.AddCircle,
            contentDescription = "add"
        )
    }
    IconButton(onClick = {
        if (globalState.isDeviceConnected) {
            dialogVisibility = true
        } else {
            globalVimel.caster.discovery.picker(context = context)
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
private fun _m3u_item(modifier: Modifier = Modifier, item: M3U, onClick: () -> Unit, onOptionClick: OnOptionClick) {
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
        _drop_down_m3u_options(modifier.padding(horizontal = 12.dp), onOptionClick)
    }
}

@Composable
private fun _drop_down_m3u_options(modifier: Modifier = Modifier, onOptionClick: OnOptionClick) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf(stringResource(id = R.string.edit_file), stringResource(id = R.string.delete_file))

    Column(modifier = modifier) {
        Icon(
            imageVector = Icons.Rounded.MoreVert, contentDescription = "options",
            modifier = Modifier
                .size(24.dp)
                .clickable { expanded = true }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .wrapContentWidth()
        ) {
            options.forEachIndexed { index, option ->
                DropdownMenuItem(onClick = {
                    if (index == 0) onOptionClick.onUpdate() else onOptionClick.onDelete()
                    expanded = false
                }, text = {
                    Text(text = option)
                })
            }
        }
    }
}

internal interface OnOptionClick {
    fun onDelete()
    fun onUpdate()
}



