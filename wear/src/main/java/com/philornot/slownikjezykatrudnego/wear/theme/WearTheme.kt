package com.philornot.slownikjezykatrudnego.wear.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.MaterialTheme

private val WearColorPalette = Colors(
    primary = Color(0xFF10B981),          // Emerald 500
    primaryVariant = Color(0xFF059669),   // Emerald 600
    secondary = Color(0xFF34D399),        // Emerald 400
    secondaryVariant = Color(0xFF047857), // Emerald 700
    background = Color(0xFF000000),       // Pure AMOLED Black
    surface = Color(0xFF131A16),          // Deep dark surface
    error = Color(0xFFEF4444),
    onPrimary = Color(0xFF021C11),
    onSecondary = Color(0xFF021C11),
    onBackground = Color(0xFFFFFFFF),
    onSurface = Color(0xFFE5E7EB),
    onError = Color(0xFFFFFFFF)
)

@Composable
fun SjtWearTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = WearColorPalette,
        content = content
    )
}
