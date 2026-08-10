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
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.philornot.slownikjezykatrudnego.R
import com.philornot.slownikjezykatrudnego.ui.theme.SjtTheme

/**
 * Top app bar with logo, streak counter, settings and account buttons.
 * Touch targets are 48×48dp (Material 3 minimum) for thumb ergonomics.
 * Theme toggle has been moved to the Settings sheet.
 *
 * @param streakDays     Number of consecutive learning days (0 = hidden).
 * @param isDarkTheme    Current theme state (for logo selection).
 * @param onOpenSettings Callback to open the settings bottom sheet.
 * @param onOpenAccount  Callback to open the account bottom sheet.
 */
@Composable
fun SjtTopBar(
    streakDays: Int,
    isDarkTheme: Boolean,
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
