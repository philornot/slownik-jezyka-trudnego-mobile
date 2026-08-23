package com.philornot.slownikjezykatrudnego.widget

import android.content.Context
import android.util.Log
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Utility helper to trigger asynchronous updates for all instances of [StreakWidget].
 */
object StreakWidgetUpdater {
    private const val TAG = "StreakWidgetUpdater"

    /**
     * Triggers an update for all active streak widgets on the home screen.
     */
    fun update(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                StreakWidget().updateAll(context.applicationContext)
                Log.d(TAG, "Successfully updated all Streak widgets")
            } catch (e: Exception) {
                Log.w(TAG, "Failed to update Streak widgets", e)
            }
        }
    }
}
