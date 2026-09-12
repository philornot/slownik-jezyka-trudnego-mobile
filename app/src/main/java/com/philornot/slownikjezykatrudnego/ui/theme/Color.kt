package com.philornot.slownikjezykatrudnego.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Exact color palette tokens matching the web design system ("Wyrazista Przydymiona Szałwia").
 */
@Immutable
data class SjtColors(
    val bgApp: Color,
    val bgSurface: Color,
    val bgSurfaceElevated: Color,
    val bgSurfaceMuted: Color,
    val borderDefault: Color,
    val borderMuted: Color,
    val brandPrimary: Color,
    val brandPrimaryHover: Color,
    val btnPrimaryText: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val textSerifTitle: Color,
    val textAmberBrand: Color,

    // Badges
    val badgeAmberBg: Color,
    val badgeAmberText: Color,
    val badgeAmberBorder: Color,
    val badgeEmeraldBg: Color,
    val badgeEmeraldText: Color,
    val badgeEmeraldBorder: Color,
    val badgeRoseBg: Color,
    val badgeRoseText: Color,
    val badgeRoseBorder: Color,

    // SM-2 Grade Buttons
    val grade0Bg: Color,
    val grade0Border: Color,
    val grade0Text: Color,
    val grade3Bg: Color,
    val grade3Border: Color,
    val grade3Text: Color,
    val grade4Bg: Color,
    val grade4Border: Color,
    val grade4Text: Color,
    val grade5Bg: Color,
    val grade5Border: Color,
    val grade5Text: Color,

    // Blockquotes & Bars
    val blockquoteBg: Color,
    val progressTrack: Color,
    val progressBorder: Color,
    val barActive: Color,
    val barInactive: Color,
    val isDark: Boolean
)

val SageLightColors = SjtColors(
    bgApp = Color(0xFFF2F5F3),
    bgSurface = Color(0xFFFFFFFF),
    bgSurfaceElevated = Color(0xFFE3EBE5),
    bgSurfaceMuted = Color(0xFFD0DED4),
    borderDefault = Color(0xFFBACBBF),
    borderMuted = Color(0xFFD4E0D7),
    brandPrimary = Color(0xFF2E4D3E),
    brandPrimaryHover = Color(0xFF223B2F),
    btnPrimaryText = Color(0xFFFFFFFF),
    textPrimary = Color(0xFF18261E),
    textSecondary = Color(0xFF27382D),
    textMuted = Color(0xFF4E6355),
    textSerifTitle = Color(0xFF12241B),
    textAmberBrand = Color(0xFF2E4D3E),

    badgeAmberBg = Color(0xFFF5F0E6),
    badgeAmberText = Color(0xFF6B5635),
    badgeAmberBorder = Color(0xFFE6DAC6),
    badgeEmeraldBg = Color(0xFFDBE6DE),
    badgeEmeraldText = Color(0xFF1E382B),
    badgeEmeraldBorder = Color(0xFFA8C4B0),
    badgeRoseBg = Color(0xFFF7E8E8),
    badgeRoseText = Color(0xFF6E2C2C),
    badgeRoseBorder = Color(0xFFDBA4A4),

    grade0Bg = Color(0xFFFDE8E8),
    grade0Border = Color(0xFFFCA5A5),
    grade0Text = Color(0xFF991B1B),
    grade3Bg = Color(0xFFFFF3E0),
    grade3Border = Color(0xFFFDBA74),
    grade3Text = Color(0xFFC2410C),
    grade4Bg = Color(0xFFE0F2FE),
    grade4Border = Color(0xFF7DD3FC),
    grade4Text = Color(0xFF0369A1),
    grade5Bg = Color(0xFFDCFCE7),
    grade5Border = Color(0xFF6EE7B7),
    grade5Text = Color(0xFF15803D),

    blockquoteBg = Color(0xFFE8F0EA),
    progressTrack = Color(0xFFB8CCBF),
    progressBorder = Color(0xFF799983),
    barActive = Color(0xFF2E4D3E),
    barInactive = Color(0xFFA6C0B0),
    isDark = false
)

val SageDarkColors = SjtColors(
    bgApp = Color(0xFF0E1411),
    bgSurface = Color(0xFF16201B),
    bgSurfaceElevated = Color(0xFF1F2E27),
    bgSurfaceMuted = Color(0xFF2A3D34),
    borderDefault = Color(0xFF31473D),
    borderMuted = Color(0xFF24362E),
    brandPrimary = Color(0xFF52997A),
    brandPrimaryHover = Color(0xFF68B090),
    btnPrimaryText = Color(0xFF06120B),
    textPrimary = Color(0xFFE4F0E9),
    textSecondary = Color(0xFFC6DCD0),
    textMuted = Color(0xFF86A394),
    textSerifTitle = Color(0xFFE8F5EE),
    textAmberBrand = Color(0xFF68B090),

    badgeAmberBg = Color(0x24D4B88A),
    badgeAmberText = Color(0xFFE8D0A9),
    badgeAmberBorder = Color(0x4DD4B88A),
    badgeEmeraldBg = Color(0x2E52997A),
    badgeEmeraldText = Color(0xFF68B090),
    badgeEmeraldBorder = Color(0x5952997A),
    badgeRoseBg = Color(0x2ED98282),
    badgeRoseText = Color(0xFFE8A0A0),
    badgeRoseBorder = Color(0x59D98282),

    grade0Bg = Color(0x29F43F5E),
    grade0Border = Color(0x66F43F5E),
    grade0Text = Color(0xFFFDA4AF),
    grade3Bg = Color(0x29F59E0B),
    grade3Border = Color(0x66F59E0B),
    grade3Text = Color(0xFFFCD34D),
    grade4Bg = Color(0x2938B9F8),
    grade4Border = Color(0x6638B9F8),
    grade4Text = Color(0xFF7DD3FC),
    grade5Bg = Color(0x2922C55E),
    grade5Border = Color(0x6622C55E),
    grade5Text = Color(0xFF86EFAC),

    blockquoteBg = Color(0x1F52997A),
    progressTrack = Color(0xFF263B31),
    progressBorder = Color(0xFF446654),
    barActive = Color(0xFF52997A),
    barInactive = Color(0xFF1C2C23),
    isDark = true
)

/**
 * High-contrast pure monochrome palette for E-Ink / E-Paper screens in Dark mode.
 * Zero green/amber/rose color tint — strictly black, white, and high-contrast grayscale.
 */
val EInkDarkColors = SjtColors(
    bgApp = Color(0xFF000000),
    bgSurface = Color(0xFF000000),
    bgSurfaceElevated = Color(0xFF141414),
    bgSurfaceMuted = Color(0xFF222222),
    borderDefault = Color(0xFFFFFFFF),
    borderMuted = Color(0xFF777777),
    brandPrimary = Color(0xFFFFFFFF),
    brandPrimaryHover = Color(0xFFE0E0E0),
    btnPrimaryText = Color(0xFF000000),
    textPrimary = Color(0xFFFFFFFF),
    textSecondary = Color(0xFFE0E0E0),
    textMuted = Color(0xFFAAAAAA),
    textSerifTitle = Color(0xFFFFFFFF),
    textAmberBrand = Color(0xFFFFFFFF),

    badgeAmberBg = Color(0xFF1A1A1A),
    badgeAmberText = Color(0xFFFFFFFF),
    badgeAmberBorder = Color(0xFF888888),
    badgeEmeraldBg = Color(0xFF1A1A1A),
    badgeEmeraldText = Color(0xFFFFFFFF),
    badgeEmeraldBorder = Color(0xFF888888),
    badgeRoseBg = Color(0xFF1A1A1A),
    badgeRoseText = Color(0xFFFFFFFF),
    badgeRoseBorder = Color(0xFF888888),

    grade0Bg = Color(0xFF141414),
    grade0Border = Color(0xFF888888),
    grade0Text = Color(0xFFFFFFFF),
    grade3Bg = Color(0xFF1E1E1E),
    grade3Border = Color(0xFFAAAAAA),
    grade3Text = Color(0xFFFFFFFF),
    grade4Bg = Color(0xFF2A2A2A),
    grade4Border = Color(0xFFCCCCCC),
    grade4Text = Color(0xFFFFFFFF),
    grade5Bg = Color(0xFF383838),
    grade5Border = Color(0xFFFFFFFF),
    grade5Text = Color(0xFFFFFFFF),

    blockquoteBg = Color(0xFF141414),
    progressTrack = Color(0xFF333333),
    progressBorder = Color(0xFFFFFFFF),
    barActive = Color(0xFFFFFFFF),
    barInactive = Color(0xFF2B2B2B),
    isDark = true
)

/**
 * High-contrast pure monochrome palette for E-Ink / E-Paper screens in Light mode.
 * Zero green/amber/rose color tint — strictly white, black, and high-contrast grayscale.
 */
val EInkLightColors = SjtColors(
    bgApp = Color(0xFFFFFFFF),
    bgSurface = Color(0xFFFFFFFF),
    bgSurfaceElevated = Color(0xFFF4F4F4),
    bgSurfaceMuted = Color(0xFFE6E6E6),
    borderDefault = Color(0xFF000000),
    borderMuted = Color(0xFF888888),
    brandPrimary = Color(0xFF000000),
    brandPrimaryHover = Color(0xFF222222),
    btnPrimaryText = Color(0xFFFFFFFF),
    textPrimary = Color(0xFF000000),
    textSecondary = Color(0xFF222222),
    textMuted = Color(0xFF555555),
    textSerifTitle = Color(0xFF000000),
    textAmberBrand = Color(0xFF000000),

    badgeAmberBg = Color(0xFFEFEFEF),
    badgeAmberText = Color(0xFF000000),
    badgeAmberBorder = Color(0xFF888888),
    badgeEmeraldBg = Color(0xFFEFEFEF),
    badgeEmeraldText = Color(0xFF000000),
    badgeEmeraldBorder = Color(0xFF888888),
    badgeRoseBg = Color(0xFFEFEFEF),
    badgeRoseText = Color(0xFF000000),
    badgeRoseBorder = Color(0xFF888888),

    grade0Bg = Color(0xFFFFFFFF),
    grade0Border = Color(0xFF888888),
    grade0Text = Color(0xFF000000),
    grade3Bg = Color(0xFFF2F2F2),
    grade3Border = Color(0xFF666666),
    grade3Text = Color(0xFF000000),
    grade4Bg = Color(0xFFE4E4E4),
    grade4Border = Color(0xFF333333),
    grade4Text = Color(0xFF000000),
    grade5Bg = Color(0xFFD6D6D6),
    grade5Border = Color(0xFF000000),
    grade5Text = Color(0xFF000000),

    blockquoteBg = Color(0xFFF2F2F2),
    progressTrack = Color(0xFFDDDDDD),
    progressBorder = Color(0xFF000000),
    barActive = Color(0xFF000000),
    barInactive = Color(0xFFCCCCCC),
    isDark = false
)