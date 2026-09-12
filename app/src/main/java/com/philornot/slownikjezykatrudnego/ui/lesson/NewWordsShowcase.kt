package com.philornot.slownikjezykatrudnego.ui.lesson

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.focusable
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import com.philornot.slownikjezykatrudnego.data.model.DictionaryWord
import com.philornot.slownikjezykatrudnego.ui.components.BadgeVariant
import com.philornot.slownikjezykatrudnego.ui.components.SjtBadge
import com.philornot.slownikjezykatrudnego.ui.components.SjtCard
import com.philornot.slownikjezykatrudnego.ui.components.SjtTouchButton
import com.philornot.slownikjezykatrudnego.ui.theme.LocalUserSettings
import com.philornot.slownikjezykatrudnego.ui.theme.SjtTheme

/**
 * Phase 1: Interactive showcase introducing new vocabulary with tap-to-reveal definitions,
 * full physical keyboard support (Space/Enter/Right Arrow), and E-Ink mode compatibility.
 */
@Composable
fun NewWordsShowcase(
    words: List<DictionaryWord>,
    onFinishShowcase: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (words.isEmpty()) return

    var currentIndex by remember { mutableIntStateOf(0) }
    var isRevealed by remember { mutableStateOf(false) }

    val currentWord = words[currentIndex]
    val colors = SjtTheme.colors
    val userSettings = LocalUserSettings.current
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val focusRequester = remember { FocusRequester() }

    fun handleNext() {
        if (!isRevealed) {
            isRevealed = true
            return
        }
        if (currentIndex + 1 < words.size) {
            currentIndex++
            isRevealed = false
        } else {
            onFinishShowcase()
        }
    }

    LaunchedEffect(currentIndex) {
        try {
            focusRequester.requestFocus()
        } catch (_: Exception) {}
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { keyEvent ->
                if (keyEvent.type != KeyEventType.KeyDown) return@onKeyEvent false
                when (keyEvent.key) {
                    Key.Spacebar, Key.Enter -> {
                        handleNext()
                        true
                    }
                    Key.DirectionRight -> {
                        if (isRevealed) {
                            handleNext()
                            true
                        } else false
                    }
                    else -> false
                }
            }
    ) {
        // Main Showcase Card
        SjtCard(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // Card Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.bgSurfaceElevated)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = colors.brandPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "NOWE (${currentIndex + 1}/${words.size})",
                        color = colors.textPrimary,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                }

                // Mini Progress Pills
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    words.forEachIndexed { idx, _ ->
                        val pillWidth = if (idx == currentIndex) 24.dp else 12.dp
                        val pillColor = when {
                            idx == currentIndex -> colors.brandPrimary
                            idx < currentIndex -> colors.brandPrimaryHover.copy(alpha = 0.7f)
                            else -> colors.progressTrack
                        }
                        Box(
                            modifier = Modifier
                                .size(width = pillWidth, height = 6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(pillColor)
                        )
                    }
                }
            }

            // Scrollable Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Word Title & Phonetic Pronunciation
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = currentWord.word,
                        style = MaterialTheme.typography.headlineLarge,
                        color = colors.textSerifTitle,
                        textAlign = TextAlign.Center
                    )

                    if (!currentWord.phonetic.isNullOrBlank()) {
                        Text(
                            text = currentWord.phonetic,
                            fontSize = 13.sp,
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Bold,
                            color = colors.textMuted,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    SjtBadge(
                        text = "Kategoria: ${currentWord.category}",
                        variant = BadgeVariant.NEUTRAL
                    )
                }

                // Definition Card (Tap to Reveal)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "DEFINICJA I ZNACZENIE",
                            color = colors.brandPrimary,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        )

                        if (!isRevealed) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = colors.brandPrimary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "Stuknij, aby odsłonić",
                                    color = colors.brandPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Interactive Blur / Unblur Box
                    Surface(
                        onClick = { isRevealed = true },
                        shape = RoundedCornerShape(12.dp),
                        color = colors.bgSurfaceElevated,
                        border = BorderStroke(1.dp, if (isRevealed) colors.borderDefault else colors.brandPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 90.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .pointerHoverIcon(PointerIcon.Hand)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 18.dp, vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val isEInk = userSettings.eInkMode || SjtTheme.isEInk
                            val skipAnimations = SjtTheme.skipAnimations

                            val blurRadius by animateDpAsState(
                                targetValue = if (isRevealed) 0.dp else 16.dp,
                                animationSpec = if (skipAnimations) snap() else tween(durationMillis = 300),
                                label = "definitionBlur"
                            )
                            val textAlpha by animateFloatAsState(
                                targetValue = if (isRevealed) 1f else 0.5f,
                                animationSpec = if (skipAnimations) snap() else tween(durationMillis = 300),
                                label = "definitionAlpha"
                            )

                            Text(
                                text = currentWord.fullDefinition,
                                style = MaterialTheme.typography.bodyLarge,
                                color = colors.textPrimary,
                                lineHeight = 22.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .then(
                                        if (isEInk) {
                                            if (!isRevealed) Modifier.alpha(0f) else Modifier
                                        } else {
                                            Modifier
                                                .alpha(textAlpha)
                                                .then(
                                                    if (blurRadius > 0.dp) {
                                                        Modifier.blur(
                                                            radius = blurRadius,
                                                            edgeTreatment = BlurredEdgeTreatment.Unbounded
                                                        )
                                                    } else {
                                                        Modifier
                                                    }
                                                )
                                        }
                                    )
                            )

                            if (!isRevealed) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = colors.brandPrimary,
                                    shadowElevation = 2.dp
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Visibility,
                                            contentDescription = null,
                                            tint = colors.btnPrimaryText,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "Stuknij, aby odsłonić",
                                            color = colors.btnPrimaryText,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Etymology
                if (!currentWord.etymology.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = colors.bgSurfaceElevated,
                        border = BorderStroke(1.dp, colors.borderDefault),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "ETYMOLOGIA",
                                color = colors.textMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = currentWord.etymology,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.textSecondary
                            )
                        }
                    }
                }

                // Example Sentences (Blockquotes)
                if (currentWord.examples.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "PRZYKŁADY UŻYCIA",
                            color = colors.textMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        )

                        currentWord.examples.forEach { example ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = colors.blockquoteBg,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(IntrinsicSize.Min),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Left thick brand bar spanning full height
                                    Box(
                                        modifier = Modifier
                                            .width(4.dp)
                                            .fillMaxHeight()
                                            .background(colors.brandPrimary)
                                    )
                                    Text(
                                        text = "\"$example\"",
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colors.textPrimary,
                                        lineHeight = 20.sp,
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(10.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // PWN Dictionary External Link
                if (currentWord.sjpUrl.isNotBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .pointerHoverIcon(PointerIcon.Hand)
                            .clickable {
                                try {
                                    val intent =
                                        Intent(Intent.ACTION_VIEW, Uri.parse(currentWord.sjpUrl))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Zobacz w SJP PWN",
                            color = colors.textAmberBrand,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = "Otwórz SJP PWN",
                            tint = colors.textAmberBrand,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

                // Sticky Bottom Action Button with shortcut hints (only show keyboard hints if physical keyboard is connected)
        val hasPhysicalKeyboard = SjtTheme.hasPhysicalKeyboard
        val nextActionText = when {
            !isRevealed -> if (hasPhysicalKeyboard) "Odsłoń definicję [Spacja]" else "Odsłoń definicję"
            currentIndex + 1 < words.size -> if (hasPhysicalKeyboard) "Następne słowo [Spacja]" else "Następne słowo"
            else -> if (hasPhysicalKeyboard) "Przejdź do testu wiedzy [Enter]" else "Przejdź do testu wiedzy"
        }

        SjtTouchButton(
            text = nextActionText,
            enabled = true,
            onClick = { handleNext() },
            trailingIcon = {
                Icon(
                    imageVector = if (currentIndex + 1 < words.size) Icons.AutoMirrored.Filled.ArrowForward else Icons.Default.Check,
                    contentDescription = null,
                    tint = colors.btnPrimaryText,
                    modifier = Modifier.size(18.dp)
                )
            }
        )
    }
}
