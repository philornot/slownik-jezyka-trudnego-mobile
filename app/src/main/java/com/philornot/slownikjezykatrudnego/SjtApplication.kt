package com.philornot.slownikjezykatrudnego

import android.app.Application
import com.google.firebase.FirebaseApp
import com.philornot.slownikjezykatrudnego.data.repository.FirebaseRepository
import com.philornot.slownikjezykatrudnego.data.repository.PreferencesRepository
import com.philornot.slownikjezykatrudnego.notifications.NotificationHelper
import com.philornot.slownikjezykatrudnego.notifications.NotificationScheduler

/**
 * Application entry point providing singleton dependencies. Initializes
 * Firebase and creates the repository layer.
 */
class SjtApplication : Application() {

    lateinit var preferencesRepository: PreferencesRepository
        private set

    lateinit var firebaseRepository: FirebaseRepository
        private set

    override fun onCreate() {
        super.onCreate()
        try {
            FirebaseApp.initializeApp(this)
        } catch (e: Exception) {
            android.util.Log.e("SjtApplication", "Firebase initialization failed. Continuing without cloud features.", e)
        }
        preferencesRepository = PreferencesRepository(this)
        firebaseRepository = FirebaseRepository(preferencesRepository)

        // Re-arm the daily reminder chain on every process start. This is what makes the
        // reminder survive app updates / WorkManager DB resets, not just device reboots
        // (device reboots alone are already handled by WorkManager's own persistence).
        NotificationHelper.ensureChannel(this)
        NotificationScheduler.scheduleReminders(this, preferencesRepository.loadSettings())
    }
}
