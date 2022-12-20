package com.abc.mirroring.cast.screen.cast.iptv

import android.app.Activity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.abc.mirroring.R
import com.abc.mirroring.cast.shared.ui.component.small_top_bar
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dev.sofi.ads.AdCenter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Destination
fun iptv_(
    navigator: DestinationsNavigator
) {
    val activity = LocalContext.current
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
                    title = stringResource(id = R.string.iptv)
                )
            },

            ) { padding ->

            }
        AdCenter.getInstance().native?.medium()
}