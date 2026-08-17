package com.philornot.slownikjezykatrudnego.notifications

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.philornot.slownikjezykatrudnego.data.repository.PreferencesRepository

/**
 * One-shot worker that fires the daily study reminder and immediately
 * re-arms [NotificationScheduler] for the next day, so the "one random
 * notification per chosen time-of-day slot" chain keeps running for as
 * long as the user leaves notifications enabled.
 *
 * Reads settings fresh from [PreferencesRepository] rather than trusting
 * stale WorkRequest input data, since the user may have changed the time
 * slot or disabled notifications entirely since this work was originally
 * scheduled (potentially a day ago).
 */
class ReminderWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val repository = PreferencesRepository(applicationContext)
        val settings = repository.loadSettings()

        if (!settings.notificationsEnabled) {
            // Disabled since this was scheduled — don't notify, don't reschedule.
            return Result.success()
        }

        val hasPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            NotificationHelper.showDailyReminder(applicationContext)
        }

        // Re-arm for tomorrow's random moment inside the (possibly updated) preferred slot.
        // forceTomorrow=true: we just fired today's notification, so the next one must land
        // tomorrow even if today's window is still technically open.
        NotificationScheduler.scheduleDailyReminder(
            applicationContext,
            settings,
            forceTomorrow = true
        )

        return Result.success()
    }
}
