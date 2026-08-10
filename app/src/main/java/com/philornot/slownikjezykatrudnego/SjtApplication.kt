package com.philornot.slownikjezykatrudnego

import android.app.Application
import com.google.firebase.FirebaseApp
import com.philornot.slownikjezykatrudnego.data.repository.FirebaseRepository
import com.philornot.slownikjezykatrudnego.data.repository.PreferencesRepository

/**
 * Application entry point providing singleton dependencies.
 * Initializes Firebase and creates the repository layer.
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
        firebaseRepository = FirebaseRepository(this, preferencesRepository)
    }
}
