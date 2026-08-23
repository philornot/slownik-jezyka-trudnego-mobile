package com.philornot.slownikjezykatrudnego.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.philornot.slownikjezykatrudnego.R
import com.philornot.slownikjezykatrudnego.ui.theme.SansFontFamily
import com.philornot.slownikjezykatrudnego.ui.theme.SjtTheme

/**
 * "Sign in with Google" button.
 *
 * Follows the official Google Identity branding guidelines
 * (https://developers.google.com/identity/branding-guidelines)
 * for button structure:
 * - Official fill/stroke/text colors for the Light and Dark themes.
 * - The updated (2025) full-color gradient Google "G" mark, untouched and
 *   unscaled.
 * - Text uses one of the approved CTAs ("Kontynuuj z Google" = "Continue
 *   with Google").
 * - Official Android padding: 12dp before the logo, 10dp after the logo,
 *   12dp after the text.
 *
 * The shape and height are adapted to match the app's Material
 * button language (same 14dp corner radius and 52dp touch target as
 * [SjtTouchButton]), which the guidelines explicitly allow ("Do: use the
 * Google Material design guidelines for button boundary and color scheme")
 * and which keeps this button visually equal in prominence to the app's
 * other auth actions, as required by the guidelines.
 *
 * @param onClick Callback invoked when the user taps the button.
 * @param modifier Modifier for layout and sizing.
 * @param isLoading True while a Google sign-in request is in flight.
 * @param enabled Whether the button reacts to input.
 * @param text CTA label. Defaults to the recommended, localized "Continue
 *    with Google".
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

    // Official Google Identity color spec — do not alter.
    val bgColor = if (isDark) Color(0xFF131314) else Color(0xFFFFFFFF)
    val borderColor = if (isDark) Color(0xFF8E918F) else Color(0xFF747775)
    val textColor = if (isDark) Color(0xFFE3E3E3) else Color(0xFF1F1F1F)

    val skipAnimations = SjtTheme.skipAnimations
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled && !isLoading && !skipAnimations) 0.98f else 1f,
        animationSpec = if (skipAnimations) androidx.compose.animation.core.snap() else tween(120),
        label = "googleButtonScale"
    )
    val elevation by animateFloatAsState(
        targetValue = if (isDark) 0f else if (isPressed) 0.5f else 1.5f,
        animationSpec = if (skipAnimations) androidx.compose.animation.core.snap() else tween(120),
        label = "googleButtonElevation"
    )

    Surface(
        onClick = onClick,
        enabled = enabled && !isLoading,
        interactionSource = interactionSource,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .scale(scale),
        shape = RoundedCornerShape(14.dp),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = elevation.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            AnimatedContent(
                targetState = isLoading,
                transitionSpec = {
                    if (skipAnimations) {
                        androidx.compose.animation.EnterTransition.None togetherWith androidx.compose.animation.ExitTransition.None
                    } else {
                        fadeIn(tween(150)) togetherWith fadeOut(tween(150))
                    }
                },
                label = "googleButtonContent"
            ) { loading ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = textColor
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Łączenie z Google…",
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = SansFontFamily,
                            color = textColor,
                            letterSpacing = 0.25.sp
                        )
                    } else {
                        // The updated (2025) Google "G" mark is a self-contained gradient
                        // badge — unlike the older flat logo it doesn't need a white backing
                        // tile, so it sits directly on the button surface.
                        Image(
                            painter = painterResource(id = R.drawable.ic_google_logo_g),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = text,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = SansFontFamily,
                            color = textColor,
                            letterSpacing = 0.25.sp
                        )
                    }
                }
            }
        }
    }
}
