package com.philornot.slownikjezykatrudnego.domain

import com.philornot.slownikjezykatrudnego.data.model.DictionaryWord
import com.philornot.slownikjezykatrudnego.data.model.SessionCard
import com.philornot.slownikjezykatrudnego.data.model.UserSettings
import com.philornot.slownikjezykatrudnego.data.model.UserWordProgress

/**
 * Manages daily study session creation, cognitive load adaptive throttling, and question generation.
 */
object SessionManager {

    data class DailySessionData(
        val cards: List<SessionCard>,
        val dueCount: Int,
        val newCount: Int,
    )

    /**
     * Identifies which congratulatory message variant to show after a
     * completed session.
     *
     * This enum intentionally carries no text. Mapping a variant to its
     * title/description is a UI concern and belongs in `strings.xml`, not in
     * the domain layer — that keeps this class testable independently of the
     * copy shown on screen.
     */
    enum class CompletionMessageType {
        GREAT_JOB,
        SESSION_DONE,
        ERUDITION_GROWING
    }

    /**
     * Mulberry32 seeded pseudo-random number generator for deterministic daily card ordering.
     */
    class Mulberry32(seedString: String) {
        private var state: Long

        init {
            var h = 2166136261L xor seedString.length.toLong()
            for (char in seedString) {
                h = (h xor char.code.toLong()) * 16777619L
                h = h and 0xFFFFFFFFL
            }
            state = h
        }

        fun nextFloat(): Float {
            state = (state + 0x6D2B79F5L) and 0xFFFFFFFFL
            var t = state
            t = Math.imul((t xor (t ushr 15)).toInt(), (t or 1L).toInt()).toLong() and 0xFFFFFFFFL
            t = (t xor (t + Math.imul((t xor (t ushr 7)).toInt(), (t or 61L).toInt()).toLong())) and 0xFFFFFFFFL
            return ((t xor (t ushr 14)) and 0xFFFFFFFFL).toFloat() / 4294967296f
        }

        private object Math {
            fun imul(a: Int, b: Int): Int = a * b
        }
    }

    /**
     * Shuffles a list using the provided seeded RNG.
     */
    fun <T> shuffleList(list: List<T>, rng: Mulberry32): List<T> {
        val result = list.toMutableList()
        for (i in result.size - 1 downTo 1) {
            val j = (rng.nextFloat() * (i + 1)).toInt().coerceIn(0, i)
            val temp = result[i]
            result[i] = result[j]
            result[j] = temp
        }
        return result
    }

    /**
     * Returns words due for review today.
     */
    fun getDueReviewWords(
        progressMap: Map<String, UserWordProgress>,
        allWords: List<DictionaryWord>,
        todayStr: String = SuperMemoEngine.getTodayDateString()
    ): List<DictionaryWord> {
        return allWords.filter { word ->
            val progress = progressMap[word.id]
            progress != null && SuperMemoEngine.isWordDueToday(progress, todayStr)
        }
    }

    /**
     * Returns count of words in progress from previous days that are not yet mastered (repetitions < 3).
     */
    fun getUnmasteredWordsFromPreviousDaysCount(
        progressMap: Map<String, UserWordProgress>,
        todayStr: String = SuperMemoEngine.getTodayDateString()
    ): Int {
        return progressMap.values.count { p ->
            if (p.repetitions >= 3) return@count false
            val firstDate = p.history.firstOrNull()?.date
            firstDate == null || firstDate < todayStr
        }
    }

    /**
     * Returns count of words newly started today.
     */
    fun getWordsStartedTodayCount(
        progressMap: Map<String, UserWordProgress>,
        todayStr: String = SuperMemoEngine.getTodayDateString()
    ): Int {
        return progressMap.values.count { p ->
            val firstDate = p.history.firstOrNull()?.date
            firstDate == todayStr
        }
    }

    /**
     * Adaptively throttles daily new words to avoid cognitive overload.
     */
    fun calculateAdaptiveNewWordsLimit(
        progressMap: Map<String, UserWordProgress>,
        userConfiguredLimit: Int,
        todayStr: String = SuperMemoEngine.getTodayDateString()
    ): Int {
        val startedToday = getWordsStartedTodayCount(progressMap, todayStr)
        val maxRemaining = (userConfiguredLimit - startedToday).coerceAtLeast(0)
        if (maxRemaining <= 0) return 0

        val unmasteredPrev = getUnmasteredWordsFromPreviousDaysCount(progressMap, todayStr)

        return when {
            unmasteredPrev >= 8 -> 0
            unmasteredPrev >= 5 -> minOf(1, maxRemaining)
            unmasteredPrev >= 3 -> minOf(2, maxRemaining)
            else -> maxRemaining
        }
    }

    /**
     * Generates 4 options for a multiple choice quiz card.
     */
    fun generateOptionsForWord(
        word: DictionaryWord,
        allWords: List<DictionaryWord>,
        rng: Mulberry32
    ): List<String> {
        val correct = word.shortDefinition
        val distractors = word.distractors.toMutableList()

        if (distractors.size < 3) {
            val fallbackPool = allWords
                .filter { it.id != word.id }
                .map { it.shortDefinition }
                .filter { it !in distractors && it != correct }

            distractors.addAll(shuffleList(fallbackPool, rng).take(3 - distractors.size))
        }

        val chosenOptions = (distractors.take(3) + correct)
        return shuffleList(chosenOptions, rng)
    }

    /**
     * Creates the complete daily session.
     */
    fun createDailySession(
        progressMap: Map<String, UserWordProgress>,
        settings: UserSettings,
        allWords: List<DictionaryWord>,
        todayStr: String = SuperMemoEngine.getTodayDateString()
    ): DailySessionData {
        val sessionRng = Mulberry32("sjt-session-$todayStr")

        // 1. Spaced Repetition Due Reviews
        val dueWords = getDueReviewWords(progressMap, allWords, todayStr)

        // 2. Adaptive New Words
        val adaptiveLimit = calculateAdaptiveNewWordsLimit(progressMap, settings.dailyNewWordsLimit, todayStr)
        val unstartedWords = allWords.filter { it.id !in progressMap }
        val poolRng = Mulberry32("sjt-pool-$todayStr")
        val shuffledUnstarted = shuffleList(unstartedWords, poolRng)
        val newWords = shuffledUnstarted.take(adaptiveLimit)

        // 3. Assemble Session Cards
        val newCards = newWords.map { word ->
            SessionCard(
                word = word,
                isNew = true,
                userProgress = null,
                options = generateOptionsForWord(word, allWords, sessionRng)
            )
        }

        val reviewCards = dueWords.map { word ->
            SessionCard(
                word = word,
                isNew = false,
                userProgress = progressMap[word.id],
                options = generateOptionsForWord(word, allWords, sessionRng)
            )
        }

        // Mieszamy nowe słówka i powtórki razem (tak jak w wersji web)
        val cards = shuffleList(newCards + reviewCards, sessionRng)

        return DailySessionData(
            cards = cards,
            dueCount = dueWords.size,
            newCount = newWords.size
        )
    }

    /**
     * Picks a random congratulatory message variant for the daily session
     * summary screen.
     *
     * @return A randomly selected [CompletionMessageType]. The caller (UI
     *    layer) is responsible for resolving it to localized title/description
     *    strings.
     */
    fun getDailyCompletionMessage(): CompletionMessageType {
        return CompletionMessageType.entries.random()
    }
}
