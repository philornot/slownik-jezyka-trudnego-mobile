package com.philornot.slownikjezykatrudnego.widget

import com.philornot.slownikjezykatrudnego.data.model.ReviewHistoryItem
import com.philornot.slownikjezykatrudnego.data.model.UserWordProgress
import com.philornot.slownikjezykatrudnego.domain.SuperMemoEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StreakWidgetDataTest {

    @Test
    fun fromProgress_emptyMap_returnsZeroStreakAndNotCompleted() {
        val data = StreakWidgetData.fromProgress(emptyMap())

        assertEquals(0, data.streakDays)
        assertFalse(data.isCompletedToday)
        assertEquals(7, data.weekDays.size)
        assertEquals(listOf("Pn", "Wt", "Śr", "Cz", "Pt", "So", "Nd"), data.weekDays.map { it.dayName })
        assertTrue(data.weekDays.none { it.isCompleted })
    }

    @Test
    fun fromProgress_todayReviewed_returnsCompletedTodayAndCorrectStreak() {
        val todayStr = SuperMemoEngine.getTodayDateString()
        val yesterdayStr = SuperMemoEngine.addDaysToDate(todayStr, -1)

        val progress = mapOf(
            "word_1" to UserWordProgress(
                wordId = "word_1",
                nextReviewDate = todayStr,
                lastReviewedAt = "2026-08-23T12:00:00.000Z",
                history = listOf(
                    ReviewHistoryItem(date = yesterdayStr, grade = 4),
                    ReviewHistoryItem(date = todayStr, grade = 5)
                )
            )
        )

        val data = StreakWidgetData.fromProgress(progress)

        assertEquals(2, data.streakDays)
        assertTrue(data.isCompletedToday)
        assertFalse(data.isUrgentEvening) // When completed, cannot be urgent evening

        val todayDayItem = data.weekDays.first { it.isToday }
        assertTrue(todayDayItem.isCompleted)
        assertEquals(todayStr, todayDayItem.dateString)
    }

    @Test
    fun fromProgress_weekDays_hasExactlyOneToday() {
        val data = StreakWidgetData.fromProgress(emptyMap())
        assertEquals(1, data.weekDays.count { it.isToday })
    }
}
