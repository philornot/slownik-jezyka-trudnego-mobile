package com.philornot.slownikjezykatrudnego.notifications

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.philornot.slownikjezykatrudnego.data.repository.PreferencesRepository
import com.philornot.slownikjezykatrudnego.domain.SuperMemoEngine

/**
 * One-shot worker that checks in the late evening if an active study streak
 * is at risk of being lost today (i.e. user has an active streak but has not
 * yet completed today's lesson). If at risk, displays a streak saver
 * reminder and re-arms [NotificationScheduler] for tomorrow evening.
 */
class StreakSaverWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val repository = PreferencesRepository(applicationContext)
        val settings = repository.loadSettings()

        if (!settings.notificationsEnabled) {
            return Result.success()
        }

        val hasPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            val progressMap = repository.loadProgressMap()
            val streak = SuperMemoEngine.calculateStreak(progressMap)
            val isCompleted = NotificationHelper.isTodaySessionCompleted(applicationContext)

            if (streak > 0 && !isCompleted) {
                NotificationHelper.showStreakSaverReminder(applicationContext)
            }
        }

        // Re-arm for tomorrow evening
        NotificationScheduler.scheduleStreakSaverReminder(
            applicationContext,
            settings,
            forceTomorrow = true
        )

        return Result.success()
    }
}
