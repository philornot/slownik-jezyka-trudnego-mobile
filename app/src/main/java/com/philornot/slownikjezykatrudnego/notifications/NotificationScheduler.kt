package com.philornot.slownikjezykatrudnego.notifications

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.philornot.slownikjezykatrudnego.data.model.NotificationTimeSlot
import com.philornot.slownikjezykatrudnego.data.model.UserSettings
import java.util.Calendar
import java.util.concurrent.TimeUnit
import kotlin.random.Random

/**
 * Schedules the daily study-reminder notification at a random moment
 * inside the user's preferred [NotificationTimeSlot] (rano / w ciągu dnia
 * / wieczorem).
 *
 * There is deliberately no [androidx.work.PeriodicWorkRequest] here: a
 * periodic request fires at a fixed interval from a fixed anchor, which
 * can't reproduce "a different random time each day". Instead a single
 * [androidx.work.OneTimeWorkRequest] is enqueued for a random instant
 * inside today's (or tomorrow's, if today's window already passed) slot.
 * When [ReminderWorker] fires it shows the notification and immediately
 * calls back into this scheduler to enqueue the next day's random instant
 * — so the chain keeps itself alive as long as notifications stay enabled.
 */
object NotificationScheduler {

    /**
     * Unique WorkManager work name — reused so re-scheduling always replaces
     * the pending job.
     */
    const val UNIQUE_WORK_NAME = "sjt_daily_reminder"

    /**
     * (Re)schedules the daily reminder according to [settings]. If
     * notifications are disabled this cancels any pending reminder instead.
     * Safe to call every time settings are saved and once on app startup to
     * guarantee a chain is always alive while enabled.
     *
     * @param forceTomorrow When true, always picks a random moment in
     *    tomorrow's window rather than allowing today's (still-upcoming)
     *    window to be chosen. [ReminderWorker] passes `true` right after
     *    firing today's notification, so the chain can't double-fire later the
     *    same day if the just-fired moment happened to land early in a wide
     *    window.
     */
    fun scheduleDailyReminder(
        context: Context,
        settings: UserSettings,
        forceTomorrow: Boolean = false,
    ) {
        val workManager = WorkManager.getInstance(context.applicationContext)

        if (!settings.notificationsEnabled) {
            workManager.cancelUniqueWork(UNIQUE_WORK_NAME)
            return
        }

        val delayMillis = millisUntilNextRandomMoment(settings.notificationTimeSlot, forceTomorrow)

        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .addTag(UNIQUE_WORK_NAME)
            .build()

        // REPLACE: any previously pending reminder (e.g. from a just-changed time slot) is
        // dropped in favor of the freshly computed one.
        workManager.enqueueUniqueWork(UNIQUE_WORK_NAME, ExistingWorkPolicy.REPLACE, request)
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context.applicationContext).cancelUniqueWork(UNIQUE_WORK_NAME)
    }

    /**
     * Computes the delay (ms) until a random hour/minute inside [slot]'s
     * window. If that random moment for today has already gone by (or
     * [forceTomorrow] is set), rolls over to tomorrow's window instead — this
     * is what makes the reminder recur daily once [ReminderWorker] reschedules
     * itself, without ever landing twice on the same day.
     */
    private fun millisUntilNextRandomMoment(
        slot: NotificationTimeSlot,
        forceTomorrow: Boolean,
    ): Long {
        val now = Calendar.getInstance()

        val candidate = randomMomentToday(slot)
        val target = if (!forceTomorrow && candidate.after(now)) {
            candidate
        } else {
            randomMomentToday(slot).apply { add(Calendar.DAY_OF_YEAR, 1) }
        }

        return (target.timeInMillis - now.timeInMillis).coerceAtLeast(0L)
    }

    /**
     * Builds a [Calendar] set to a uniformly random minute inside [slot]'s
     * [startHour, endHour) window, today.
     */
    private fun randomMomentToday(slot: NotificationTimeSlot): Calendar {
        val windowMinutes = (slot.endHour - slot.startHour) * 60
        val randomMinuteOffset = Random.nextInt(windowMinutes)

        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, slot.startHour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.MINUTE, randomMinuteOffset)
        }
    }
}