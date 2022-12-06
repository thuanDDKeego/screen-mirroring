package com.abc.mirroring.cast.setup.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.rememberSystemUiController

private val DarkColorPalette = darkColorScheme(
    primary = Purple200,
    secondary = Teal200,
    background = Color(0xFFF5F5F5)
)

private val LightColorPalette = lightColorScheme(
    primary = Color(0xFF8b7ffc),
    secondary = Color(0xFF240868),
    background = Color(0xFFF5F5F5),
    surface = Color(0xFFF5F5F5)

    /* Other default colors to override
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    */
)

@Composable
fun CastTvTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
//    TODO: Support darktheme
//    val colors = if (darkTheme) {
//        DarkColorPalette
//    } else {
//        LightColorPalette
//    }
    
    val colors = LightColorPalette
    val systemUiController = rememberSystemUiController()
    systemUiController.setSystemBarsColor(
        color = colors.background
    )

    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}