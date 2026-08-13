package com.philornot.slownikjezykatrudnego.ui.lesson

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.philornot.slownikjezykatrudnego.data.model.DictionaryWord
import com.philornot.slownikjezykatrudnego.data.model.ReviewGrade
import com.philornot.slownikjezykatrudnego.data.model.SessionCard
import com.philornot.slownikjezykatrudnego.data.model.SessionPhase
import com.philornot.slownikjezykatrudnego.domain.SessionManager
import com.philornot.slownikjezykatrudnego.ui.theme.SjtTheme

/**
 * Main Lesson Screen orchestrating Phase 1 (Showcase), Phase 2 (Quiz), and Summary.
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Faza 2 · ${currentCardIndex + 1}/${sessionCards.size}",
                                color = colors.brandPrimary,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.ExtraBold
                            )

                            val progressFraction = ((currentCardIndex + 1).toFloat() / sessionCards.size.toFloat()).coerceIn(0f, 1f)

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

                    HybridQuizCard(
                        card = currentCard,
                        onGrade = onGradeCard,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        } else {
            SessionSummaryScreen(
                cardsReviewedCount = cardsReviewedCount,
                streakDays = streakDays,
                completionMessage = completionMessage,
                onNavigateCatalog = onNavigateCatalog,
                onNavigateStats = onNavigateStats
            )
        }
    }
}
