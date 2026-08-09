package pl.slownikjezykatrudnego.app.data.model

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable

/**
 * Historical record of a single review grade submission.
 *
 * @property date Date of review formatted as YYYY-MM-DD.
 * @property grade SuperMemo score (0, 3, 4, 5).
 */
@OptIn(InternalSerializationApi::class)
@Serializable
data class ReviewHistoryItem(
    val date: String,
    val grade: Int
)

/**
 * Represents the learning progress and SuperMemo SM-2 state for a single word.
 *
 * @property wordId Identifier matching [DictionaryWord.id].
 * @property repetitions Number of consecutive successful recall reviews.
 * @property easeFactor SM-2 ease factor determining interval growth (default 2.5, min 1.3).
 * @property interval Current review interval in days.
 * @property nextReviewDate Scheduled next review date formatted as YYYY-MM-DD.
 * @property lastReviewedAt Timestamp in ISO format of the latest review.
 * @property history Chronological list of historical review evaluations.
 */
@OptIn(InternalSerializationApi::class)
@Serializable
data class UserWordProgress(
    val wordId: String,
    val repetitions: Int = 0,
    val easeFactor: Double = 2.5,
    val interval: Int = 1,
    val nextReviewDate: String,
    val lastReviewedAt: String,
    val history: List<ReviewHistoryItem> = emptyList()
)
