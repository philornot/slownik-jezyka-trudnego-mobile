package com.philornot.slownikjezykatrudnego.wear.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Card
import androidx.wear.compose.material.CompactChip
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import com.philornot.slownikjezykatrudnego.data.model.DictionaryWord

@Composable
fun WearWordDetailScreen(
    word: DictionaryWord,
    onBackClick: () -> Unit,
    onRandomAnother: () -> Unit
) {
    val listState = rememberScalingLazyListState()

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
            // Title & phonetic
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                ) {
                    Text(
                        text = word.word,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colors.secondary,
                        textAlign = TextAlign.Center
                    )
                    word.phonetic?.let { phonetic ->
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = phonetic,
                            fontSize = 11.sp,
                            fontStyle = FontStyle.Italic,
                            color = Color(0xFF9CA3AF),
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .background(
                                color = Color(0xFF131A16),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = word.category.uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colors.primary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Short definition card
            item {
                Card(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(0.92f)
                ) {
                    Column(modifier = Modifier.padding(4.dp)) {
                        Text(
                            text = "ZNACZENIE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colors.primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = word.fullDefinition.ifBlank { word.shortDefinition },
                            fontSize = 12.sp,
                            color = Color.White,
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            // Example sentence
            if (word.examples.isNotEmpty()) {
                item {
                    Card(
                        onClick = {},
                        modifier = Modifier.fillMaxWidth(0.92f)
                    ) {
                        Column(modifier = Modifier.padding(4.dp)) {
                            Text(
                                text = "PRZYKŁAD UŻYCIA",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colors.secondary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "„${word.examples.first()}”",
                                fontSize = 11.sp,
                                fontStyle = FontStyle.Italic,
                                color = Color(0xFFE5E7EB),
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            }

            // Action buttons
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                ) {
                    CompactChip(
                        onClick = onRandomAnother,
                        label = { Text("Losuj inne", fontSize = 11.sp) }
                    )
                    CompactChip(
                        onClick = onBackClick,
                        label = { Text("Powrót", fontSize = 11.sp, color = Color(0xFF9CA3AF)) }
                    )
                }
            }
        }
    }
}
