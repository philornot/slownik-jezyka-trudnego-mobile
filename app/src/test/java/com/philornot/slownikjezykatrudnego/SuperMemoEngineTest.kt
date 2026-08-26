package com.philornot.slownikjezykatrudnego

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import com.philornot.slownikjezykatrudnego.data.model.ReviewGrade
import com.philornot.slownikjezykatrudnego.data.model.ReviewHistoryItem
import com.philornot.slownikjezykatrudnego.data.model.UserWordProgress
import com.philornot.slownikjezykatrudnego.domain.SuperMemoEngine

/**
 * Unit tests verifying SuperMemo SM-2 calculation and streak logic.
 */
class SuperMemoEngineTest {

    @Test
    fun testFirstSuccessfulReview_createsStandardInterval() {
        val result = SuperMemoEngine.calculateSM2(
            wordId = "imponderabilia",
            grade = ReviewGrade.GOOD
        )

        assertEquals("imponderabilia", result.wordId)
        assertEquals(1, result.repetitions)
        assertEquals(1, result.interval)
        assertEquals(1, result.history.size)
        assertEquals(4, result.history.first().grade)
        assertTrue(result.easeFactor >= 2.5)
    }

    @Test
    fun testSecondSuccessfulReview_createsSixDayInterval() {
        val firstProgress = SuperMemoEngine.calculateSM2(
            wordId = "imponderabilia",
            grade = ReviewGrade.GOOD
        )

        val secondProgress = SuperMemoEngine.calculateSM2(
            wordId = "imponderabilia",
            grade = ReviewGrade.EASY,
            currentProgress = firstProgress
        )

        assertEquals(2, secondProgress.repetitions)
        assertEquals(6, secondProgress.interval)
        assertEquals(2, secondProgress.history.size)
    }

    @Test
    fun testFailedReview_resetsIntervalAndRepetitions() {
        val masteredProgress = UserWordProgress(
            wordId = "abnegat",
            repetitions = 4,
            easeFactor = 2.6,
            interval = 24,
            nextReviewDate = "2026-08-09",
            lastReviewedAt = "2026-07-15T12:00:00.000Z"
        )

        val failedResult = SuperMemoEngine.calculateSM2(
            wordId = "abnegat",
            grade = ReviewGrade.AGAIN,
            currentProgress = masteredProgress
        )

        assertEquals(0, failedResult.repetitions)
        assertEquals(1, failedResult.interval)
    }

    @Test
    fun testRepeatedFailedReviewsInSameSession_doNotInflateHistory() {
        var progress = SuperMemoEngine.calculateSM2(
            wordId = "abnegat",
            grade = ReviewGrade.AGAIN
        )
        assertEquals(1, progress.history.size)
        assertEquals(0, progress.history.first().grade)

        // Repeat AGAIN 5 times in the same session
        repeat(5) {
            progress = SuperMemoEngine.calculateSM2(
                wordId = "abnegat",
                grade = ReviewGrade.AGAIN,
                currentProgress = progress
            )
        }

        // History must still have exactly 1 entry for today
        assertEquals(1, progress.history.size)
        assertEquals(0, progress.history.first().grade)

        // Finally pass the card in the same session
        progress = SuperMemoEngine.calculateSM2(
            wordId = "abnegat",
            grade = ReviewGrade.GOOD,
            currentProgress = progress
        )

        assertEquals(1, progress.repetitions)
        assertEquals(1, progress.history.size)
        assertEquals(4, progress.history.first().grade)
    }

    @Test
    fun testStreakCalculation_withConsecutiveDays() {
        val today = SuperMemoEngine.getTodayDateString()
        val yesterday = SuperMemoEngine.addDaysToDate(today, -1)
        val twoDaysAgo = SuperMemoEngine.addDaysToDate(today, -2)

        val progressMap = mapOf(
            "w1" to UserWordProgress(
                wordId = "w1",
                nextReviewDate = today,
                lastReviewedAt = today,
                history = listOf(
                    ReviewHistoryItem(twoDaysAgo, 4),
                    ReviewHistoryItem(yesterday, 5),
                    ReviewHistoryItem(today, 4)
                )
            )
        )

        val streak = SuperMemoEngine.calculateStreak(progressMap)
        assertEquals(3, streak)
    }
}
