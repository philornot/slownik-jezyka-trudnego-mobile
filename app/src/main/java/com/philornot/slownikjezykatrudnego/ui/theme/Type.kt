package com.philornot.slownikjezykatrudnego.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.philornot.slownikjezykatrudnego.data.model.TextSizeLevel

val SerifFontFamily = FontFamily.Serif
val SansFontFamily = FontFamily.SansSerif

/**
 * Creates scaled Material3 Typography based on the user's accessibility text size level.
 */
fun createSjtTypography(textSizeLevel: TextSizeLevel): Typography {
    val scale = when (textSizeLevel) {
        TextSizeLevel.SMALL -> 1.0f
        TextSizeLevel.MEDIUM -> 1.125f
        TextSizeLevel.LARGE -> 1.25f
    }

    return Typography(
        displayLarge = TextStyle(
            fontFamily = SerifFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (32 * scale).sp,
            lineHeight = (40 * scale).sp
        ),
        headlineLarge = TextStyle(
            fontFamily = SerifFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (26 * scale).sp,
            lineHeight = (32 * scale).sp
        ),
        headlineMedium = TextStyle(
            fontFamily = SerifFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (22 * scale).sp,
            lineHeight = (28 * scale).sp
        ),
        headlineSmall = TextStyle(
            fontFamily = SerifFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (18 * scale).sp,
            lineHeight = (24 * scale).sp
        ),
        titleLarge = TextStyle(
            fontFamily = SerifFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (20 * scale).sp,
            lineHeight = (26 * scale).sp
        ),
        titleMedium = TextStyle(
            fontFamily = SansFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (16 * scale).sp,
            lineHeight = (22 * scale).sp
        ),
        titleSmall = TextStyle(
            fontFamily = SansFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (14 * scale).sp,
            lineHeight = (20 * scale).sp
        ),
        bodyLarge = TextStyle(
            fontFamily = SansFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = (15 * scale).sp,
            lineHeight = (22 * scale).sp
        ),
        bodyMedium = TextStyle(
            fontFamily = SansFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (14 * scale).sp,
            lineHeight = (20 * scale).sp
        ),
        bodySmall = TextStyle(
            fontFamily = SansFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (12 * scale).sp,
            lineHeight = (16 * scale).sp
        ),
        labelLarge = TextStyle(
            fontFamily = SansFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (14 * scale).sp,
            lineHeight = (18 * scale).sp
        ),
        labelMedium = TextStyle(
            fontFamily = SansFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (12 * scale).sp,
            lineHeight = (16 * scale).sp
        ),
        labelSmall = TextStyle(
            fontFamily = SansFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (11 * scale).sp,
            lineHeight = (14 * scale).sp
        )
    )
}
