package com.philornot.slownikjezykatrudnego.ui.theme

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import kotlin.math.hypot
import kotlin.math.max

/**
 * Data class holding the origin point (click position) for a circular reveal animation.
 *
 * @property x X coordinate of the reveal origin in pixels.
 * @property y Y coordinate of the reveal origin in pixels.
 */
data class RevealOrigin(val x: Float, val y: Float)

/**
 * Remembers the current theme transition state for circular reveal animations.
 *
 * @return [ThemeTransitionState] used to trigger and compose the reveal effect.
 */
@Composable
fun rememberThemeTransitionState(): ThemeTransitionState {
    return remember { ThemeTransitionState() }
}

/**
 * State holder for the circular theme reveal animation.
 * Tracks the animation progress, origin, and whether a transition is currently running.
 */
class ThemeTransitionState {
    /** Current reveal progress: 0f = hidden (old theme), 1f = fully revealed (new theme). */
    var progress by mutableFloatStateOf(1f)
        internal set

    /** Origin point (in px) from which the circle expands. Null = no origin set yet. */
    var origin by mutableStateOf<RevealOrigin?>(null)
        internal set

    /** True while an animation is in progress — blocks concurrent triggers. */
    var isAnimating by mutableStateOf(false)
        internal set
}

/**
 * Wraps content with a circular reveal overlay animation that plays when the theme is toggled.
 * Mirrors the web version's `clip-path: circle()` View Transitions API animation.
 *
 * The animation reveals the new theme content from a small circle at [state.origin],
 * expanding outward to cover the entire screen. When [skipAnimation] is true (reducedMotion),
 * the new content is shown instantly without any transition.
 *
 * @param state         The [ThemeTransitionState] controlling this transition.
 * @param skipAnimation When true, transitions are instant (respects reducedMotion setting).
 * @param newContent    Composable rendering the NEW theme state (will be revealed by the circle).
 * @param oldContent    Composable rendering the OLD theme state (visible beneath the reveal).
 * @param modifier      Modifier applied to the container Box.
 */
@Composable
fun CircularRevealThemeWrapper(
    state: ThemeTransitionState,
    skipAnimation: Boolean,
    newContent: @Composable () -> Unit,
    oldContent: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = state.progress
    val origin = state.origin

    Box(modifier = modifier.fillMaxSize()) {
        // Layer 1: Old theme content (always visible underneath)
        oldContent()

        // Layer 2: New theme content — clipped to an expanding circle
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawWithContent {
                    if (progress >= 1f || skipAnimation) {
                        // Fully revealed — no clipping needed
                        drawContent()
                    } else if (origin != null) {
                        // Circular reveal from the toggle button position
                        val o = origin!!
                        val maxRadius = hypot(
                            max(o.x, size.width - o.x).toDouble(),
                            max(o.y, size.height - o.y).toDouble()
                        ).toFloat()
                        val currentRadius = maxRadius * progress

                        val clipPath = Path().apply {
                            addOval(
                                Rect(
                                    center = Offset(o.x, o.y),
                                    radius = currentRadius
                                )
                            )
                        }
                        clipPath(clipPath) {
                            this@drawWithContent.drawContent()
                        }
                    } else {
                        // No origin yet — show normally (snap)
                        drawContent()
                    }
                }
        ) {
            newContent()
        }
    }
}

/**
 * Launches a circular reveal animation using the provided [ThemeTransitionState].
 * Should be called inside a coroutine scope when the theme is toggled.
 *
 * @param state         The transition state to animate.
 * @param origin        Optional pixel offset of the toggle button (for directional reveal).
 * @param skipAnimation If true, completes the transition instantly.
 * @param animSpec      Animation spec controlling duration and easing. Defaults to 600ms ease-out cubic.
 * @param onStart       Called when animation starts — switch to new theme HERE to enable the reveal.
 */
suspend fun animateThemeReveal(
    state: ThemeTransitionState,
    origin: RevealOrigin?,
    skipAnimation: Boolean,
    animSpec: AnimationSpec<Float> = tween(
        durationMillis = 600,
        easing = { t ->
            // ease-out cubic: decelerates into the final position
            val p = t - 1f
            p * p * p + 1f
        }
    ),
    onStart: () -> Unit
) {
    if (state.isAnimating) return

    if (skipAnimation) {
        onStart()
        return
    }

    state.isAnimating = true
    state.origin = origin
    state.progress = 0f

    onStart()

    val anim = Animatable(0f)
    anim.animateTo(targetValue = 1f, animationSpec = animSpec) {
        state.progress = value
    }

    state.progress = 1f
    state.isAnimating = false
}
