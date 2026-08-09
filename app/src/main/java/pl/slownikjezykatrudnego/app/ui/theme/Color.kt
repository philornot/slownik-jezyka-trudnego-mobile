package pl.slownikjezykatrudnego.app.ui.theme

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
