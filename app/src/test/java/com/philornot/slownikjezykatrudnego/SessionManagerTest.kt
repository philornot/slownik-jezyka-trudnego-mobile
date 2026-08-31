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
    fun testAdaptiveThrottling_withNormalInProgressWords_respectsUserConfiguredLimit() {
        val today = "2026-08-09"
        val yesterday = "2026-08-08"

        // 9 words in progress from previous days (scheduled in future)
        val inProgressMap = (1..9).associate { idx ->
            "word$idx" to UserWordProgress(
                wordId = "word$idx",
                repetitions = 1,
                nextReviewDate = "2026-08-15",
                lastReviewedAt = yesterday,
                history = listOf(ReviewHistoryItem(yesterday, 4))
            )
        }

        val limit = SessionManager.calculateAdaptiveNewWordsLimit(
            progressMap = inProgressMap,
            userConfiguredLimit = 5,
            dueWordsCount = 0,
            todayStr = today
        )

        assertEquals("Normal in-progress words should not block configured new words", 5, limit)
    }

    @Test
    fun testAdaptiveThrottling_withExtremeBacklog_throttlesNewWords() {
        val today = "2026-08-09"

        val limit = SessionManager.calculateAdaptiveNewWordsLimit(
            progressMap = emptyMap(),
            userConfiguredLimit = 5,
            dueWordsCount = 25,
            todayStr = today
        )

        assertEquals("Extreme backlog of >20 due reviews should throttle new words to 1", 1, limit)
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

    private fun createTestProgress(
        wordId: String,
        repetitions: Int = 1,
        easeFactor: Double = 2.5,
        nextReviewDate: String = "2026-08-25",
        lastReviewedAt: String = "2026-08-20T10:00:00Z",
    ) = UserWordProgress(
        wordId = wordId,
        repetitions = repetitions,
        easeFactor = easeFactor,
        nextReviewDate = nextReviewDate,
        lastReviewedAt = lastReviewedAt
    )

    @Test
    fun testCreateExtraNewWordsSession_returnsRequestedCountOfUnstartedWords() {
        val words = DictionaryWordsData.WORDS
        val startedMap = mapOf(
            words[0].id to createTestProgress(wordId = words[0].id, repetitions = 1)
        )

        val extraCards = SessionManager.createExtraNewWordsSession(
            progressMap = startedMap,
            allWords = words,
            count = 4,
            seed = "test-extra"
        )

        assertEquals(4, extraCards.size)
        assertTrue(extraCards.all { it.isNew })
        assertTrue(extraCards.none { it.word.id == words[0].id })
    }

    @Test
    fun testCreateHardWordsPracticeSession_prioritizesLowestEaseFactor() {
        val words = DictionaryWordsData.WORDS
        check(words.size >= 3)

        val progressMap = mapOf(
            words[0].id to createTestProgress(
                wordId = words[0].id,
                easeFactor = 2.5,
                repetitions = 3
            ),
            words[1].id to createTestProgress(
                wordId = words[1].id,
                easeFactor = 1.3,
                repetitions = 1
            ),
            words[2].id to createTestProgress(
                wordId = words[2].id,
                easeFactor = 1.8,
                repetitions = 2
            ),
        )

        val practiceCards = SessionManager.createHardWordsPracticeSession(
            progressMap = progressMap,
            allWords = words,
            count = 2,
            seed = "test-practice"
        )

        assertEquals(2, practiceCards.size)
        assertEquals(
            "Hardest word (easeFactor 1.3) should be first",
            words[1].id,
            practiceCards[0].word.id
        )
        assertEquals(
            "Second hardest word (easeFactor 1.8) should be second",
            words[2].id,
            practiceCards[1].word.id
        )
        assertTrue(practiceCards.all { !it.isNew })
    }

    @Test
    fun testHasUnstartedWords_and_hasWordsToPractice() {
        val words = DictionaryWordsData.WORDS

        assertTrue(
            "Empty progress map has unstarted words",
            SessionManager.hasUnstartedWords(emptyMap(), words)
        )
        assertTrue(
            "Empty progress map has no words to practice",
            !SessionManager.hasWordsToPractice(emptyMap())
        )

        val allStarted = words.associate { it.id to createTestProgress(it.id) }
        assertTrue(
            "Full progress map has no unstarted words",
            !SessionManager.hasUnstartedWords(allStarted, words)
        )
        assertTrue(
            "Full progress map has words to practice",
            SessionManager.hasWordsToPractice(allStarted)
        )
    }

    @Test
    fun testDailyLessonLimit_calculatesRemainingLessonsCorrectly() {
        val today = "2026-08-09"
        val words = DictionaryWordsData.WORDS

        // No words started today -> 2 lessons remaining with limit 3 (max 2 * 3 = 6 words)
        val remaining0 = SessionManager.getRemainingNewLessonsToday(
            progressMap = emptyMap(),
            dailyNewWordsLimit = 3,
            maxLessons = 2,
            todayStr = today
        )
        assertEquals(2, remaining0)
        assertTrue(SessionManager.canStartNewLessonToday(emptyMap(), 3, words, 2, today))

        // 3 words started today -> 1 lesson remaining
        val started3 = (1..3).associate { idx ->
            "w$idx" to createTestProgress(
                wordId = "w$idx",
                repetitions = 1,
                lastReviewedAt = today,
                nextReviewDate = "2026-08-10"
            ).copy(history = listOf(ReviewHistoryItem(today, 4)))
        }
        val remaining1 = SessionManager.getRemainingNewLessonsToday(
            progressMap = started3,
            dailyNewWordsLimit = 3,
            maxLessons = 2,
            todayStr = today
        )
        assertEquals(1, remaining1)
        assertTrue(SessionManager.canStartNewLessonToday(started3, 3, words, 2, today))

        // 6 words started today -> 0 lessons remaining
        val started6 = (1..6).associate { idx ->
            "w$idx" to createTestProgress(
                wordId = "w$idx",
                repetitions = 1,
                lastReviewedAt = today,
                nextReviewDate = "2026-08-10"
            ).copy(history = listOf(ReviewHistoryItem(today, 4)))
        }
        val remaining2 = SessionManager.getRemainingNewLessonsToday(
            progressMap = started6,
            dailyNewWordsLimit = 3,
            maxLessons = 2,
            todayStr = today
        )
        assertEquals(0, remaining2)
        assertTrue(!SessionManager.canStartNewLessonToday(started6, 3, words, 2, today))
    }

    @Test
    fun testCreateQuickPracticeSession_returnsLearnedWordsForPractice() {
        val words = DictionaryWordsData.WORDS
        val startedMap = mapOf(
            words[0].id to createTestProgress(wordId = words[0].id),
            words[1].id to createTestProgress(wordId = words[1].id),
            words[2].id to createTestProgress(wordId = words[2].id)
        )

        val quickCards = SessionManager.createQuickPracticeSession(
            progressMap = startedMap,
            allWords = words,
            count = 2,
            seed = "test-quick"
        )

        assertEquals(2, quickCards.size)
        assertTrue(quickCards.all { !it.isNew })
        assertTrue(quickCards.all { it.word.id in startedMap.keys })
        assertTrue(quickCards.all { it.options.size == 4 })
    }
}
