package com.abc.mirroring.cast.shared.ui.component

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.rounded.Cast
import androidx.compose.material.icons.rounded.CastConnected
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.abc.mirroring.cast.GlobalState
import com.abc.mirroring.cast.GlobalVimel
import com.abc.mirroring.R
import net.sofigo.cast.tv.screen.destinations.premium_Destination
import com.abc.mirroring.cast.setup.config.AppConfigRemote

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun small_top_bar(
    navigator: DestinationsNavigator,
    title: String = "",
    navigatorIcon: @Composable (() -> Unit) = {
        IconButton(onClick = {
            navigator.popBackStack()
        }) {
            Icon(
                Icons.Filled.ArrowBack,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    },
    color: Color = MaterialTheme.colorScheme.background,
    actions: @Composable (() -> Unit)? = null
) {
    val context = LocalContext.current
    val globalVimel = GlobalState.current as GlobalVimel
    val globalState by GlobalState.current.state.collectAsState()

    val enablePremium = AppConfigRemote.premium_enable!!

    // show / hide disconnect device confirmation
    var dialogVisibility by remember { mutableStateOf(false) }

    TopAppBar(
        colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = color),
        title = {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onBackground
            )
        },
        navigationIcon = {
            navigatorIcon.invoke()
        },
        actions = {
            if (actions != null) {
                actions.invoke()
            } else {
                if (enablePremium) {
                    IconButton(onClick = { navigator.navigate(premium_Destination()) }) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_crown),
                            contentDescription = "Premium"
                        )
                    }
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
    )
}

@Composable
fun top_bar_webview(
    modifier: Modifier = Modifier,
    navigator: DestinationsNavigator,
    onBack: () -> Unit,
    txtSearch: MutableState<String>,
    onSearch: () -> Unit,
    actions: (@Composable () -> Unit)? = null
) {
    val context = LocalContext.current
    val globalVimel = GlobalState.current as GlobalVimel
    val globalState by GlobalState.current.state.collectAsState()

    val enablePremium = AppConfigRemote.premium_enable!!

    // used to clear focus in text-field
    val focusManager = LocalFocusManager.current


    // show / hide disconnect device confirmation
    var dialogVisibility by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        IconButton(onClick = {
            onBack.invoke()
        }) {
            Icon(
                Icons.Filled.ArrowBack,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        if (actions != null) {
            actions.invoke()
        } else {
            Box(
                Modifier
                    .padding(horizontal = 8.dp)
                    .background(shape = RoundedCornerShape(10.dp), color = Color.White)
                    .height(36.dp)
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                BasicTextField(
                    value = txtSearch.value,
                    onValueChange = { txtSearch.value = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 36.dp),
                    singleLine = true,
                    textStyle = TextStyle(textAlign = TextAlign.Left),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            //clear focus on text-field
                            focusManager.clearFocus()
                            onSearch()
                        }
                    ),
                    decorationBox = { innerTextField ->
                        Text(
                            text = "",
                            color = Color.Black,
                            fontSize = 12.sp,
                        )
                        innerTextField()
                    }
                )
                //x button to clear url
                Icon(
                    imageVector = Icons.Rounded.Close, contentDescription = "clear url",
                    modifier = Modifier
                        .padding(6.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            txtSearch.value = ""
                        }
                        .size(24.dp)
                        .align(Alignment.CenterEnd)
                )
            }
            if (enablePremium) {
                IconButton(onClick = { navigator.navigate(premium_Destination()) }) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_crown),
                        contentDescription = "Premium",
                    )
                }
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