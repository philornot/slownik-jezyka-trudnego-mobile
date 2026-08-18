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
        /**
         * True when the regular SM-2 session was empty and this is a voluntary
         * bonus review.
         */
        val isBonusSession: Boolean = false,
    )

    /**
     * Identifies which congratulatory message variant to show after a
     * completed session.
     *
     * This enum intentionally carries no text. Mapping a variant to its
     * title/description is a UI concern and belongs in `strings.xml`, not in
     * the domain layer — that keeps this class testable independently of the
     * copy shown on screen.
     *
     * Regular variants are shown after a normal SM-2 session. Bonus variants
     * are shown after a voluntary extra review session.
     */
    enum class CompletionMessageType {
        // ── Regular session variants ──────────────────────────────────────
        GREAT_JOB,
        SESSION_DONE,
        ERUDITION_GROWING,
        VOCABULARY_MASTERED,
        DAY_WELL_SPENT,
        LEXICON_GROWS,
        ONE_STEP_FURTHER,
        BRAIN_TRAINED,
        KNOWLEDGE_IS_POWER,
        SHORT_SESSIONS_WIN,

        // ── Bonus session variants ────────────────────────────────────────
        BONUS_GREAT_INITIATIVE,
        BONUS_BEYOND_THE_PLAN,
        BONUS_EXTRA_DOSE,
        BONUS_CONSISTENT,
    }

    private val regularMessages = listOf(
        CompletionMessageType.GREAT_JOB,
        CompletionMessageType.SESSION_DONE,
        CompletionMessageType.ERUDITION_GROWING,
        CompletionMessageType.VOCABULARY_MASTERED,
        CompletionMessageType.DAY_WELL_SPENT,
        CompletionMessageType.LEXICON_GROWS,
        CompletionMessageType.ONE_STEP_FURTHER,
        CompletionMessageType.BRAIN_TRAINED,
        CompletionMessageType.KNOWLEDGE_IS_POWER,
        CompletionMessageType.SHORT_SESSIONS_WIN,
    )

    private val bonusMessages = listOf(
        CompletionMessageType.BONUS_GREAT_INITIATIVE,
        CompletionMessageType.BONUS_BEYOND_THE_PLAN,
        CompletionMessageType.BONUS_EXTRA_DOSE,
        CompletionMessageType.BONUS_CONSISTENT,
    )

    /**
     * Mulberry32 seeded pseudo-random number generator for deterministic daily
     * card ordering.
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
     * Returns words that are currently in-progress (started but not yet
     * mastered) and are NOT yet scheduled for review today.
     *
     * These words are used to build a voluntary "bonus" practice session when
     * the regular SM-2 session would otherwise be empty (e.g. due to adaptive
     * throttling). Cards are sorted by first-encounter date ascending so the
     * oldest unmastered words get priority.
     *
     * @param progressMap Current word progress keyed by word ID.
     * @param allWords Full dictionary word list.
     * @param todayStr Today's date as YYYY-MM-DD (defaults to system date).
     * @return Sorted list of in-progress, not-yet-due [DictionaryWord]s.
     */
    fun getInProgressNotDueWords(
        progressMap: Map<String, UserWordProgress>,
        allWords: List<DictionaryWord>,
        todayStr: String = SuperMemoEngine.getTodayDateString(),
    ): List<DictionaryWord> {
        val wordMap = allWords.associateBy { it.id }
        return progressMap.values
            .filter { p ->
                p.repetitions < 3 &&
                        !SuperMemoEngine.isWordDueToday(p, todayStr)
            }
            .sortedBy { p -> p.history.firstOrNull()?.date ?: todayStr }
            .mapNotNull { p -> wordMap[p.wordId] }
    }

    /**
     * Returns count of words in progress from previous days that are not yet
     * mastered (repetitions < 3).
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

        // If the regular session is empty, fall back to a bonus review of all
        // in-progress words that aren't officially due yet. This ensures the user
        // always has something meaningful to practice every day.
        if (cards.isEmpty()) {
            val bonusWords = getInProgressNotDueWords(progressMap, allWords, todayStr)
            val bonusRng = Mulberry32("sjt-bonus-$todayStr")
            val bonusCards = bonusWords.map { word ->
                SessionCard(
                    word = word,
                    isNew = false,
                    userProgress = progressMap[word.id],
                    options = generateOptionsForWord(word, allWords, bonusRng)
                )
            }
            return DailySessionData(
                cards = bonusCards,
                dueCount = 0,
                newCount = 0,
                isBonusSession = bonusCards.isNotEmpty()
            )
        }

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
     * @return A randomly selected [CompletionMessageType] from the regular
     *    pool. The caller (UI layer) is responsible for resolving it to
     *    localized title/description strings.
     */
    fun getDailyCompletionMessage(): CompletionMessageType = regularMessages.random()

    /**
     * Picks a random congratulatory message variant for a voluntary bonus
     * review session (shown when no regular SM-2 cards were due today).
     *
     * @return A randomly selected [CompletionMessageType] from the bonus pool.
     */
    fun getBonusCompletionMessage(): CompletionMessageType = bonusMessages.random()
}
