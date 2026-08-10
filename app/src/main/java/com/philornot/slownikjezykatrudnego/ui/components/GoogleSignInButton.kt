package com.philornot.slownikjezykatrudnego.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.philornot.slownikjezykatrudnego.R
import com.philornot.slownikjezykatrudnego.ui.theme.SjtTheme

/**
 * Official Google Sign-In button fully adhering to Google Identity Branding Guidelines.
 * Shape: Pill (CircleShape), Colors: Official Light/Dark, Text: Polish ("Kontynuuj z Google").
 *
 * @param onClick   Callback invoked when user clicks the button.
 * @param modifier  Modifier for layout and sizing.
 * @param isLoading True when auth request is currently in flight.
 * @param enabled   Whether the button is interactive.
 * @param text      Button text label.
 */
@Composable
fun GoogleSignInButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    text: String = "Kontynuuj z Google"
) {
    val isDark = SjtTheme.colors.isDark

    // Google Identity official theme specs
    val bgColor = if (isDark) Color(0xFF131314) else Color(0xFFFFFFFF)
    val borderColor = if (isDark) Color(0xFF8E918F) else Color(0xFF747775)
    val textColor = if (isDark) Color(0xFFE3E3E3) else Color(0xFF1F1F1F)
    val logoRes = if (isDark) R.drawable.ic_google_logo_dark else R.drawable.ic_google_logo_light

    Surface(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp),
        shape = CircleShape,
        color = bgColor,
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = if (isDark) 0.dp else 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = textColor
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Łączenie z Google…",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.Default,
                    color = textColor,
                    letterSpacing = 0.25.sp
                )
            } else {
                Image(
                    painter = painterResource(id = logoRes),
                    contentDescription = "Google",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = text,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.Default,
                    color = textColor,
                    letterSpacing = 0.25.sp
                )
            }
        }
    }
}
