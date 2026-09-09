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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.CompactChip
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import com.philornot.slownikjezykatrudnego.data.datasource.DictionaryWordsData
import com.philornot.slownikjezykatrudnego.data.model.DictionaryWord
import kotlinx.coroutines.delay

private data class QuizQuestion(
    val word: DictionaryWord,
    val options: List<String>,
    val correctIndex: Int
)

@Composable
fun WearQuizScreen(
    onFinish: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val listState = rememberScalingLazyListState()

    fun generateQuestions(): List<QuizQuestion> {
        val allWords = DictionaryWordsData.WORDS.shuffled()
        val questions = mutableListOf<QuizQuestion>()
        for (i in 0 until minOf(3, allWords.size / 2)) {
            val target = allWords[i * 2]
            val distractor = allWords[i * 2 + 1]
            val isTargetFirst = (i % 2 == 0)
            val options = if (isTargetFirst) {
                listOf(target.shortDefinition, distractor.shortDefinition)
            } else {
                listOf(distractor.shortDefinition, target.shortDefinition)
            }
            val correctIndex = if (isTargetFirst) 0 else 1
            questions.add(QuizQuestion(target, options, correctIndex))
        }
        return questions
    }

    var questions by remember { mutableStateOf(generateQuestions()) }
    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
    var score by remember { mutableIntStateOf(0) }
    var isQuizCompleted by remember { mutableStateOf(false) }

    LaunchedEffect(selectedOptionIndex) {
        val selected = selectedOptionIndex ?: return@LaunchedEffect
        val currentQuestion = questions.getOrNull(currentQuestionIndex) ?: return@LaunchedEffect

        val isCorrect = (selected == currentQuestion.correctIndex)
        if (isCorrect) {
            score++
        }
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)

        delay(950)
        if (currentQuestionIndex + 1 < questions.size) {
            currentQuestionIndex++
            selectedOptionIndex = null
        } else {
            isQuizCompleted = true
        }
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
            if (isQuizCompleted) {
                // Results Screen
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp)
                    ) {
                        Text(
                            text = "WYNIK QUIZU",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colors.primary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$score / ${questions.size}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (score >= 2) Color(0xFF34D399) else Color(0xFFFBBF24),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = when (score) {
                                3 -> "Znakomity wynik! Perfekcyjna erudycja."
                                2 -> "Bardzo dobrze! Prawie bezbłędnie."
                                else -> "Dobra próba! Praktyka czyni mistrza."
                            },
                            fontSize = 11.sp,
                            color = Color(0xFFE5E7EB),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
                    ) {
                        Chip(
                            onClick = {
                                questions = generateQuestions()
                                currentQuestionIndex = 0
                                selectedOptionIndex = null
                                score = 0
                                isQuizCompleted = false
                            },
                            label = { Text("Zagraj ponownie", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth(0.85f)
                        )
                        CompactChip(
                            onClick = onFinish,
                            label = { Text("Menu główne", fontSize = 11.sp, color = Color(0xFF9CA3AF)) }
                        )
                    }
                }
            } else {
                // Question Header
                val currentQuestion = questions.getOrNull(currentQuestionIndex)
                if (currentQuestion != null) {
                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp)
                        ) {
                            Text(
                                text = "PYTANIE ${currentQuestionIndex + 1} Z ${questions.size}",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colors.primary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = currentQuestion.word.word,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colors.secondary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Co oznacza to słowo?",
                                fontSize = 10.sp,
                                color = Color(0xFF9CA3AF),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Answer Options
                    currentQuestion.options.forEachIndexed { index, optionText ->
                        item {
                            val isSelected = (selectedOptionIndex == index)
                            val isCorrectAnswer = (index == currentQuestion.correctIndex)
                            val isLocked = (selectedOptionIndex != null)

                            val chipColor = when {
                                !isLocked -> Color(0xFF131A16)
                                isCorrectAnswer -> Color(0xFF064E3B)
                                isSelected -> Color(0xFF7F1D1D)
                                else -> Color(0xFF111827)
                            }
                            val textColor = when {
                                !isLocked -> Color.White
                                isCorrectAnswer -> Color(0xFF6EE7B7)
                                isSelected -> Color(0xFFFCA5A5)
                                else -> Color(0xFF6B7280)
                            }

                            Chip(
                                onClick = {
                                    if (selectedOptionIndex == null) {
                                        selectedOptionIndex = index
                                    }
                                },
                                enabled = (selectedOptionIndex == null),
                                modifier = Modifier.fillMaxWidth(0.92f),
                                colors = ChipDefaults.chipColors(
                                    backgroundColor = chipColor,
                                    contentColor = textColor,
                                    disabledBackgroundColor = chipColor,
                                    disabledContentColor = textColor
                                ),
                                label = {
                                    Text(
                                        text = optionText,
                                        fontSize = 11.sp,
                                        lineHeight = 13.5.sp,
                                        textAlign = TextAlign.Start,
                                        color = textColor
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
