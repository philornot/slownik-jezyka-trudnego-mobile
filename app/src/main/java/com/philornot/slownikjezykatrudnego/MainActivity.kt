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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as SjtApplication
        val viewModel = ViewModelProvider(
            this,
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SjtViewModel(
                        app.preferencesRepository,
                        app.firebaseRepository
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
}
