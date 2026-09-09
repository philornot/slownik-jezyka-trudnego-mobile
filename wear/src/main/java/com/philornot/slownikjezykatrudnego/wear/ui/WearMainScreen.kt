package com.philornot.slownikjezykatrudnego.wear.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Card
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import com.philornot.slownikjezykatrudnego.data.datasource.DictionaryWordsData
import com.philornot.slownikjezykatrudnego.data.model.DictionaryWord
import java.util.Calendar

@Composable
fun WearMainScreen(
    onWordClick: (DictionaryWord) -> Unit,
    onStartQuiz: () -> Unit,
    onRandomWord: () -> Unit,
    onOpenCatalog: () -> Unit
) {
    val listState = rememberScalingLazyListState()

    val dailyWord = remember {
        val calendar = Calendar.getInstance()
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        val year = calendar.get(Calendar.YEAR)
        val words = DictionaryWordsData.WORDS
        val index = (dayOfYear + year * 365) % words.size
        words[index]
    }

    Scaffold(
        timeText = { TimeText() },
        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) },
        positionIndicator = { PositionIndicator(scalingLazyListState = listState) }
    ) {
        ScalingLazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // App Header
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = "SŁOWNIK",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colors.primary,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "JĘZYKA TRUDNEGO",
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF9CA3AF),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Word of the day card
            item {
                Card(
                    onClick = { onWordClick(dailyWord) },
                    modifier = Modifier.fillMaxWidth(0.92f)
                ) {
                    Column(modifier = Modifier.padding(4.dp)) {
                        Text(
                            text = "SŁÓWKO DNIA",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colors.primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = dailyWord.word,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colors.secondary
                        )
                        dailyWord.phonetic?.let { phonetic ->
                            Text(
                                text = phonetic,
                                fontSize = 10.sp,
                                fontStyle = FontStyle.Italic,
                                color = Color(0xFF9CA3AF)
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = dailyWord.shortDefinition,
                            fontSize = 10.5.sp,
                            color = Color(0xFFE5E7EB),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 13.5.sp
                        )
                    }
                }
            }

            // Quick quiz button
            item {
                Chip(
                    onClick = onStartQuiz,
                    label = { Text("Szybki quiz", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    secondaryLabel = { Text("3 pytania na refleks", fontSize = 9.sp) },
                    colors = ChipDefaults.primaryChipColors(
                        backgroundColor = Color(0xFF131A16),
                        contentColor = MaterialTheme.colors.primary
                    ),
                    modifier = Modifier.fillMaxWidth(0.92f)
                )
            }

            // Random word button
            item {
                Chip(
                    onClick = onRandomWord,
                    label = { Text("Losuj słówko", fontSize = 12.sp) },
                    colors = ChipDefaults.secondaryChipColors(
                        backgroundColor = Color(0xFF161B18)
                    ),
                    modifier = Modifier.fillMaxWidth(0.92f)
                )
            }

            // Catalog button
            item {
                Chip(
                    onClick = onOpenCatalog,
                    label = { Text("Katalog słówek", fontSize = 12.sp) },
                    secondaryLabel = { Text("${DictionaryWordsData.WORDS.size} słówek", fontSize = 9.sp) },
                    colors = ChipDefaults.secondaryChipColors(
                        backgroundColor = Color(0xFF161B18)
                    ),
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .padding(bottom = 16.dp)
                )
            }
        }
    }
}
