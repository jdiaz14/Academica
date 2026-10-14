package com.example.academica

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import cafe.adriel.voyager.navigator.Navigator

private val USBOrange = Color(0xFFE05A10)
private val USBOrangeContainer = Color(0xFFFFECE5)
private val USBCharcoal = Color(0xFF1C1B1F)
private val BackgroundLight = Color(0xFFF8F9FC)
private val SurfaceVariantLight = Color(0xFFEFF1F6)

private val AppColorScheme = lightColorScheme(
    primary = USBOrange,
    onPrimary = Color.White,
    primaryContainer = USBOrangeContainer,
    onPrimaryContainer = Color(0xFF3E1200),
    secondary = USBCharcoal,
    onSecondary = Color.White,
    secondaryContainer = SurfaceVariantLight,
    onSecondaryContainer = Color(0xFF1C1B1F),
    background = BackgroundLight,
    surface = Color.White,
    onSurface = USBCharcoal,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = Color(0xFF49454F),
    error = Color(0xFFBA1A1A)
)

@Composable
fun App() {
    MaterialTheme(
        colorScheme = AppColorScheme
    ) {
        Navigator(
            screen = InicioScreen()
        )
    }
}
