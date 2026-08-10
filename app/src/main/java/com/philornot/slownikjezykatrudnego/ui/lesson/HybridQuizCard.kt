package com.philornot.slownikjezykatrudnego.ui.lesson

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.philornot.slownikjezykatrudnego.data.model.ReviewGrade
import com.philornot.slownikjezykatrudnego.data.model.SessionCard
import com.philornot.slownikjezykatrudnego.ui.components.BadgeVariant
import com.philornot.slownikjezykatrudnego.ui.components.SjtBadge
import com.philornot.slownikjezykatrudnego.ui.components.SjtCard
import com.philornot.slownikjezykatrudnego.ui.theme.SjtTheme

/**
 * Phase 2: Hybrid Quiz card with active recall choice, full context
 * revelation, and thumb-friendly self-grading.
 *
 * Layout:
 * - Non-scrollable header: badges (category + new/review)
 * - Non-scrollable sticky word title + phonetic — always visible
 * - Scrollable content area: quiz options OR definition + examples
 * - Non-scrollable bottom bar: grade buttons (after answer)
 */
@Composable
fun HybridQuizCard(
    card: SessionCard,
    onGrade: (ReviewGrade) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedOption by remember(card.word.id) { mutableStateOf<String?>(null) }
    val isAnswered = selectedOption != null
    val isCorrect = selectedOption == card.word.shortDefinition

    val colors = SjtTheme.colors
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(card.word.id, isAnswered) {
        scrollState.scrollTo(0)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        // ─── Main Card Container ───
        SjtCard(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // Always fill available screen height
        ) {
            // ─── Non-scrollable Header: badges only ───
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.bgSurfaceElevated)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SjtBadge(
                    text = card.word.category,
                    variant = BadgeVariant.NEUTRAL
                )

                if (card.isNew) {
                    SjtBadge(
                        text = "Nowo poznane słówko",
                        variant = BadgeVariant.EMERALD,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = colors.badgeEmeraldText
                            )
                        }
                    )
                } else {
                    SjtBadge(
                        text = "Powtórka",
                        variant = BadgeVariant.AMBER,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = colors.badgeAmberText
                            )
                        }
                    )
                }
            }

            if (isAnswered) {
                // ─── ETAP 2: Context Revelation (Sticky Word Title + Scrollable Context) ───
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.bgSurface)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = card.word.word,
                        style = MaterialTheme.typography.headlineLarge,
                        color = colors.textSerifTitle,
                        textAlign = TextAlign.Center
                    )

                    if (!card.word.phonetic.isNullOrBlank()) {
                        Text(
                            text = card.word.phonetic,
                            fontSize = 13.sp,
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Bold,
                            color = colors.textMuted,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(scrollState)
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Feedback Banner
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isCorrect) colors.badgeEmeraldBg else colors.badgeRoseBg,
                        border = BorderStroke(
                            1.dp,
                            if (isCorrect) colors.badgeEmeraldBorder else colors.badgeRoseBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Close,
                                contentDescription = null,
                                tint = if (isCorrect) colors.badgeEmeraldText else colors.badgeRoseText,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = if (isCorrect) "Świetnie! Poprawna odpowiedź."
                                else "Twój wybór różni się od definicji.",
                                color = if (isCorrect) colors.badgeEmeraldText else colors.badgeRoseText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    // Full Definition Card
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = colors.bgSurfaceElevated,
                        border = BorderStroke(1.dp, colors.borderDefault),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "PEŁNA DEFINICJA",
                                color = colors.brandPrimary,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = card.word.fullDefinition,
                                style = MaterialTheme.typography.bodyLarge,
                                color = colors.textPrimary,
                                lineHeight = 22.sp
                            )

                            if (!card.word.etymology.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "ETYMOLOGIA",
                                    color = colors.textMuted,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = card.word.etymology,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.textSecondary
                                )
                            }
                        }
                    }

                    // Example Sentences
                    if (card.word.examples.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "PRZYKŁADY UŻYCIA W ZDANIACH",
                                color = colors.textMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp
                            )

                            card.word.examples.forEach { example ->
                                Surface(
                                    shape = RoundedCornerShape(topEnd = 10.dp, bottomEnd = 10.dp),
                                    color = colors.blockquoteBg,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        Box(
                                            modifier = Modifier
                                                .width(4.dp)
                                                .heightIn(min = 36.dp)
                                                .background(colors.brandPrimary)
                                        )
                                        Text(
                                            text = "\"$example\"",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = colors.textPrimary,
                                            lineHeight = 19.sp,
                                            modifier = Modifier.padding(10.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // SJP Link
                    if (card.word.sjpUrl.isNotBlank()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    try {
                                        val intent = Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse(card.word.sjpUrl)
                                        )
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                                .padding(vertical = 2.dp),
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
            } else {
                // ─── ETAP 1: Active Recall Quiz Options (Harmonious 2-Zone Layout) ───
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Strefa 1: Górna część - Wyeksponowane słowo i fonetyka
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = card.word.word,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = MaterialTheme.typography.headlineLarge.fontFamily,
                            color = colors.textSerifTitle,
                            textAlign = TextAlign.Center,
                            lineHeight = 38.sp
                        )

                        if (!card.word.phonetic.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = colors.bgSurfaceElevated,
                                border = BorderStroke(1.dp, colors.borderDefault)
                            ) {
                                Text(
                                    text = card.word.phonetic,
                                    fontSize = 13.5.sp,
                                    fontStyle = FontStyle.Italic,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.textMuted,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "WYBIERZ WŁAŚCIWE ZNACZENIE",
                            color = colors.brandPrimary,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center,
                            letterSpacing = 0.8.sp
                        )
                    }

                    // Strefa 2: Dolna część - Wygodne, duże przyciski opcji w strefie kciuka
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        card.options.forEachIndexed { index, option ->
                            val letter = ('A' + index).toString()

                            Surface(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    selectedOption = option
                                },
                                shape = RoundedCornerShape(14.dp),
                                color = colors.bgSurfaceElevated,
                                border = BorderStroke(1.dp, colors.borderDefault),
                                shadowElevation = 1.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(9.dp),
                                        color = colors.bgSurface,
                                        border = BorderStroke(1.dp, colors.borderDefault),
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = letter,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = colors.brandPrimary
                                            )
                                        }
                                    }

                                    Text(
                                        text = option,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontSize = 14.5.sp,
                                        color = colors.textPrimary,
                                        lineHeight = 20.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ─── ETAP 3: Sticky Grade Buttons — below the card, always at bottom ───
        if (isAnswered) {
            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = colors.bgSurfaceElevated,
                border = BorderStroke(1.dp, colors.borderDefault),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Oceń, jak dobrze pamiętasz to słówko",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.textPrimary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Bardzo słabo (0)
                        Surface(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onGrade(ReviewGrade.AGAIN)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(72.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = colors.grade0Bg,
                            border = BorderStroke(1.dp, colors.grade0Border)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp, horizontal = 2.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null,
                                    tint = colors.grade0Text,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Bardzo słabo",
                                    color = colors.grade0Text,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        }

                        // Słabo (3)
                        Surface(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onGrade(ReviewGrade.HARD)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(72.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = colors.grade3Bg,
                            border = BorderStroke(1.dp, colors.grade3Border)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp, horizontal = 2.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RemoveCircle,
                                    contentDescription = null,
                                    tint = colors.grade3Text,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Słabo",
                                    color = colors.grade3Text,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        }

                        // Dobrze (4)
                        Surface(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onGrade(ReviewGrade.GOOD)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(72.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = colors.grade4Bg,
                            border = BorderStroke(1.dp, colors.grade4Border)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp, horizontal = 2.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = colors.grade4Text,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Dobrze",
                                    color = colors.grade4Text,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        }

                        // Bardzo dobrze (5)
                        Surface(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onGrade(ReviewGrade.EASY)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(72.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = colors.grade5Bg,
                            border = BorderStroke(1.dp, colors.grade5Border)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp, horizontal = 2.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = colors.grade5Text,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Bardzo dobrze",
                                    color = colors.grade5Text,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
