package com.abc.mirroring.cast

import android.annotation.SuppressLint
import androidx.compose.runtime.compositionLocalOf

class PreviewState() : State

val LocalState = compositionLocalOf<VimelStateHolder<out State>> {
    VimelStateHolder<PreviewState>(PreviewState())
}

@SuppressLint("CompositionLocalNaming")
val GlobalState = compositionLocalOf<VimelStateHolder<GlobalVimel.GlobalState>> {
    VimelStateHolder<GlobalVimel.GlobalState>(GlobalVimel.GlobalState())
}


private fun noLocalProvidedFor(name: String): Nothing {
    error("CompositionLocal $name not present")
}