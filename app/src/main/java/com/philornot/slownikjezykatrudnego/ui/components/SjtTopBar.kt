package com.philornot.slownikjezykatrudnego.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.philornot.slownikjezykatrudnego.R
import com.philornot.slownikjezykatrudnego.ui.theme.SjtTheme

/**
 * Top app bar with logo, streak counter, theme toggle, and settings button.
 * Touch targets are 48×48dp (Material 3 minimum) for thumb ergonomics.
 * Theme toggle reports its screen position for the circular reveal animation.
 *
 * @param streakDays     Number of consecutive learning days (0 = hidden).
 * @param isDarkTheme    Current theme state (for icon selection).
 * @param onToggleTheme  Called when theme toggle is pressed, with the button center offset.
 *                       Null = button is disabled (during animation).
 * @param onOpenSettings Callback to open the settings bottom sheet.
 * @param onOpenAccount  Callback to open the account bottom sheet.
 */
@Composable
fun SjtTopBar(
    streakDays: Int,
    isDarkTheme: Boolean,
    onToggleTheme: ((Offset?) -> Unit)?,
    onOpenSettings: () -> Unit,
    onOpenAccount: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = SjtTheme.colors

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = colors.bgApp,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Brand Logo + Streak Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val logoRes = if (isDarkTheme) R.drawable.logo_circle_dark else R.drawable.logo_circle
                Image(
                    painter = painterResource(id = logoRes),
                    contentDescription = "Słownik Języka Trudnego Logo",
                    modifier = Modifier.size(36.dp)
                )

                if (streakDays > 0) {
                    Surface(
                        shape = RoundedCornerShape(9999.dp),
                        color = colors.badgeAmberBg,
                        border = BorderStroke(1.dp, colors.badgeAmberBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = "Seria dni",
                                tint = colors.badgeAmberText,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "$streakDays dni",
                                color = colors.badgeAmberText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Action Buttons — 48dp touch targets (Material 3 minimum)
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Theme Toggle — captures screen position for circular reveal
                var themeButtonCenter = Offset.Zero
                Surface(
                    onClick = { if (onToggleTheme != null) onToggleTheme(themeButtonCenter) },
                    modifier = Modifier
                        .size(48.dp)
                        .onGloballyPositioned { coords ->
                            val pos = coords.positionInWindow()
                            val size = coords.size
                            themeButtonCenter = Offset(
                                pos.x + size.width / 2f,
                                pos.y + size.height / 2f
                            )
                        },
                    shape = RoundedCornerShape(12.dp),
                    color = colors.bgSurface,
                    border = BorderStroke(1.dp, colors.borderDefault),
                    enabled = onToggleTheme != null
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Przełącz motyw",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Settings Button — 48dp
                Surface(
                    onClick = onOpenSettings,
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = colors.bgSurface,
                    border = BorderStroke(1.dp, colors.borderDefault)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Ustawienia",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Account Button — 48dp
                Surface(
                    onClick = onOpenAccount,
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = colors.bgSurface,
                    border = BorderStroke(1.dp, colors.borderDefault)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Konto",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
