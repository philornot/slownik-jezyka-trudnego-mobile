package pl.slownikjezykatrudnego.app

import android.app.Application
import pl.slownikjezykatrudnego.app.data.repository.PreferencesRepository

/**
 * Application entry point providing singleton dependencies.
 */
class SjtApplication : Application() {

    lateinit var preferencesRepository: PreferencesRepository
        private set

    override fun onCreate() {
        super.onCreate()
        preferencesRepository = PreferencesRepository(this)
    }
}
