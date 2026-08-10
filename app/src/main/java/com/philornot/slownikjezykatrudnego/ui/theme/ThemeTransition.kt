package com.philornot.slownikjezykatrudnego.ui.theme

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.unit.IntSize
import kotlin.math.hypot
import kotlin.math.max

/**
 * Logcat tag for filtering theme transition animation steps and lifecycle events.
 */
const val THEME_TRANSITION_TAG = "ThemeTransition"

/**
 * CompositionLocal providing access to the current [ThemeTransitionState] across screens and modal bottom sheets.
 */
val LocalThemeTransitionState = androidx.compose.runtime.compositionLocalOf<ThemeTransitionState?> { null }

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
 * Tracks the animation progress, origin, active GraphicsLayer reference,
 * and the snapshot bitmap of the previous theme.
 */
class ThemeTransitionState {
    /** Current reveal progress: 0f = old theme fully visible, 1f = new theme fully revealed. */
    var progress by mutableFloatStateOf(1f)
        internal set

    /** Origin point (in px) from which the circular hole expands. Null = screen center fallback. */
    var origin by mutableStateOf<RevealOrigin?>(null)
        internal set

    /** True while an animation is in progress — blocks concurrent triggers. */
    var isAnimating by mutableStateOf(false)
        internal set

    /** Snapshot image of the old theme before switching, drawn with expanding circular cutout. */
    var oldBitmap by mutableStateOf<ImageBitmap?>(null)

    /** Reference to the active GraphicsLayer for capturing the composable snapshot. */
    var graphicsLayer: GraphicsLayer? = null
}

/**
 * Wraps application content with a smooth circular reveal overlay when the theme is toggled.
 *
 * How it works:
 * 1. The live application content renders normally with the active Theme (New Theme) and records into [graphicsLayer].
 * 2. While [state.isAnimating] is true, an overlay draws the snapshot of the Old Theme
 *    ([state.oldBitmap]) with an expanding circular cutout centered at [state.origin].
 * 3. As [state.progress] animates from 0f to 1f, the circular hole grows, revealing the New Theme.
 * 4. When complete, the overlay is dismissed and memory is freed immediately.
 *
 * @param state           The [ThemeTransitionState] controlling this transition.
 * @param skipAnimation   When true, transitions are instant (respects reducedMotion setting).
 * @param modifier        Modifier applied to the container Box.
 * @param content         Composable lambda rendering the active application UI.
 */
@Composable
fun CircularRevealThemeWrapper(
    state: ThemeTransitionState,
    skipAnimation: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val isAnimating = state.isAnimating && !skipAnimation
    val progress = state.progress
    val origin = state.origin
    val oldBitmap = state.oldBitmap
    val graphicsLayer = rememberGraphicsLayer()
    state.graphicsLayer = graphicsLayer

    LaunchedEffect(isAnimating) {
        if (isAnimating) {
            Log.d(
                THEME_TRANSITION_TAG,
                "[COMPOSITION] Reveal overlay ACTIVE: Drawing old theme snapshot with expanding circular mask on top of live new theme."
            )
        } else {
            Log.d(
                THEME_TRANSITION_TAG,
                "[COMPOSITION] Reveal overlay IDLE: Live theme rendering directly (no overlay overhead)."
            )
        }
    }

    Box(modifier = modifier) {
        // Base Layer: Live app with the active theme, recorded into GraphicsLayer
        Box(
            modifier = Modifier.drawWithContent {
                graphicsLayer.record {
                    this@drawWithContent.drawContent()
                }
                drawLayer(graphicsLayer)
            }
        ) {
            content()
        }

        // Overlay Layer: Snapshot of old theme with expanding circular hole revealing new theme
        if (isAnimating && oldBitmap != null) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val o = origin ?: RevealOrigin(size.width / 2f, size.height / 2f)
                val maxRadius = hypot(
                    max(o.x, size.width - o.x).toDouble(),
                    max(o.y, size.height - o.y).toDouble()
                ).toFloat().coerceAtLeast(100f)
                val currentRadius = maxRadius * progress

                // Path that covers the full screen MINUS the expanding circle at (o.x, o.y)
                val clipPath = Path().apply {
                    fillType = PathFillType.EvenOdd
                    addRect(Rect(0f, 0f, size.width, size.height))
                    addOval(
                        Rect(
                            center = Offset(o.x, o.y),
                            radius = currentRadius
                        )
                    )
                }

                clipPath(clipPath) {
                    drawImage(
                        image = oldBitmap,
                        dstSize = IntSize(size.width.toInt(), size.height.toInt())
                    )
                }
            }
        }
    }
}

/**
 * Launches a circular reveal animation using the provided [ThemeTransitionState].
 * Logs each step with the [THEME_TRANSITION_TAG] tag for logcat inspection.
 *
 * @param state         The transition state to animate.
 * @param origin        Optional pixel offset of the toggle button (for directional reveal).
 * @param skipAnimation If true, completes the transition instantly without animation.
 * @param animSpec      Animation spec controlling duration and easing. Defaults to 650ms cubic ease-out.
 * @param onStart       Called when animation starts — switches the theme in ViewModel.
 */
suspend fun animateThemeReveal(
    state: ThemeTransitionState,
    origin: RevealOrigin?,
    skipAnimation: Boolean,
    animSpec: AnimationSpec<Float> = tween(
        durationMillis = 650,
        easing = { t ->
            val p = t - 1f
            p * p * p + 1f
        }
    ),
    onStart: () -> Unit
) {
    val originStr = origin?.let { "(${it.x.toInt()}px, ${it.y.toInt()}px)" } ?: "ScreenCenter(fallback)"
    Log.d(
        THEME_TRANSITION_TAG,
        "[STEP 1/5: REQUEST] animateThemeReveal invoked. Origin: $originStr, skipAnimation: $skipAnimation, isAlreadyAnimating: ${state.isAnimating}"
    )

    if (state.isAnimating) {
        Log.w(
            THEME_TRANSITION_TAG,
            "[BLOCKED] Theme toggle request ignored because an animation is already running."
        )
        return
    }

    if (skipAnimation || state.oldBitmap == null) {
        Log.i(
            THEME_TRANSITION_TAG,
            "[STEP 2/5: SKIP] Reduced motion active or snapshot unavailable. Executing instant theme toggle."
        )
        onStart()
        state.oldBitmap = null
        state.isAnimating = false
        Log.d(THEME_TRANSITION_TAG, "[STEP 5/5: COMPLETE] Instant theme change finished.")
        return
    }

    Log.d(
        THEME_TRANSITION_TAG,
        "[STEP 2/5: INIT] Initializing circular reveal state (progress=0.0, isAnimating=true, origin=$originStr)."
    )
    state.isAnimating = true
    state.origin = origin
    state.progress = 0f

    Log.d(
        THEME_TRANSITION_TAG,
        "[STEP 3/5: STATE_CHANGE] Calling onStart() callback to switch theme in ViewModel..."
    )
    onStart()
    kotlinx.coroutines.delay(32)
    Log.d(
        THEME_TRANSITION_TAG,
        "[STEP 3/5: STATE_CHANGE] onStart() completed and first frame synced. Beginning Animatable interpolation from 0.0 to 1.0 (duration=650ms)..."
    )

    var lastLoggedStep = -1
    val anim = Animatable(0f)
    try {
        anim.animateTo(targetValue = 1f, animationSpec = animSpec) {
            state.progress = value
            val step = (value * 5).toInt() // Checkpoints at 0%, 20%, 40%, 60%, 80%, 100%
            if (step > lastLoggedStep) {
                lastLoggedStep = step
                val percent = (value * 100).toInt()
                Log.d(
                    THEME_TRANSITION_TAG,
                    "[STEP 4/5: ANIMATING] Reveal progress: $percent% (progress=${"%.3f".format(value)})"
                )
            }
        }
    } catch (e: Exception) {
        Log.e(THEME_TRANSITION_TAG, "[ERROR] Exception occurred during theme reveal animation", e)
    } finally {
        Log.d(
            THEME_TRANSITION_TAG,
            "[STEP 5/5: COMPLETE] Circular reveal animation finished. Resetting state: progress=1.0, isAnimating=false, clearing oldBitmap."
        )
        state.progress = 1f
        state.isAnimating = false
        state.oldBitmap = null
    }
}
