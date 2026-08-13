package com.philornot.slownikjezykatrudnego

import com.philornot.slownikjezykatrudnego.data.datasource.DictionaryWordsData
import com.philornot.slownikjezykatrudnego.data.model.ReviewHistoryItem
import com.philornot.slownikjezykatrudnego.data.model.UserSettings
import com.philornot.slownikjezykatrudnego.data.model.UserWordProgress
import com.philornot.slownikjezykatrudnego.domain.SessionManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests verifying daily session creation and adaptive throttling.
 */
class SessionManagerTest {

    @Test
    fun testAdaptiveThrottling_withManyUnmasteredWords_blocksNewWords() {
        val today = "2026-08-09"
        val yesterday = "2026-08-08"

        val unmasteredMap = (1..9).associate { idx ->
            "word$idx" to UserWordProgress(
                wordId = "word$idx",
                repetitions = 1,
                nextReviewDate = today,
                lastReviewedAt = yesterday,
                history = listOf(ReviewHistoryItem(yesterday, 3))
            )
        }

        val limit = SessionManager.calculateAdaptiveNewWordsLimit(
            progressMap = unmasteredMap,
            userConfiguredLimit = 5,
            todayStr = today
        )

        assertEquals(0, limit)
    }

    @Test
    fun testDailySession_withFreshState_createsNewWordsCards() {
        val sessionData = SessionManager.createDailySession(
            progressMap = emptyMap(),
            settings = UserSettings(dailyNewWordsLimit = 5),
            allWords = DictionaryWordsData.WORDS
        )

        assertEquals(5, sessionData.cards.size)
        assertTrue(sessionData.cards.all { it.isNew })
        assertTrue(sessionData.cards.all { it.options.size == 4 })
    }

    @Test
    fun testDailySession_withDefaultSettings_createsThreeCards() {
        val defaultSettings = UserSettings()
        assertEquals(3, defaultSettings.dailyNewWordsLimit)

        val sessionData = SessionManager.createDailySession(
            progressMap = emptyMap(),
            settings = defaultSettings,
            allWords = DictionaryWordsData.WORDS
        )

        assertEquals(3, sessionData.cards.size)
        assertTrue(sessionData.cards.all { it.isNew })
    }

    @Test
    fun testMulberry32_isDeterministic() {
        val rng1 = SessionManager.Mulberry32("test-seed-123")
        val rng2 = SessionManager.Mulberry32("test-seed-123")

        val val1 = rng1.nextFloat()
        val val2 = rng2.nextFloat()

        assertEquals(val1, val2, 0.00001f)
    }

    @Test
    fun testGetDailyCompletionMessage_returnsOneOfTheDefinedVariants() {
        val result = SessionManager.getDailyCompletionMessage()

        assertTrue(result in SessionManager.CompletionMessageType.entries)
    }
}
