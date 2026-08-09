package pl.slownikjezykatrudnego.app.data.model

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable

/**
 * SuperMemo review evaluation grades.
 */
enum class ReviewGrade(val value: Int, val label: String) {
    AGAIN(0, "Bardzo słabo"),
    HARD(3, "Słabo"),
    GOOD(4, "Dobrze"),
    EASY(5, "Bardzo dobrze")
}

/**
 * Represents an interactive flashcard/quiz item within the daily study session.
 *
 * @property word The vocabulary entry.
 * @property isNew Whether the word is newly introduced today.
 * @property userProgress Existing progress data or null if not yet reviewed.
 * @property options 4 randomized short definition choices for the active recall quiz.
 */
@OptIn(InternalSerializationApi::class)
@Serializable
data class SessionCard(
    val word: DictionaryWord,
    val isNew: Boolean,
    val userProgress: UserWordProgress? = null,
    val options: List<String> = emptyList()
)

/**
 * Phase of the daily study session.
 */
@OptIn(InternalSerializationApi::class)
@Serializable
enum class SessionPhase {
    SHOWCASE,
    QUIZ
}

/**
 * Snapshot of active session state for persistence across process deaths.
 */
@OptIn(InternalSerializationApi::class)
@Serializable
data class SessionState(
    val date: String,
    val sessionPhase: SessionPhase,
    val currentCardIndex: Int,
    val cardsReviewedInSession: Int,
    val sessionCompleted: Boolean
)
