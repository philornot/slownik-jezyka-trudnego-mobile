package pl.slownikjezykatrudnego.app.ui.catalog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.slownikjezykatrudnego.app.data.model.DictionaryWord
import pl.slownikjezykatrudnego.app.data.model.UserWordProgress
import pl.slownikjezykatrudnego.app.ui.components.BadgeVariant
import pl.slownikjezykatrudnego.app.ui.components.SjtBadge
import pl.slownikjezykatrudnego.app.ui.components.SjtCard
import pl.slownikjezykatrudnego.app.ui.components.SjtInteractiveCard
import pl.slownikjezykatrudnego.app.ui.theme.SjtTheme

/**
 * Catalog Screen displaying dictionary vocabulary divided into unlocked and locked entries.
 */
@Composable
fun CatalogScreen(
    words: List<DictionaryWord>,
    progressMap: Map<String, UserWordProgress>,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Wszystkie") }
    var selectedWordForDetail by remember { mutableStateOf<DictionaryWord?>(null) }

    val colors = SjtTheme.colors
    val categories = remember(words) {
        listOf("Wszystkie") + words.map { it.category }.distinct().filter { it.isNotBlank() }
    }

    val unlockedWords = remember(words, progressMap) {
        words.filter { it.id in progressMap }
    }

    val lockedWords = remember(words, progressMap) {
        words.filter { it.id !in progressMap }
    }

    val filteredUnlocked = remember(unlockedWords, searchQuery, selectedCategory) {
        unlockedWords.filter { w ->
            val matchSearch = searchQuery.isBlank() ||
                    w.word.contains(searchQuery, ignoreCase = true) ||
                    w.shortDefinition.contains(searchQuery, ignoreCase = true) ||
                    w.fullDefinition.contains(searchQuery, ignoreCase = true)
            val matchCat = selectedCategory == "Wszystkie" || w.category == selectedCategory
            matchSearch && matchCat
        }
    }

    val filteredLocked = remember(lockedWords, searchQuery, selectedCategory) {
        lockedWords.filter { w ->
            val matchSearch = searchQuery.isBlank() ||
                    w.word.contains(searchQuery, ignoreCase = true) ||
                    w.category.contains(searchQuery, ignoreCase = true)
            val matchCat = selectedCategory == "Wszystkie" || w.category == selectedCategory
            matchSearch && matchCat
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Sticky Header: Search Bar & Categories
        SjtCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            cornerRadius = 14.dp
        ) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Search Input
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = "Szukaj w słówkach...",
                            fontSize = 13.sp,
                            color = colors.textMuted
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Szukaj",
                            tint = colors.textMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Wyczyść",
                                    tint = colors.textMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.brandPrimary,
                        unfocusedBorderColor = colors.borderDefault,
                        focusedContainerColor = colors.bgSurfaceElevated,
                        unfocusedContainerColor = colors.bgSurfaceElevated,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Category Chips & Progress Pill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.forEach { cat ->
                            val isSelected = selectedCategory == cat
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategory = cat },
                                label = {
                                    Text(
                                        text = cat,
                                        fontSize = 11.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = colors.brandPrimary,
                                    selectedLabelColor = colors.btnPrimaryText,
                                    containerColor = colors.bgSurfaceElevated,
                                    labelColor = colors.textSecondary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = if (isSelected) colors.brandPrimary else colors.borderDefault
                                ),
                                shape = RoundedCornerShape(9999.dp)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(9999.dp),
                        color = colors.bgSurfaceElevated,
                        border = BorderStroke(1.dp, colors.borderDefault),
                        modifier = Modifier.padding(start = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = colors.brandPrimary,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "${unlockedWords.size}/${words.size}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                        }
                    }
                }
            }
        }

        // List of Words
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // SEKCJA 1: Poznane Słówka
            if (filteredUnlocked.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = colors.badgeEmeraldText,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Poznane Słówka (${filteredUnlocked.size})",
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.textPrimary
                        )
                    }
                }

                items(filteredUnlocked, key = { it.id }) { word ->
                    val progress = progressMap[word.id]
                    val rep = progress?.repetitions ?: 0
                    val (repLabel, repVariant) = when {
                        rep >= 3 -> {
                            val label = when (rep) {
                                1 -> "1 powtórzenie"
                                in 2..4 -> "$rep powtórzenia"
                                else -> "$rep powtórzeń"
                            }
                            Pair(label, BadgeVariant.EMERALD)
                        }
                        else -> Pair("W trakcie nauki", BadgeVariant.AMBER)
                    }

                    SjtInteractiveCard(
                        onClick = { selectedWordForDetail = word },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = word.word,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = colors.textSerifTitle
                                    )
                                    if (!word.phonetic.isNullOrBlank()) {
                                        Text(
                                            text = word.phonetic,
                                            fontSize = 11.5.sp,
                                            fontStyle = FontStyle.Italic,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.textMuted
                                        )
                                    }
                                }

                                SjtBadge(
                                    text = repLabel,
                                    variant = repVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = word.shortDefinition,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.textSecondary,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(2.dp))
                            SjtBadge(
                                text = word.category,
                                variant = BadgeVariant.NEUTRAL
                            )
                        }
                    }
                }
            }

            // SEKCJA 2: Zablokowane Słówka
            if (filteredLocked.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 14.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = colors.textMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Oczekujące w kolejce (${filteredLocked.size})",
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.textMuted
                        )
                    }
                }

                items(filteredLocked, key = { it.id }) { word ->
                    SjtInteractiveCard(
                        onClick = { selectedWordForDetail = word },
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = colors.bgSurfaceElevated.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = word.word,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = word.category,
                                    fontSize = 11.5.sp,
                                    color = colors.textMuted
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Zablokowane",
                                tint = colors.textMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Modal Details Bottom Sheet
    selectedWordForDetail?.let { word ->
        WordDetailBottomSheet(
            word = word,
            onDismiss = { selectedWordForDetail = null }
        )
    }
}
