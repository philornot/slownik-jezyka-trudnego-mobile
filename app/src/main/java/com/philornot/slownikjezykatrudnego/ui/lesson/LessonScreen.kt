package com.philornot.slownikjezykatrudnego.ui.lesson

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.philornot.slownikjezykatrudnego.R
import com.philornot.slownikjezykatrudnego.data.model.DictionaryWord
import com.philornot.slownikjezykatrudnego.data.model.ReviewGrade
import com.philornot.slownikjezykatrudnego.data.model.SessionCard
import com.philornot.slownikjezykatrudnego.data.model.SessionPhase
import com.philornot.slownikjezykatrudnego.domain.SessionManager
import com.philornot.slownikjezykatrudnego.ui.theme.SjtTheme

/**
 * Main Lesson Screen orchestrating Phase 1 (Showcase), Phase 2 (Quiz), and
 * Summary.
 *
 * @param isBonusSession When true, the current session is a voluntary
 *    extra review and a "Bonus" label is shown above the progress bar.
 */
@Composable
fun LessonScreen(
    sessionCompleted: Boolean,
    sessionPhase: SessionPhase,
    newWordsToLearn: List<DictionaryWord>,
    sessionCards: List<SessionCard>,
    currentCardIndex: Int,
    cardsReviewedCount: Int,
    streakDays: Int,
    completionMessage: SessionManager.CompletionMessageType,
    isBonusSession: Boolean,
    newWordsBatchSize: Int = 3,
    canStartNewLessonToday: Boolean = true,
    remainingNewLessonsToday: Int = 2,
    hasUnstartedWords: Boolean = true,
    hasWordsToPractice: Boolean = true,
    onStartExtraLesson: () -> Unit = {},
    onStartReviewPractice: () -> Unit = {},
    onStartQuickPractice: () -> Unit = {},
    onFinishShowcase: () -> Unit,
    onGradeCard: (ReviewGrade) -> Unit,
    onNavigateCatalog: () -> Unit,
    onNavigateStats: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SjtTheme.colors

    Box(modifier = modifier.fillMaxSize()) {
        if (!sessionCompleted) {
            if (sessionPhase == SessionPhase.SHOWCASE && newWordsToLearn.isNotEmpty()) {
                NewWordsShowcase(
                    words = newWordsToLearn,
                    onFinishShowcase = onFinishShowcase
                )
            } else if (sessionCards.isNotEmpty() && currentCardIndex in sessionCards.indices) {
                val currentCard = sessionCards[currentCardIndex]

                Column(modifier = Modifier.fillMaxSize()) {
                    // Top Progress Bar for Phase 2 Quiz
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        // Bonus session label chip
                        if (isBonusSession) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = colors.textAmberBrand.copy(alpha = 0.15f),
                                modifier = Modifier.padding(bottom = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(
                                        horizontal = 10.dp,
                                        vertical = 3.dp
                                    ),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = colors.textAmberBrand,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = stringResource(R.string.bonus_session_label),
                                        color = colors.textAmberBrand,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Progress bar with fraction and percentage
                        val totalCards = sessionCards.size
                        val currentCardNum = (currentCardIndex + 1).coerceAtMost(totalCards)
                        val progressFraction =
                            if (totalCards > 0) currentCardNum.toFloat() / totalCards.toFloat() else 0f

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Faza 2 · $currentCardNum/$totalCards",
                                color = colors.brandPrimary,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.ExtraBold
                            )

                            LinearProgressIndicator(
                                progress = { progressFraction },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 10.dp)
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = colors.brandPrimary,
                                trackColor = colors.progressTrack
                            )

                            Text(
                                text = "${(progressFraction * 100).toInt()}%",
                                color = colors.textMuted,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    key(currentCardIndex) {
                        HybridQuizCard(
                            card = currentCard,
                            onGrade = onGradeCard,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        } else {
            SessionSummaryScreen(
                cardsReviewedCount = cardsReviewedCount,
                streakDays = streakDays,
                completionMessage = completionMessage,
                isBonusSession = isBonusSession,
                newWordsBatchSize = newWordsBatchSize,
                canStartNewLessonToday = canStartNewLessonToday,
                remainingNewLessonsToday = remainingNewLessonsToday,
                hasUnstartedWords = hasUnstartedWords,
                hasWordsToPractice = hasWordsToPractice,
                onStartExtraLesson = onStartExtraLesson,
                onStartReviewPractice = onStartReviewPractice,
                onStartQuickPractice = onStartQuickPractice,
                onNavigateCatalog = onNavigateCatalog,
                onNavigateStats = onNavigateStats
            )
        }
    }
}
