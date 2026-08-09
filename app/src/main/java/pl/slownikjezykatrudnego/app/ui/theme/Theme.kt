package pl.slownikjezykatrudnego.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import pl.slownikjezykatrudnego.app.data.model.TextSizeLevel
import pl.slownikjezykatrudnego.app.data.model.UserSettings

val LocalSjtColors = staticCompositionLocalOf { SageLightColors }
val LocalUserSettings = staticCompositionLocalOf { UserSettings() }

object SjtTheme {
    val colors: SjtColors
        @Composable
        @ReadOnlyComposable
        get() = LocalSjtColors.current

    val settings: UserSettings
        @Composable
        @ReadOnlyComposable
        get() = LocalUserSettings.current
}

/**
 * Main application theme wrapper providing Sage Green design tokens and accessibility overrides.
 */
@Composable
fun SlownikJezykaTrudnegoTheme(
    settings: UserSettings = UserSettings(),
    content: @Composable () -> Unit
) {
    val isDark = settings.isDarkTheme ?: isSystemInDarkTheme()
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

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
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
        LocalUserSettings provides settings
    ) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            typography = typography,
            content = content
        )
    }
}
