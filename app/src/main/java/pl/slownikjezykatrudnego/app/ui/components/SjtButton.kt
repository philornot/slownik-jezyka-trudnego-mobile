package pl.slownikjezykatrudnego.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.slownikjezykatrudnego.app.ui.theme.SjtTheme

/**
 * Large thumb-friendly primary action button (min 52dp height).
 */
@Composable
fun SjtTouchButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    val colors = SjtTheme.colors

    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp),
        shape = RoundedCornerShape(14.dp),
        color = if (enabled) colors.brandPrimary else colors.brandPrimary.copy(alpha = 0.5f),
        contentColor = colors.btnPrimaryText,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            leadingIcon?.let {
                it()
                Row(modifier = Modifier.padding(end = 8.dp)) {}
            }
            Text(
                text = text,
                style = SjtTheme.colors.let {
                    androidx.compose.ui.text.TextStyle(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = colors.btnPrimaryText
                    )
                }
            )
            trailingIcon?.let {
                Row(modifier = Modifier.padding(start = 8.dp)) {}
                it()
            }
        }
    }
}

/**
 * Secondary button with elevated surface and border.
 */
@Composable
fun SjtSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    val colors = SjtTheme.colors

    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = 46.dp),
        shape = RoundedCornerShape(12.dp),
        color = colors.bgSurfaceElevated,
        contentColor = colors.textPrimary,
        border = BorderStroke(1.dp, colors.borderDefault)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            leadingIcon?.let {
                it()
                Row(modifier = Modifier.padding(end = 8.dp)) {}
            }
            Text(
                text = text,
                style = androidx.compose.ui.text.TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = colors.textPrimary
                )
            )
        }
    }
}
