package com.example.stalkerplayer.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkScheme = darkColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    secondary = Accent,
    onSecondary = Color.Black,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnBackground,
    surfaceVariant = SurfaceVariant,
    error = ErrorColor
)

@Composable
fun StalkerPlayerTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        val window = (view.context as? android.app.Activity)?.window
        window?.let {
            it.statusBarColor = Background.toArgb()
            it.navigationBarColor = Background.toArgb()
            WindowCompat.getInsetsController(it, view).isAppearanceLightStatusBars = false
        }
    }
    MaterialTheme(
        colorScheme = DarkScheme,
        typography = AppTypography,
        content = content
    )
}
