package com.philornot.slownikjezykatrudnego.ui.lesson

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.philornot.slownikjezykatrudnego.R
import com.philornot.slownikjezykatrudnego.domain.SessionManager
import com.philornot.slownikjezykatrudnego.ui.components.SjtCard
import com.philornot.slownikjezykatrudnego.ui.components.SjtSecondaryButton
import com.philornot.slownikjezykatrudnego.ui.components.SjtTouchButton
import com.philornot.slownikjezykatrudnego.ui.theme.SjtTheme

/**
 * Session completion summary screen with trophy, study metrics, and
 * navigation actions including on-demand learning.
 *
 * @param isBonusSession When true, shows bonus-session-specific copy and
 *    icon instead of the regular completion message.
 * @param newWordsBatchSize The batch size of new words configured by the
 *    user.
 * @param canStartNewLessonToday True if daily limit of new-word lessons is not yet reached.
 * @param remainingNewLessonsToday Number of remaining new-word lessons today.
 * @param maxDailyNewLessons Maximum recommended lessons per day.
 * @param hasUnstartedWords True if there are remaining unstarted words in
 *    dictionary.
 * @param hasWordsToPractice True if user has any words in progress to
 *    practice.
 * @param onStartExtraLesson Callback to start an extra lesson immediately.
 * @param onStartReviewPractice Callback to start a practice session for
 *    difficult words.
 * @param onStartQuickPractice Callback to start a quick general practice session.
 */
@Composable
fun SessionSummaryScreen(
    cardsReviewedCount: Int,
    streakDays: Int,
    completionMessage: SessionManager.CompletionMessageType,
    isBonusSession: Boolean,
    newWordsBatchSize: Int = 3,
    canStartNewLessonToday: Boolean = true,
    remainingNewLessonsToday: Int = 2,
    maxDailyNewLessons: Int = SessionManager.MAX_DAILY_NEW_LESSONS,
    hasUnstartedWords: Boolean = true,
    hasWordsToPractice: Boolean = true,
    onStartExtraLesson: () -> Unit = {},
    onStartReviewPractice: () -> Unit = {},
    onStartQuickPractice: () -> Unit = {},
    onNavigateCatalog: () -> Unit,
    onNavigateStats: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SjtTheme.colors
    val scrollState = rememberScrollState()

    val (titleRes, descriptionRes) = remember(completionMessage) {
        completionMessage.toStringRes()
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SjtCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Trophy / Bonus Icon Box
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(colors.brandPrimary, colors.brandPrimaryHover)
                            ),
                            shape = RoundedCornerShape(22.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isBonusSession) Icons.Default.AutoAwesome else Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(44.dp)
                    )
                }

                // Title & Description
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(titleRes),
                        style = MaterialTheme.typography.headlineLarge,
                        color = colors.textSerifTitle,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(descriptionRes),
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textMuted,
                        textAlign = TextAlign.Center,
                        lineHeight = 19.sp
                    )
                }

                // SRS Educational / Daily Limit Banner
                if (canStartNewLessonToday) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = colors.badgeEmeraldBg.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, colors.badgeEmeraldBorder.copy(alpha = 0.7f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = colors.brandPrimary.copy(alpha = 0.2f),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = colors.brandPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Text(
                                text = stringResource(R.string.daily_lesson_available_banner),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.textPrimary,
                                lineHeight = 16.sp
                            )
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = colors.badgeAmberBg,
                        border = BorderStroke(1.dp, colors.badgeAmberBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = colors.brandPrimary.copy(alpha = 0.2f),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Psychology,
                                        contentDescription = null,
                                        tint = colors.brandPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.daily_limit_reached_title),
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = stringResource(R.string.daily_limit_reached_description),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = colors.textMuted,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }

                // Metrics Row
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = colors.bgSurfaceElevated,
                    border = BorderStroke(1.dp, colors.borderDefault),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "PRZEJRZANE SŁOWA",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = colors.textMuted,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "$cardsReviewedCount",
                                style = MaterialTheme.typography.displayLarge,
                                color = colors.textAmberBrand
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(width = 1.dp, height = 48.dp)
                                .background(colors.borderDefault)
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "SERIA NAUKI",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = colors.textMuted,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "$streakDays dni",
                                style = MaterialTheme.typography.displayLarge,
                                color = colors.textAmberBrand
                            )
                        }
                    }
                }

                // Action Buttons
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (canStartNewLessonToday) {
                        SjtTouchButton(
                            text = stringResource(
                                R.string.action_learn_more_words_with_counter,
                                newWordsBatchSize,
                                maxDailyNewLessons - remainingNewLessonsToday + 1,
                                maxDailyNewLessons
                            ),
                            onClick = onStartExtraLesson,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = colors.btnPrimaryText,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        )
                    } else if (hasWordsToPractice) {
                        SjtTouchButton(
                            text = stringResource(
                                R.string.action_quick_practice_with_count,
                                5
                            ),
                            onClick = onStartQuickPractice,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = colors.btnPrimaryText,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        )
                    }

                    if (hasWordsToPractice) {
                        if (canStartNewLessonToday) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                SjtSecondaryButton(
                                    text = stringResource(R.string.action_quick_practice),
                                    onClick = onStartQuickPractice,
                                    modifier = Modifier.weight(1f),
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Bolt,
                                            contentDescription = null,
                                            tint = colors.brandPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                )

                                SjtSecondaryButton(
                                    text = stringResource(R.string.action_practice_hard_words_short),
                                    onClick = onStartReviewPractice,
                                    modifier = Modifier.weight(1f),
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = null,
                                            tint = colors.brandPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                )
                            }
                        } else {
                            SjtSecondaryButton(
                                text = stringResource(R.string.action_practice_hard_words),
                                onClick = onStartReviewPractice,
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        tint = colors.brandPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SjtSecondaryButton(
                            text = "Słowniczek",
                            onClick = onNavigateCatalog,
                            modifier = Modifier.weight(1f),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.AutoStories,
                                    contentDescription = null,
                                    tint = colors.textPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )

                        SjtSecondaryButton(
                            text = "Statystyki",
                            onClick = onNavigateStats,
                            modifier = Modifier.weight(1f),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.BarChart,
                                    contentDescription = null,
                                    tint = colors.textPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Resolves a [SessionManager.CompletionMessageType] to its title and
 * description string resources.
 *
 * Keeping this mapping in the UI layer (rather than in `SessionManager`)
 * is deliberate: the domain layer only decides *which* message variant was
 * picked, not what text represents it.
 *
 * @return A pair of `(titleResId, descriptionResId)` to pass into
 *    [stringResource].
 * @receiver The completion message variant selected by
 *    [SessionManager.getDailyCompletionMessage].
 */
private fun SessionManager.CompletionMessageType.toStringRes(): Pair<Int, Int> = when (this) {
    SessionManager.CompletionMessageType.GREAT_JOB ->
        R.string.completion_great_job_title to R.string.completion_great_job_description

    SessionManager.CompletionMessageType.SESSION_DONE ->
        R.string.completion_session_done_title to R.string.completion_session_done_description

    SessionManager.CompletionMessageType.ERUDITION_GROWING ->
        R.string.completion_erudition_title to R.string.completion_erudition_description

    SessionManager.CompletionMessageType.VOCABULARY_MASTERED ->
        R.string.completion_vocabulary_mastered_title to R.string.completion_vocabulary_mastered_description

    SessionManager.CompletionMessageType.DAY_WELL_SPENT ->
        R.string.completion_day_well_spent_title to R.string.completion_day_well_spent_description

    SessionManager.CompletionMessageType.LEXICON_GROWS ->
        R.string.completion_lexicon_grows_title to R.string.completion_lexicon_grows_description

    SessionManager.CompletionMessageType.ONE_STEP_FURTHER ->
        R.string.completion_one_step_further_title to R.string.completion_one_step_further_description

    SessionManager.CompletionMessageType.BRAIN_TRAINED ->
        R.string.completion_brain_trained_title to R.string.completion_brain_trained_description

    SessionManager.CompletionMessageType.KNOWLEDGE_IS_POWER ->
        R.string.completion_knowledge_is_power_title to R.string.completion_knowledge_is_power_description

    SessionManager.CompletionMessageType.SHORT_SESSIONS_WIN ->
        R.string.completion_short_sessions_win_title to R.string.completion_short_sessions_win_description

    SessionManager.CompletionMessageType.BONUS_GREAT_INITIATIVE ->
        R.string.completion_bonus_great_initiative_title to R.string.completion_bonus_great_initiative_description

    SessionManager.CompletionMessageType.BONUS_BEYOND_THE_PLAN ->
        R.string.completion_bonus_beyond_the_plan_title to R.string.completion_bonus_beyond_the_plan_description

    SessionManager.CompletionMessageType.BONUS_EXTRA_DOSE ->
        R.string.completion_bonus_extra_dose_title to R.string.completion_bonus_extra_dose_description

    SessionManager.CompletionMessageType.BONUS_CONSISTENT ->
        R.string.completion_bonus_consistent_title to R.string.completion_bonus_consistent_description
}
