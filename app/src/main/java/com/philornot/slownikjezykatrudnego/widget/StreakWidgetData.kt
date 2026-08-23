package com.philornot.slownikjezykatrudnego.widget

import com.philornot.slownikjezykatrudnego.data.model.UserWordProgress
import com.philornot.slownikjezykatrudnego.domain.SuperMemoEngine
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

/**
 * Model representing learning status for a single day of the week.
 */
data class DayActivityStatus(
    val dayName: String,
    val dateString: String,
    val isCompleted: Boolean,
    val isToday: Boolean,
    val isPast: Boolean
)

/**
 * High-level data state required to render the Streak Widget.
 */
data class StreakWidgetData(
    val streakDays: Int,
    val isCompletedToday: Boolean,
    val isUrgentEvening: Boolean,
    val weekDays: List<DayActivityStatus>
) {
    companion object {
        private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        /**
         * Computes the complete widget state from the user's progress map.
         */
        fun fromProgress(progressMap: Map<String, UserWordProgress>): StreakWidgetData {
            val todayStr = SuperMemoEngine.getTodayDateString()
            val streak = SuperMemoEngine.calculateStreak(progressMap)

            val reviewedDates = mutableSetOf<String>()
            for (prog in progressMap.values) {
                for (h in prog.history) {
                    if (h.date.isNotBlank()) {
                        reviewedDates.add(h.date)
                    }
                }
            }

            val isCompletedToday = reviewedDates.contains(todayStr)

            // Urgent if after 19:00 (7 PM) and lesson is not yet completed
            val currentHour = LocalTime.now().hour
            val isUrgentEvening = (!isCompletedToday && currentHour >= 19)

            // Build current week (Monday to Sunday)
            val todayDate = LocalDate.now()
            val monday = todayDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

            val weekDayLabels = listOf("Pn", "Wt", "Śr", "Cz", "Pt", "So", "Nd")
            val weekDays = (0..6).map { dayOffset ->
                val date = monday.plusDays(dayOffset.toLong())
                val dateFormatted = date.format(dateFormatter)
                val isToday = date.isEqual(todayDate)
                val isPast = date.isBefore(todayDate)
                val isCompleted = reviewedDates.contains(dateFormatted)

                DayActivityStatus(
                    dayName = weekDayLabels[dayOffset],
                    dateString = dateFormatted,
                    isCompleted = isCompleted,
                    isToday = isToday,
                    isPast = isPast
                )
            }

            return StreakWidgetData(
                streakDays = streak,
                isCompletedToday = isCompletedToday,
                isUrgentEvening = isUrgentEvening,
                weekDays = weekDays
            )
        }
    }
}
