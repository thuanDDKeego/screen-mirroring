package com.abc.mirroring.cast.setup.compose

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview

/**
 * other settings can be placed inside this class
 */
class PreviewModule {

}


/**
 * Setting default previews here
 */
@SuppressLint("ComposableNaming")
@Preview(
    name = "Normal",
    group = "Screen",
    showBackground = true
)
@Preview(
    name = "Dark",
    group = "Screen",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Preview(
    name = "RTL",
    group = "Screen",
    showBackground = true,
    locale = "iw"
)
annotation class CastTvPreviews