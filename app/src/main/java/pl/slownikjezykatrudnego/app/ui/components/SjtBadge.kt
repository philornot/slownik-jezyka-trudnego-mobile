package pl.slownikjezykatrudnego.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.slownikjezykatrudnego.app.ui.theme.SjtTheme

enum class BadgeVariant {
    NEUTRAL,
    EMERALD,
    AMBER,
    ROSE
}

/**
 * Pill-shaped badge component matching the design system badges (.badge-emerald, .badge-amber, etc.).
 */
@Composable
fun SjtBadge(
    text: String,
    modifier: Modifier = Modifier,
    variant: BadgeVariant = BadgeVariant.NEUTRAL,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    val colors = SjtTheme.colors

    val (bg, textColor, border) = when (variant) {
        BadgeVariant.NEUTRAL -> Triple(colors.bgSurfaceElevated, colors.textSecondary, colors.borderDefault)
        BadgeVariant.EMERALD -> Triple(colors.badgeEmeraldBg, colors.badgeEmeraldText, colors.badgeEmeraldBorder)
        BadgeVariant.AMBER -> Triple(colors.badgeAmberBg, colors.badgeAmberText, colors.badgeAmberBorder)
        BadgeVariant.ROSE -> Triple(colors.badgeRoseBg, colors.badgeRoseText, colors.badgeRoseBorder)
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(9999.dp),
        color = bg,
        border = BorderStroke(1.dp, border)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            leadingIcon?.invoke()
            Text(
                text = text,
                color = textColor,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
