package pl.slownikjezykatrudnego.app.domain

import pl.slownikjezykatrudnego.app.data.model.ReviewGrade
import pl.slownikjezykatrudnego.app.data.model.ReviewHistoryItem
import pl.slownikjezykatrudnego.app.data.model.UserWordProgress
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToInt

/**
 * SuperMemo SM-2 Spaced Repetition engine and study streak calculator.
 */
object SuperMemoEngine {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    /**
     * Returns today's date formatted as YYYY-MM-DD in the device local timezone.
     */
    fun getTodayDateString(): String {
        return LocalDate.now().format(dateFormatter)
    }

    /**
     * Adds or subtracts days to/from a YYYY-MM-DD date string.
     */
    fun addDaysToDate(dateStr: String, days: Int): String {
        return try {
            val date = LocalDate.parse(dateStr, dateFormatter)
            date.plusDays(days.toLong()).format(dateFormatter)
        } catch (e: Exception) {
            getTodayDateString()
        }
    }

    /**
     * Calculates the current continuous study streak in days.
     */
    fun calculateStreak(progressMap: Map<String, UserWordProgress>): Int {
        val reviewDates = mutableSetOf<String>()
        for (prog in progressMap.values) {
            for (h in prog.history) {
                if (h.date.isNotBlank()) {
                    reviewDates.add(h.date)
                }
            }
        }
        if (reviewDates.isEmpty()) return 0

        val today = getTodayDateString()
        var streak = 0
        var checkDate = today

        while (true) {
            if (reviewDates.contains(checkDate)) {
                streak++
                checkDate = addDaysToDate(checkDate, -1)
            } else {
                if (streak == 0) {
                    val yesterday = addDaysToDate(today, -1)
                    if (reviewDates.contains(yesterday)) {
                        streak++
                        checkDate = addDaysToDate(yesterday, -1)
                        continue
                    }
                }
                break
            }
        }
        return streak
    }

    /**
     * Computes the updated SM-2 parameters for a word after a user review grade.
     */
    fun calculateSM2(
        wordId: String,
        grade: ReviewGrade,
        currentProgress: UserWordProgress? = null
    ): UserWordProgress {
        val today = getTodayDateString()
        val historyItem = ReviewHistoryItem(date = today, grade = grade.value)

        var repetitions = currentProgress?.repetitions ?: 0
        var easeFactor = currentProgress?.easeFactor ?: 2.5
        var interval = currentProgress?.interval ?: 1
        val existingHistory = currentProgress?.history ?: emptyList()
        val updatedHistory = existingHistory + historyItem

        val gradeVal = grade.value

        if (gradeVal < 3) {
            repetitions = 0
            interval = 1
        } else {
            easeFactor += (0.1 - (5 - gradeVal) * (0.08 + (5 - gradeVal) * 0.02))
            if (easeFactor < 1.3) {
                easeFactor = 1.3
            }

            when (repetitions) {
                0 -> interval = 1
                1 -> interval = 6
                else -> interval = (interval * easeFactor).roundToInt()
            }
            repetitions += 1
        }

        val nextReviewDate = addDaysToDate(today, interval)
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        return UserWordProgress(
            wordId = wordId,
            repetitions = repetitions,
            easeFactor = (easeFactor * 100.0).roundToInt() / 100.0,
            interval = interval,
            nextReviewDate = nextReviewDate,
            lastReviewedAt = isoFormat.format(Date()),
            history = updatedHistory
        )
    }

    /**
     * Checks if a word is due for spaced review today.
     */
    fun isWordDueToday(progress: UserWordProgress, todayStr: String = getTodayDateString()): Boolean {
        return progress.nextReviewDate <= todayStr
    }
}
