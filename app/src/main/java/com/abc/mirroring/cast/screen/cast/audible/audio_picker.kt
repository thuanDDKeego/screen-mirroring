package com.abc.mirroring.cast.screen.cast.audible

import android.annotation.SuppressLint
import android.app.Activity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.abc.mirroring.R
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dev.sofi.extentions.SofiBinding
import dev.sofi.extentions.SofiScreen
import com.abc.mirroring.cast.GlobalVimel
import com.abc.mirroring.cast.screen.cast.audible.component.audibles
import com.abc.mirroring.cast.screen.destinations.audible_player_Destination
import com.abc.mirroring.cast.section.MediaType
import com.abc.mirroring.cast.section.SourceType
import com.abc.mirroring.cast.setup.graphs.AudioNavGraph
import com.abc.mirroring.cast.shared.Permissionary
import com.abc.mirroring.cast.shared.ui.component.small_top_bar

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnrememberedMutableState")
@AudioNavGraph(start = true)
@Destination
@Composable
@SofiScreen
fun audio_picker_(
    @SofiBinding navigator: DestinationsNavigator,
    @SofiBinding vm: AudibleVimel,
    @SofiBinding main: GlobalVimel,
) {
    video_picker(navigator = navigator, vm = vm, main = main, type = MediaType.Audio)
}
