package com.philornot.slownikjezykatrudnego.ui.theme

import android.app.Activity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.philornot.slownikjezykatrudnego.data.model.TextSizeLevel
import com.philornot.slownikjezykatrudnego.data.model.UserSettings

val LocalSjtColors = staticCompositionLocalOf { SageLightColors }
val LocalUserSettings = staticCompositionLocalOf { UserSettings() }

/**
 * Composition local providing the motion duration scale (0f = no animations, 1f = full animations).
 * Controlled by the user's reducedMotion preference.
 */
val LocalMotionDurationScale = compositionLocalOf { 1f }

object SjtTheme {
    val colors: SjtColors
        @Composable
        @ReadOnlyComposable
        get() = LocalSjtColors.current

    val settings: UserSettings
        @Composable
        @ReadOnlyComposable
        get() = LocalUserSettings.current

    /** Returns true if animations should be skipped (respects both OS and in-app settings). */
    val skipAnimations: Boolean
        @Composable
        @ReadOnlyComposable
        get() = LocalMotionDurationScale.current == 0f
}

/**
 * Main application theme wrapper providing Sage Green design tokens and accessibility overrides.
 *
 * Handles:
 * - Dark/light mode with proper AppCompatDelegate sync (fixes Pixel flicker bug)
 * - High contrast mode
 * - reducedMotion via LocalMotionDurationScale
 * - System status/navigation bar colors
 *
 * @param settings User configuration preferences.
 * @param content Composable content to render within the theme.
 */
@Composable
fun SlownikJezykaTrudnegoTheme(
    settings: UserSettings = UserSettings(),
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val isDark = settings.isDarkTheme ?: isSystemDark
    val baseColors = if (isDark) SageDarkColors else SageLightColors

    val colors = if (settings.highContrast) {
        if (isDark) {
            baseColors.copy(
                bgSurface = androidx.compose.ui.graphics.Color(0xFF0A0F0D),
                bgSurfaceElevated = androidx.compose.ui.graphics.Color(0xFF131C18),
                borderDefault = androidx.compose.ui.graphics.Color(0xFF52997A),
                textPrimary = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
                textSerifTitle = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
                brandPrimary = androidx.compose.ui.graphics.Color(0xFF68B090)
            )
        } else {
            baseColors.copy(
                bgSurfaceElevated = androidx.compose.ui.graphics.Color(0xFFD4E0D7),
                borderDefault = androidx.compose.ui.graphics.Color(0xFF4E6355),
                textPrimary = androidx.compose.ui.graphics.Color(0xFF000000),
                textSerifTitle = androidx.compose.ui.graphics.Color(0xFF000000),
                brandPrimary = androidx.compose.ui.graphics.Color(0xFF12241B)
            )
        }
    } else {
        baseColors
    }

    val typography = createSjtTypography(settings.textSize)

    val materialColorScheme = if (isDark) {
        darkColorScheme(
            primary = colors.brandPrimary,
            background = colors.bgApp,
            surface = colors.bgSurface,
            onPrimary = colors.btnPrimaryText,
            onBackground = colors.textPrimary,
            onSurface = colors.textPrimary
        )
    } else {
        lightColorScheme(
            primary = colors.brandPrimary,
            background = colors.bgApp,
            surface = colors.bgSurface,
            onPrimary = colors.btnPrimaryText,
            onBackground = colors.textPrimary,
            onSurface = colors.textPrimary
        )
    }

    // Motion duration scale: 0f = no animations (reduced motion), 1f = normal animations.
    val motionDurationScale = if (settings.reducedMotion) 0f else 1f

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            // Sync status/navigation bar appearance.
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colors.bgApp.toArgb()
                window.navigationBarColor = colors.bgSurface.toArgb()
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !isDark
                    isAppearanceLightNavigationBars = !isDark
                }
            }
        }
    }

    CompositionLocalProvider(
        LocalSjtColors provides colors,
        LocalUserSettings provides settings,
        LocalMotionDurationScale provides motionDurationScale
    ) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            typography = typography,
            content = content
        )
    }
}
