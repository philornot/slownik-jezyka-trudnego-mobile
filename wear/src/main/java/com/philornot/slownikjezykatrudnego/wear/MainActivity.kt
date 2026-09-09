package com.philornot.slownikjezykatrudnego.wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.philornot.slownikjezykatrudnego.data.datasource.DictionaryWordsData
import com.philornot.slownikjezykatrudnego.data.model.DictionaryWord
import com.philornot.slownikjezykatrudnego.wear.theme.SjtWearTheme
import com.philornot.slownikjezykatrudnego.wear.ui.WearMainScreen
import com.philornot.slownikjezykatrudnego.wear.ui.WearQuizScreen
import com.philornot.slownikjezykatrudnego.wear.ui.WearWordDetailScreen
import com.philornot.slownikjezykatrudnego.wear.ui.WearWordListScreen

sealed interface WearDestination {
    data object Main : WearDestination
    data class WordDetail(val word: DictionaryWord) : WearDestination
    data object Quiz : WearDestination
    data object Catalog : WearDestination
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SjtWearTheme {
                WearAppNavigation()
            }
        }
    }
}

@Composable
fun WearAppNavigation() {
    var currentDestination by remember { mutableStateOf<WearDestination>(WearDestination.Main) }

    fun navigateTo(destination: WearDestination) {
        currentDestination = destination
    }

    fun navigateBack() {
        currentDestination = WearDestination.Main
    }

    BackHandler(enabled = currentDestination != WearDestination.Main) {
        navigateBack()
    }

    when (val destination = currentDestination) {
        is WearDestination.Main -> {
            WearMainScreen(
                onWordClick = { word -> navigateTo(WearDestination.WordDetail(word)) },
                onStartQuiz = { navigateTo(WearDestination.Quiz) },
                onRandomWord = {
                    val randomWord = DictionaryWordsData.WORDS.random()
                    navigateTo(WearDestination.WordDetail(randomWord))
                },
                onOpenCatalog = { navigateTo(WearDestination.Catalog) }
            )
        }

        is WearDestination.WordDetail -> {
            WearWordDetailScreen(
                word = destination.word,
                onBackClick = { navigateBack() },
                onRandomAnother = {
                    val nextWord = DictionaryWordsData.WORDS.random()
                    navigateTo(WearDestination.WordDetail(nextWord))
                }
            )
        }

        is WearDestination.Quiz -> {
            WearQuizScreen(
                onFinish = { navigateBack() }
            )
        }

        is WearDestination.Catalog -> {
            WearWordListScreen(
                onWordClick = { word -> navigateTo(WearDestination.WordDetail(word)) },
                onBackClick = { navigateBack() }
            )
        }
    }
}
