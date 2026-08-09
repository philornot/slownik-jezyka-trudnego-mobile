package pl.slownikjezykatrudnego.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import pl.slownikjezykatrudnego.app.ui.SjtApp
import pl.slownikjezykatrudnego.app.ui.SjtViewModel

/**
 * Main Activity hosting the Jetpack Compose Słownik Języka Trudnego UI.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as SjtApplication
        val viewModel = ViewModelProvider(
            this,
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SjtViewModel(app.preferencesRepository) as T
                }
            }
        )[SjtViewModel::class.java]

        setContent {
            SjtApp(viewModel = viewModel)
        }
    }
}
