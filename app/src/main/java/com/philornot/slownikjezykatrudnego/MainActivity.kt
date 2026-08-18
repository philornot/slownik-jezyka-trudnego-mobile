package com.philornot.slownikjezykatrudnego

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.philornot.slownikjezykatrudnego.ui.SjtApp
import com.philornot.slownikjezykatrudnego.ui.SjtViewModel

/**
 * Main Activity hosting the Jetpack Compose Słownik Języka Trudnego UI.
 * Extends AppCompatActivity for seamless integration with AppCompatDelegate theme switching.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: SjtViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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
    }

    /**
     * Called when the app returns to the foreground.
     * Triggers a cloud data merge in case other platforms (web, another device)
     * made changes while this app was in the background.
     */
    override fun onResume() {
        super.onResume()
        if (::viewModel.isInitialized) {
            viewModel.onAppForegrounded()
        }
    }
}