package com.transservice.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val PrimaryColor = Color(0xFF1B5E20)   // vert foncé (transport/logistique)
private val SecondaryColor = Color(0xFFFFA000) // ambre

private val LightColors = lightColorScheme(
    primary = PrimaryColor,
    secondary = SecondaryColor
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF66BB6A),
    secondary = SecondaryColor
)

@Composable
fun TransServiceTheme(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, content = content)
}
