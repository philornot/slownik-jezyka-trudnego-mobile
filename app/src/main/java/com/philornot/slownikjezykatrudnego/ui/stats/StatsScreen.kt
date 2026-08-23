package com.philornot.slownikjezykatrudnego.ui.stats

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.philornot.slownikjezykatrudnego.data.model.DictionaryWord
import com.philornot.slownikjezykatrudnego.data.model.UserWordProgress
import com.philornot.slownikjezykatrudnego.domain.SuperMemoEngine
import com.philornot.slownikjezykatrudnego.ui.catalog.WordDetailBottomSheet
import com.philornot.slownikjezykatrudnego.ui.components.SjtCard
import com.philornot.slownikjezykatrudnego.ui.components.SjtInteractiveCard
import com.philornot.slownikjezykatrudnego.ui.theme.SjtTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Statistics Screen displaying streak, 7-day review activity chart, category progress, and hardest words.
 */
@Composable
fun StatsScreen(
    words: List<DictionaryWord>,
    progressMap: Map<String, UserWordProgress>,
    streakDays: Int,
    modifier: Modifier = Modifier
) {
    val colors = SjtTheme.colors
    val scrollState = rememberScrollState()
    var selectedWordForDetail by remember { mutableStateOf<DictionaryWord?>(null) }

    val progressList = remember(progressMap) { progressMap.values.toList() }
    val learnedCount = remember(progressList) { progressList.count { it.repetitions >= 3 } }
    val inProgressCount = remember(progressList) { progressList.count { it.repetitions < 3 } }
    val totalReviews = remember(progressList) { progressList.sumOf { it.history.size } }

    // 7-day chart data
    val last7DaysData = remember(progressList) {
        val countByDate = mutableMapOf<String, Int>()
        for (prog in progressList) {
            for (h in prog.history) {
                if (h.date.isNotBlank()) {
                    countByDate[h.date] = (countByDate[h.date] ?: 0) + 1
                }
            }
        }

        val today = LocalDate.now()
        val dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val dayNames = listOf("Pon", "Wt", "Śr", "Czw", "Pt", "Sob", "Nd")

        (6 downTo 0).map { offset ->
            val d = today.minusDays(offset.toLong())
            val dateStr = d.format(dtf)
            val label = dayNames[d.dayOfWeek.value - 1]
            val count = countByDate[dateStr] ?: 0
            Triple(label, count, offset == 0)
        }
    }

    val max7DayCount = remember(last7DaysData) {
        maxOf(last7DaysData.maxOfOrNull { it.second } ?: 5, 5)
    }

    // Category Stats
    val categoryStats = remember(words, progressMap) {
        val cats = mutableMapOf<String, Pair<Int, Int>>() // Total, Learned
        for (w in words) {
            val (total, learned) = cats.getOrDefault(w.category, Pair(0, 0))
            val prog = progressMap[w.id]
            val isLearned = (prog?.repetitions ?: 0) >= 3
            cats[w.category] = Pair(total + 1, if (isLearned) learned + 1 else learned)
        }
        cats.map { (name, pair) ->
            val pct = if (pair.first > 0) (pair.second.toFloat() / pair.first.toFloat()) * 100f else 0f
            Triple(name, pair, pct)
        }.sortedByDescending { it.third }
    }

    // Hardest Words
    val hardestWords = remember(words, progressMap) {
        words.mapNotNull { w ->
            val prog = progressMap[w.id]
            if (prog != null) {
                val hardCount = prog.history.count { it.grade == 0 }
                if (hardCount > 0 || prog.easeFactor < 2.4) {
                    Pair(w, prog)
                } else null
            } else null
        }.sortedBy { it.second.easeFactor }.take(6)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Summary Metrics 2x2 Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Card 1: Seria nauki
            SjtCard(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SERIA NAUKI",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = colors.textMuted
                        )
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = colors.badgeAmberText,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "$streakDays dni",
                        style = MaterialTheme.typography.headlineMedium,
                        color = colors.textAmberBrand
                    )
                }
            }

            // Card 2: Opanowane słowa
            SjtCard(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "OPANOWANE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = colors.textMuted
                        )
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = colors.badgeEmeraldText,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "$learnedCount / ${words.size}",
                        style = MaterialTheme.typography.headlineMedium,
                        color = colors.brandPrimary
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Card 3: W trakcie nauki
            SjtCard(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "W TRAKCIE NAUKI",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.textMuted
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "$inProgressCount",
                        style = MaterialTheme.typography.headlineMedium,
                        color = colors.textPrimary
                    )
                }
            }

            // Card 4: Wszystkie powtórki
            SjtCard(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "WYKONANE POWTÓRKI",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.textMuted
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "$totalReviews",
                        style = MaterialTheme.typography.headlineMedium,
                        color = colors.textPrimary
                    )
                }
            }
        }

        // 7-Day Activity Bar Chart
        SjtCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Aktywność (ostatnie 7 dni)",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "${last7DaysData.sumOf { it.second }} powtórek",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textMuted
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    last7DaysData.forEach { (label, count, isToday) ->
                        val barHeightFraction = (count.toFloat() / max7DayCount.toFloat()).coerceIn(0.06f, 1f)

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        ) {
                            // Review count number above bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (count > 0) {
                                    Text(
                                        text = "$count",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isToday) colors.brandPrimary else colors.textMuted
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Bar track / bar container with bottom baseline alignment
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(22.dp)
                                        .fillMaxHeight(barHeightFraction)
                                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                        .background(
                                            if (isToday) colors.brandPrimary
                                            else if (count > 0) colors.brandPrimary.copy(alpha = 0.5f)
                                            else colors.progressTrack
                                        )
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Day of week label
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isToday) FontWeight.ExtraBold else FontWeight.Medium,
                                color = if (isToday) colors.brandPrimary else colors.textMuted
                            )
                        }
                    }
                }
            }
        }

        // Category Breakdown
        SjtCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Opanowanie według kategorii",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary
                )

                categoryStats.forEach { (name, pair, pct) ->
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = name,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "${pair.second}/${pair.first} (${pct.toInt()}%)",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textMuted
                            )
                        }

                        LinearProgressIndicator(
                            progress = { pct / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = colors.brandPrimary,
                            trackColor = colors.progressTrack
                        )
                    }
                }
            }
        }

        // Hardest Words Section
        if (hardestWords.isNotEmpty()) {
            SjtCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = colors.badgeRoseText,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Najbardziej wymagające słówka",
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.textPrimary
                        )
                    }

                    hardestWords.forEach { (word, _) ->
                        Surface(
                            onClick = { selectedWordForDetail = word },
                            shape = RoundedCornerShape(10.dp),
                            color = colors.bgSurfaceElevated,
                            border = BorderStroke(1.dp, colors.borderDefault),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = word.word,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = colors.textSerifTitle
                                    )
                                    Text(
                                        text = word.shortDefinition,
                                        fontSize = 12.sp,
                                        color = colors.textSecondary,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    // Modal Details Bottom Sheet
    selectedWordForDetail?.let { word ->
        WordDetailBottomSheet(
            word = word,
            onDismiss = { selectedWordForDetail = null }
        )
    }
}
