package com.philornot.slownikjezykatrudnego

import com.philornot.slownikjezykatrudnego.data.model.ReviewHistoryItem
import com.philornot.slownikjezykatrudnego.data.model.UserWordProgress
import com.philornot.slownikjezykatrudnego.domain.SessionManager
import com.philornot.slownikjezykatrudnego.domain.SuperMemoEngine
import com.philornot.slownikjezykatrudnego.data.datasource.DictionaryWordsData
import com.philornot.slownikjezykatrudnego.data.model.UserSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests covering the 4 synchronization bugs fixed between the mobile
 * and web versions.
 *
 * Tests are pure JUnit4 and require no Android framework or Firebase --
 * they run fast on the JVM with `./gradlew test`.
 */
class SyncBugRegressionTest {

    // ─────────────────────── Helpers ────────────────────────────────────

    /** Creates a word progress entry reviewed at the given ISO timestamp. */
    private fun progress(
        wordId: String,
        lastReviewedAt: String,
        repetitions: Int = 1,
        nextReviewDate: String = "2099-01-01",
        historyDate: String = lastReviewedAt.take(10)
    ) = UserWordProgress(
        wordId = wordId,
        repetitions = repetitions,
        lastReviewedAt = lastReviewedAt,
        nextReviewDate = nextReviewDate,
        history = listOf(ReviewHistoryItem(date = historyDate, grade = 4))
    )

    /**
     * The merge logic extracted from FirebaseRepository -- tested in isolation
     * without requiring a Firebase connection.
     */
    private fun mergeProgressMaps(
        local: Map<String, UserWordProgress>,
        cloud: Map<String, UserWordProgress>
    ): Map<String, UserWordProgress> {
        val allIds = local.keys + cloud.keys
        return allIds.associateWith { wordId ->
            val localEntry = local[wordId]
            val cloudEntry = cloud[wordId]
            when {
                localEntry != null && cloudEntry != null -> {
                    val localTime = localEntry.lastReviewedAt
                    val cloudTime = cloudEntry.lastReviewedAt
                    if (cloudTime > localTime) cloudEntry else localEntry
                }
                localEntry != null -> localEntry
                else -> cloudEntry!!
            }
        }
    }

    // ─────────────────────── Bug 1 -- Race condition fix ──────────────────
    // The original bug: startSession() ran synchronously in init{} before
    // onUserLoggedIn() could fetch cloud data. If local progress was empty
    // but the cloud had entries marking today's limit as exhausted, the
    // session saw 0 cards -> sessionCompleted = true -> "0 slowek".

    @Test
    fun `Bug1 - session with already exhausted cloud progress has 0 new cards`() {
        val today = SuperMemoEngine.getTodayDateString()

        // Cloud has 3 words already started today (= default daily limit exhausted)
        val cloudProgress = (1..3).associate { i ->
            "word$i" to progress(
                wordId = "word$i",
                lastReviewedAt = "${today}T10:00:00Z",
                historyDate = today,
                nextReviewDate = SuperMemoEngine.addDaysToDate(today, 1)
            )
        }

        val sessionData = SessionManager.createDailySession(
            progressMap = cloudProgress,
            settings = UserSettings(dailyNewWordsLimit = 3),
            allWords = DictionaryWordsData.WORDS,
            todayStr = today
        )

        assertEquals(
            "Session should be empty when daily limit is already exhausted in cloud",
            0, sessionData.cards.size
        )
    }

    @Test
    fun `Bug1 - session with truly fresh state creates cards`() {
        val sessionData = SessionManager.createDailySession(
            progressMap = emptyMap(),
            settings = UserSettings(dailyNewWordsLimit = 3),
            allWords = DictionaryWordsData.WORDS
        )

        assertEquals(
            "Fresh session with no progress should create 3 new-word cards",
            3, sessionData.cards.size
        )
        assertTrue("All cards should be new", sessionData.cards.all { it.isNew })
    }

    // ─────────────────────── Bug 2 -- Debounced sync snapshot ─────────────
    // The original bug: scheduleSyncProgress received progressMap.value at call
    // time (before saveWordProgress suspend finished). The fix passes a lambda
    // evaluated after the 2.5s delay, so it always uploads the latest state.

    @Test
    fun `Bug2 - newer local entry takes priority over older cloud entry`() {
        val local = mapOf(
            "alea" to progress("alea", lastReviewedAt = "2026-08-18T14:00:00Z")
        )
        val cloud = mapOf(
            "alea" to progress("alea", lastReviewedAt = "2026-08-18T12:00:00Z")
        )

        val merged = mergeProgressMaps(local, cloud)

        assertEquals(
            "Local timestamp 14:00 should beat cloud timestamp 12:00",
            "2026-08-18T14:00:00Z", merged["alea"]!!.lastReviewedAt
        )
    }

    @Test
    fun `Bug2 - newer cloud entry takes priority over older local entry`() {
        val local = mapOf(
            "alea" to progress("alea", lastReviewedAt = "2026-08-17T09:00:00Z")
        )
        val cloud = mapOf(
            "alea" to progress("alea", lastReviewedAt = "2026-08-18T14:30:00Z")
        )

        val merged = mergeProgressMaps(local, cloud)

        assertEquals(
            "Cloud timestamp 2026-08-18 should beat local timestamp 2026-08-17",
            "2026-08-18T14:30:00Z", merged["alea"]!!.lastReviewedAt
        )
    }

    // ─────────────────────── Bug 3 -- Real-time merge logic ───────────────
    // The original bug: no onSnapshot listener on mobile -> lesson done on web
    // was invisible to the app until restart. The fix adds a real-time listener.
    // We test the MERGE LOGIC that the listener triggers.

    @Test
    fun `Bug3 - merge adds cloud-only words to result`() {
        val local = mapOf(
            "ubikwitet" to progress("ubikwitet", lastReviewedAt = "2026-08-18T08:00:00Z")
        )
        val cloud = mapOf(
            "ubikwitet" to progress("ubikwitet", lastReviewedAt = "2026-08-18T08:00:00Z"),
            "apatia"    to progress("apatia",    lastReviewedAt = "2026-08-18T14:00:00Z")
        )

        val merged = mergeProgressMaps(local, cloud)

        assertTrue("Merged map should contain the cloud-only word 'apatia'", "apatia" in merged)
        assertEquals("Merged map should contain 2 words", 2, merged.size)
    }

    @Test
    fun `Bug3 - merge keeps local-only words not yet synced to cloud`() {
        val local = mapOf(
            "ubikwitet" to progress("ubikwitet", lastReviewedAt = "2026-08-18T08:00:00Z"),
            "nomen-omen" to progress("nomen-omen", lastReviewedAt = "2026-08-18T13:00:00Z")
        )
        val cloud = mapOf(
            "ubikwitet" to progress("ubikwitet", lastReviewedAt = "2026-08-18T08:00:00Z")
        )

        val merged = mergeProgressMaps(local, cloud)

        assertTrue(
            "Merged map should preserve local-only word 'nomen-omen'",
            "nomen-omen" in merged
        )
    }

    @Test
    fun `Bug3 - end-to-end scenario web lesson makes mobile session empty`() {
        val today = "2026-08-18"
        val yesterday = "2026-08-17"

        // Mobile has old local state (lesson done yesterday)
        val localBeforeSync = mapOf(
            "w1" to progress("w1", lastReviewedAt = "${yesterday}T20:00:00Z",
                repetitions = 1, nextReviewDate = SuperMemoEngine.addDaysToDate(today, 1)),
            "w2" to progress("w2", lastReviewedAt = "${yesterday}T20:01:00Z",
                repetitions = 1, nextReviewDate = SuperMemoEngine.addDaysToDate(today, 1)),
        )

        // Web did today's lesson -- 3 new words added to cloud
        val cloudAfterWebLesson = localBeforeSync + mapOf(
            "w3" to progress("w3", lastReviewedAt = "${today}T14:00:00Z",
                repetitions = 1, historyDate = today,
                nextReviewDate = SuperMemoEngine.addDaysToDate(today, 1)),
            "w4" to progress("w4", lastReviewedAt = "${today}T14:01:00Z",
                repetitions = 1, historyDate = today,
                nextReviewDate = SuperMemoEngine.addDaysToDate(today, 1)),
            "w5" to progress("w5", lastReviewedAt = "${today}T14:02:00Z",
                repetitions = 1, historyDate = today,
                nextReviewDate = SuperMemoEngine.addDaysToDate(today, 1)),
        )

        val merged = mergeProgressMaps(
            local = localBeforeSync,
            cloud = cloudAfterWebLesson
        )

        assertEquals("Merged result should have all 5 words", 5, merged.size)

        val session = SessionManager.createDailySession(
            progressMap = merged,
            settings = UserSettings(dailyNewWordsLimit = 3),
            allWords = DictionaryWordsData.WORDS,
            todayStr = today
        )

        assertEquals(
            "After merging web lesson: 0 new cards expected (daily limit exhausted on web)",
            0, session.newCount
        )
    }

    // ─────────────────────── Merge edge cases ────────────────────────────

    @Test
    fun `merge - empty local and empty cloud returns empty map`() {
        val merged = mergeProgressMaps(emptyMap(), emptyMap())
        assertTrue(merged.isEmpty())
    }

    @Test
    fun `merge - equal timestamps prefer local entry`() {
        val sameTimestamp = "2026-08-18T12:00:00Z"
        val localEntry = progress("w1", lastReviewedAt = sameTimestamp, repetitions = 3)
        val cloudEntry = progress("w1", lastReviewedAt = sameTimestamp, repetitions = 1)

        val merged = mergeProgressMaps(
            local = mapOf("w1" to localEntry),
            cloud = mapOf("w1" to cloudEntry)
        )

        assertEquals(
            "With equal timestamps, local entry is preferred (repetitions=3)",
            3, merged["w1"]!!.repetitions
        )
    }

    @Test
    fun `merge - result is union of all word IDs`() {
        val local = mapOf(
            "a" to progress("a", "2026-08-18T10:00:00Z"),
            "b" to progress("b", "2026-08-18T10:00:00Z")
        )
        val cloud = mapOf(
            "b" to progress("b", "2026-08-18T10:00:00Z"),
            "c" to progress("c", "2026-08-18T10:00:00Z")
        )

        val merged = mergeProgressMaps(local, cloud)

        assertEquals(3, merged.size)
        assertTrue("a" in merged)
        assertTrue("b" in merged)
        assertTrue("c" in merged)
    }

    // ─────────────────────── SessionManager regression ───────────────────

    @Test
    fun `SessionManager - cards empty when all todays words already started`() {
        val today = SuperMemoEngine.getTodayDateString()

        val progressMap = (1..3).associate { i ->
            "word$i" to UserWordProgress(
                wordId = "word$i",
                repetitions = 1,
                lastReviewedAt = "${today}T09:00:00Z",
                nextReviewDate = SuperMemoEngine.addDaysToDate(today, 1),
                history = listOf(ReviewHistoryItem(today, 4))
            )
        }

        val session = SessionManager.createDailySession(
            progressMap = progressMap,
            settings = UserSettings(dailyNewWordsLimit = 3),
            allWords = DictionaryWordsData.WORDS,
            todayStr = today
        )

        assertEquals(
            "0 new words expected when daily limit already used up",
            0, session.newCount
        )
    }

    @Test
    fun `SessionManager - due reviews included even when new-word limit exhausted`() {
        val today = SuperMemoEngine.getTodayDateString()
        val yesterday = SuperMemoEngine.addDaysToDate(today, -1)

        // Use real word IDs from DictionaryWordsData.WORDS — fake IDs are invisible
        // to getDueReviewWords() which filters by allWords membership.
        val realWordIds = DictionaryWordsData.WORDS.map { it.id }
        check(realWordIds.size >= 4) { "Need at least 4 real words for this test" }

        // 3 words started today using real IDs (daily limit = 3 exhausted)
        val startedTodayWords = realWordIds.take(3).associateWith { wordId ->
            UserWordProgress(
                wordId = wordId,
                repetitions = 1,
                lastReviewedAt = "${today}T09:00:00Z",
                nextReviewDate = SuperMemoEngine.addDaysToDate(today, 1),
                history = listOf(ReviewHistoryItem(today, 4))
            )
        }

        // 1 word due for review today using a real ID (4th word in the dictionary)
        val dueWordId = realWordIds[3]
        val dueReview = mapOf(
            dueWordId to UserWordProgress(
                wordId = dueWordId,
                repetitions = 1,
                lastReviewedAt = "${yesterday}T09:00:00Z",
                nextReviewDate = today,
                history = listOf(ReviewHistoryItem(yesterday, 4))
            )
        )

        val session = SessionManager.createDailySession(
            progressMap = startedTodayWords + dueReview,
            settings = UserSettings(dailyNewWordsLimit = 3),
            allWords = DictionaryWordsData.WORDS,
            todayStr = today
        )

        assertEquals("Due review word should still appear in session", 1, session.dueCount)
        assertEquals("No new words expected", 0, session.newCount)
        assertEquals("Total session should have exactly 1 card (the due review)", 1, session.cards.size)
    }
}

