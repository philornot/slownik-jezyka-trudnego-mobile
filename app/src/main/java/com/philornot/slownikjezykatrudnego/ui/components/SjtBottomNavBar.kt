package com.philornot.slownikjezykatrudnego.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.philornot.slownikjezykatrudnego.ui.theme.SjtTheme

enum class SjtTab(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    LESSON("Lekcja", Icons.AutoMirrored.Filled.MenuBook, Icons.AutoMirrored.Outlined.MenuBook),
    CATALOG("Katalog", Icons.Filled.Bookmark, Icons.Outlined.Bookmark),
    STATS("Statystyki", Icons.Filled.BarChart, Icons.Outlined.BarChart)
}

/**
 * Mobile-first bottom navigation bar with thumb ergonomics (56dp min touch target).
 */
@Composable
fun SjtBottomNavBar(
    currentTab: SjtTab,
    onTabSelected: (SjtTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = SjtTheme.colors

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = colors.bgSurface,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            HorizontalDivider(
                thickness = 1.dp,
                color = colors.borderDefault
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val skipAnimations = SjtTheme.skipAnimations
                SjtTab.values().forEach { tab ->
                    val isSelected = currentTab == tab
                    val interactionSource = remember { MutableInteractionSource() }

                    val targetColor = if (isSelected) colors.brandPrimary else colors.textMuted
                    val iconTint by animateColorAsState(
                        targetValue = targetColor,
                        animationSpec = if (skipAnimations) androidx.compose.animation.core.snap() else androidx.compose.animation.core.spring(),
                        label = "iconTint"
                    )

                    val textTint by animateColorAsState(
                        targetValue = targetColor,
                        animationSpec = if (skipAnimations) androidx.compose.animation.core.snap() else androidx.compose.animation.core.spring(),
                        label = "textTint"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp)
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null
                            ) {
                                onTabSelected(tab)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        // Top active tab indicator pill
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .size(width = 36.dp, height = 3.dp)
                                    .background(
                                        color = colors.brandPrimary,
                                        shape = RoundedCornerShape(bottomStart = 3.dp, bottomEnd = 3.dp)
                                    )
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(3.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.label,
                                tint = iconTint,
                                modifier = Modifier.size(26.dp)
                            )

                            Text(
                                text = tab.label.uppercase(),
                                color = textTint,
                                fontSize = 10.5.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
