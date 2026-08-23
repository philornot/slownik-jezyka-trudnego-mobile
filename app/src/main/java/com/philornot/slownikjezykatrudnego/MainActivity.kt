package com.philornot.slownikjezykatrudnego

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.philornot.slownikjezykatrudnego.ui.SjtApp
import com.philornot.slownikjezykatrudnego.ui.SjtViewModel
import com.philornot.slownikjezykatrudnego.util.InAppUpdateManager

/**
 * Main Activity hosting the Jetpack Compose Słownik Języka Trudnego
 * UI. Extends AppCompatActivity for seamless integration with
 * AppCompatDelegate theme switching.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: SjtViewModel
    private lateinit var inAppUpdateManager: InAppUpdateManager

    private val updateResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode != RESULT_OK) {
            // Update cancelled or failed; app continues running normally
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        inAppUpdateManager = InAppUpdateManager(this)
        inAppUpdateManager.checkForAppUpdate(updateResultLauncher)

        val app = application as SjtApplication
        viewModel = ViewModelProvider(
            this,
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SjtViewModel(
                        app.preferencesRepository,
                        app.firebaseRepository,
                        app.applicationContext
                    ) as T
                }
            }
        )[SjtViewModel::class.java]

        setContent {
            SjtApp(
                viewModel = viewModel,
                activity = this
            )
        }

        handleIntent(intent)
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: android.content.Intent?) {
        if (intent?.getBooleanExtra(EXTRA_AUTO_START_LESSON, false) == true) {
            if (::viewModel.isInitialized) {
                viewModel.setActiveTab(com.philornot.slownikjezykatrudnego.ui.components.SjtTab.LESSON)
            }
        }
    }

    /**
     * Called when the app returns to the foreground. Triggers a cloud data
     * merge in case other platforms (web, another device) made changes while
     * this app was in the background, and resumes any in-progress updates.
     */
    override fun onResume() {
        super.onResume()
        if (::viewModel.isInitialized) {
            viewModel.onAppForegrounded()
        }
        if (::inAppUpdateManager.isInitialized) {
            inAppUpdateManager.onResume(updateResultLauncher)
        }
    }

    companion object {
        const val EXTRA_AUTO_START_LESSON = "com.philornot.slownikjezykatrudnego.EXTRA_AUTO_START_LESSON"
    }
}