package com.abc.mirroring.cast.screen.cast.audible

import android.annotation.SuppressLint
import androidx.compose.foundation.lazy.LazyListScope.*
import androidx.compose.runtime.Composable
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dev.sofi.extentions.SofiBinding
import dev.sofi.extentions.SofiScreen
import com.abc.mirroring.cast.GlobalVimel
import com.abc.mirroring.cast.section.MediaType
import com.abc.mirroring.cast.setup.graphs.AudioNavGraph

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
