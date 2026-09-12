package com.philornot.slownikjezykatrudnego.ui.theme

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.view.InputDevice
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Density
import androidx.core.view.WindowCompat
import com.philornot.slownikjezykatrudnego.data.model.TextSizeLevel
import com.philornot.slownikjezykatrudnego.data.model.UserSettings

val LocalSjtColors = staticCompositionLocalOf { SageLightColors }
val LocalUserSettings = staticCompositionLocalOf { UserSettings() }

/**
 * Composition local indicating whether a physical/hardware keyboard is currently attached.
 * False on standard smartphones without an external keyboard attached.
 */
val LocalHasPhysicalKeyboard = staticCompositionLocalOf { false }

/**
 * Composition local providing the motion duration scale (0f = no animations, 1f = full animations).
 * Controlled by the user's reducedMotion preference.
 */
val LocalMotionDurationScale = compositionLocalOf { 1f }

/**
 * Detects whether a physical/hardware keyboard is currently attached and available.
 * Returns false on standard touch-only phones without an external keyboard.
 * Returns true on Chromebooks, tablets with keyboard dock, and devices with connected BT/USB keyboard.
 */
fun checkHasPhysicalKeyboard(context: Context): Boolean {
    val config = context.resources.configuration
    if (config.keyboard == Configuration.KEYBOARD_QWERTY &&
        config.hardKeyboardHidden != Configuration.HARDKEYBOARDHIDDEN_YES
    ) {
        return true
    }
    return try {
        InputDevice.getDeviceIds().any { id ->
            val device = InputDevice.getDevice(id)
            device != null &&
                !device.isVirtual &&
                (device.sources and InputDevice.SOURCE_KEYBOARD) == InputDevice.SOURCE_KEYBOARD &&
                device.keyboardType == InputDevice.KEYBOARD_TYPE_ALPHABETIC
        }
    } catch (_: Exception) {
        false
    }
}

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
        get() = LocalUserSettings.current.reducedMotion || LocalUserSettings.current.eInkMode || LocalMotionDurationScale.current == 0f

    /** Returns true if high-contrast monochrome E-Ink reader mode is active. */
    val isEInk: Boolean
        @Composable
        @ReadOnlyComposable
        get() = LocalUserSettings.current.eInkMode

    /** Returns true if a physical keyboard is attached and available. */
    val hasPhysicalKeyboard: Boolean
        @Composable
        @ReadOnlyComposable
        get() = LocalHasPhysicalKeyboard.current
}

/**
 * Main application theme wrapper providing Sage Green design tokens and accessibility overrides.
 *
 * Handles:
 * - Dark/light mode with proper AppCompatDelegate sync (fixes Pixel flicker bug)
 * - High contrast mode
 * - E-Ink / E-Paper mode (pure monochrome, maximum contrast, zero animations)
 * - reducedMotion via LocalMotionDurationScale
 * - System status/navigation bar colours
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

    val colors = when {
        settings.eInkMode -> if (isDark) EInkDarkColors else EInkLightColors
        settings.highContrast -> {
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
        }
        else -> baseColors
    }

    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val hasPhysicalKeyboard = remember(configuration) {
        checkHasPhysicalKeyboard(context)
    }

    val currentDensity = LocalDensity.current
    val systemFontScale = configuration.fontScale
    val fontScaleMultiplier = when (settings.textSize) {
        TextSizeLevel.SMALL -> 1.0f
        TextSizeLevel.MEDIUM -> 1.20f
        TextSizeLevel.LARGE -> 1.40f
    }
    val effectiveFontScale = systemFontScale * fontScaleMultiplier
    val scaledDensity = Density(
        density = currentDensity.density,
        fontScale = effectiveFontScale
    )

    SideEffect {
        android.util.Log.d(
            "SjtTheme",
            "[THEME] SlownikJezykaTrudnegoTheme composing: textSize=${settings.textSize}, systemFontScale=$systemFontScale, multiplier=$fontScaleMultiplier, effectiveFontScale=$effectiveFontScale, hasPhysicalKeyboard=$hasPhysicalKeyboard, eInkMode=${settings.eInkMode}"
        )
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

    // Motion duration scale: 0f = no animations (reduced motion or e-ink mode), 1f = normal animations.
    val motionDurationScale = if (settings.reducedMotion || settings.eInkMode) 0f else 1f

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            // enableEdgeToEdge() in MainActivity already sets transparent bars.
            // Here we only sync the icon/text appearance (light vs dark).
            val window = (view.context as? Activity)?.window
            if (window != null) {
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
        LocalHasPhysicalKeyboard provides hasPhysicalKeyboard,
        LocalMotionDurationScale provides motionDurationScale,
        LocalDensity provides scaledDensity
    ) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            typography = typography,
            content = content
        )
    }
}