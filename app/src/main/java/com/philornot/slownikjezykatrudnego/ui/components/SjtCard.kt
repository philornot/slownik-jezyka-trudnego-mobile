package com.philornot.slownikjezykatrudnego.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.philornot.slownikjezykatrudnego.ui.theme.SjtTheme

/**
 * Reusable container card matching .app-card design system token.
 */
@Composable
fun SjtCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = SjtTheme.colors.bgSurface,
    borderColor: Color = SjtTheme.colors.borderDefault,
    cornerRadius: Dp = 16.dp,
    elevation: Dp = 1.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius),
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = elevation
    ) {
        Column {
            content()
        }
    }
}

/**
 * Interactive card with click handler matching .app-card-interactive design token.
 */
@Composable
fun SjtInteractiveCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = SjtTheme.colors.bgSurface,
    borderColor: Color = SjtTheme.colors.borderDefault,
    cornerRadius: Dp = 14.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius),
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column {
            content()
        }
    }
}
