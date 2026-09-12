package com.philornot.slownikjezykatrudnego.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.philornot.slownikjezykatrudnego.R
import com.philornot.slownikjezykatrudnego.ui.theme.SjtTheme

/**
 * Adaptive navigation rail for large screens (tablets, Chromebooks, Samsung DeX,
 * foldables in tabletop/unfolded mode, landscape phones).
 *
 * Replaces both the top bar and bottom bar in wide layouts:
 * - Top: Brand Logo + Streak Counter
 * - Center: Navigation destinations with keyboard shortcut hints [1], [2], [3]
 * - Bottom: Settings & Account action buttons
 */
@Composable
fun SjtNavigationRail(
    currentTab: SjtTab,
    onTabSelected: (SjtTab) -> Unit,
    streakDays: Int,
    isDarkTheme: Boolean,
    onOpenSettings: () -> Unit,
    onOpenAccount: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = SjtTheme.colors
    val skipAnimations = SjtTheme.skipAnimations

    Surface(
        modifier = modifier
            .fillMaxHeight()
            .width(88.dp),
        color = colors.bgSurface,
        shadowElevation = 4.dp
    ) {
        Row(modifier = Modifier.fillMaxHeight()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .statusBarsPadding()
                    .displayCutoutPadding()
                    .navigationBarsPadding()
                    .padding(vertical = 12.dp, horizontal = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // ─── Header: Logo + Streak ───
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    val logoRes = if (isDarkTheme) R.drawable.logo_circle_dark else R.drawable.logo_circle
                    Image(
                        painter = painterResource(id = logoRes),
                        contentDescription = "Słownik Języka Trudnego Logo",
                        colorFilter = if (SjtTheme.isEInk) ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) }) else null,
                        modifier = Modifier.size(38.dp)
                    )

                    if (streakDays > 0) {
                        Surface(
                            shape = RoundedCornerShape(9999.dp),
                            color = colors.badgeAmberBg,
                            border = BorderStroke(1.dp, colors.badgeAmberBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = "Seria dni",
                                    tint = colors.badgeAmberText,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "$streakDays",
                                    color = colors.badgeAmberText,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }

                // ─── Center: Navigation Tabs ───
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(vertical = 12.dp)
                ) {
                    SjtTab.values().forEachIndexed { index, tab ->
                        val isSelected = currentTab == tab
                        val interactionSource = remember { MutableInteractionSource() }

                        val targetColor = if (isSelected) colors.brandPrimary else colors.textMuted
                        val animSpec: AnimationSpec<Color> = if (skipAnimations) snap() else spring()

                        val iconTint by animateColorAsState(
                            targetValue = targetColor,
                            animationSpec = animSpec,
                            label = "railIconTint"
                        )
                        val textTint by animateColorAsState(
                            targetValue = targetColor,
                            animationSpec = animSpec,
                            label = "railTextTint"
                        )

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(66.dp)
                                .pointerHoverIcon(PointerIcon.Hand),
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) colors.bgSurfaceElevated else androidx.compose.ui.graphics.Color.Transparent,
                            border = if (isSelected) BorderStroke(1.dp, colors.borderDefault) else null,
                            onClick = { onTabSelected(tab) }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                // Left active pill indicator
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.CenterStart)
                                            .size(width = 3.5.dp, height = 28.dp)
                                            .background(
                                                color = colors.brandPrimary,
                                                shape = RoundedCornerShape(topEnd = 3.dp, bottomEnd = 3.dp)
                                            )
                                    )
                                }

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(3.dp),
                                    modifier = Modifier.padding(vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.label,
                                        tint = iconTint,
                                        modifier = Modifier.size(24.dp)
                                    )

                                    Text(
                                        text = tab.label,
                                        color = textTint,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                        letterSpacing = 0.3.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // ─── Bottom: Settings & Account Buttons ───
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    // Settings Button
                    Surface(
                        onClick = onOpenSettings,
                        modifier = Modifier
                            .size(44.dp)
                            .pointerHoverIcon(PointerIcon.Hand),
                        shape = RoundedCornerShape(12.dp),
                        color = colors.bgSurfaceElevated,
                        border = BorderStroke(1.dp, colors.borderDefault)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Ustawienia",
                                tint = colors.textSecondary,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }

                    // Account Button
                    Surface(
                        onClick = onOpenAccount,
                        modifier = Modifier
                            .size(44.dp)
                            .pointerHoverIcon(PointerIcon.Hand),
                        shape = RoundedCornerShape(12.dp),
                        color = colors.bgSurfaceElevated,
                        border = BorderStroke(1.dp, colors.borderDefault)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Konto",
                                tint = colors.textSecondary,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                }
            }

            // Divider separating navigation rail from screen content
            VerticalDivider(
                thickness = 1.dp,
                color = colors.borderDefault
            )
        }
    }
}
