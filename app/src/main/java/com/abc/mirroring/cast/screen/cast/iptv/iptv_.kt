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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abc.mirroring.R
import com.abc.mirroring.cast.GlobalState
import com.abc.mirroring.cast.GlobalVimel
import com.abc.mirroring.cast.shared.ui.component._dialog
import com.abc.mirroring.cast.shared.ui.component.small_top_bar
import com.abc.mirroring.ui.dialog.DialogCenter
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dev.sofi.ads.AdCenter
import net.bjoernpetersen.m3u.M3uParser
import net.bjoernpetersen.m3u.model.M3uEntry
import okio.source
import timber.log.Timber
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.file.Paths

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Destination
fun iptv_(
    navigator: DestinationsNavigator,
    main: GlobalVimel
) {
    val activity = LocalContext.current as Activity
    var isDialogAddIPTVShow by remember { mutableStateOf(false) }
    var isShowApologizeMessage by remember { mutableStateOf(false) }
    var caster = main.caster
    LaunchedEffect(true) {
        val m3uFile = Paths.get("index.m3u")
//        val fileEntries: List<M3uEntry> = M3uParser.parse(m3uFile)

        val m3uStream: InputStream = activity.resources.openRawResource(R.raw.index)
        val m3uReader: InputStreamReader = m3uStream.reader()
        val streamEntries: List<M3uEntry> = M3uParser.parse(m3uReader)
        streamEntries.forEach {entry ->
//            Timber.d("M3u8File: ${streamEntries[i].title}")

            entry.metadata.entries.joinToString { "${it.key}: ${it.value}" }.let {  Timber.d("M3u8File: $it ${entry.location.url}") }
        }
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
                Image(
                    painter = painterResource(id = R.mipmap.bg_iptv), contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(36.dp),
                    contentScale = ContentScale.FillWidth
                )
                if (!isShowApologizeMessage) {
                    Box(modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0091EA))
                        .clickable {
//                            isDialogAddIPTVShow = true
                            caster.cast(main.caster.discovery.device.value!!) {}
                        }
                        .padding(vertical = 8.dp, horizontal = 36.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.add), textAlign = TextAlign.Center, color = Color.White, fontSize = 14.sp
                        )
                    }
                } else {
                    Text(
                        text = stringResource(id = R.string.iptv_apologize_message), fontSize = 14.sp, color = Color.Red,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
            if (isDialogAddIPTVShow) {
                dialog_iptv_url(onHide = {
                    isDialogAddIPTVShow = false
                }) {
                    isShowApologizeMessage = true
                }
            }
        }
        AdCenter.getInstance().native?.medium()
    }
}

@Composable
fun _iptv_actions_top_bar(
    onAddIPTV: () -> Unit
) {
    val context = LocalContext.current
    val dialogCenter: DialogCenter = DialogCenter(context as Activity)
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