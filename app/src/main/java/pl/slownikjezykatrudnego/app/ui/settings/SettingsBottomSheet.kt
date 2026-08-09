package pl.slownikjezykatrudnego.app.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import pl.slownikjezykatrudnego.app.data.model.TextSizeLevel
import pl.slownikjezykatrudnego.app.data.model.UserSettings
import pl.slownikjezykatrudnego.app.ui.components.SjtTouchButton
import pl.slownikjezykatrudnego.app.ui.theme.SjtTheme

/**
 * Settings Bottom Sheet for configuring daily limits, theme, accessibility, and resetting progress.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
    settings: UserSettings,
    onSaveSettings: (UserSettings) -> Unit,
    onResetProgress: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = SjtTheme.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scrollState = rememberScrollState()

    var localLimit by remember { mutableIntStateOf(settings.dailyNewWordsLimit) }
    var localHighContrast by remember { mutableStateOf(settings.highContrast) }
    var localReducedMotion by remember { mutableStateOf(settings.reducedMotion) }
    var localTextSize by remember { mutableStateOf(settings.textSize) }
    var isDarkTheme by remember { mutableStateOf(settings.isDarkTheme ?: false) }

    // Reset progress confirmation timer
    var isResetConfirmOpen by remember { mutableStateOf(false) }
    var resetCountdown by remember { mutableIntStateOf(5) }

    LaunchedEffect(isResetConfirmOpen) {
        if (isResetConfirmOpen) {
            resetCountdown = 5
            while (resetCountdown > 0) {
                delay(1000)
                resetCountdown--
            }
        }
    }

    fun applyAndSave() {
        onSaveSettings(
            settings.copy(
                dailyNewWordsLimit = localLimit,
                highContrast = localHighContrast,
                reducedMotion = localReducedMotion,
                textSize = localTextSize,
                isDarkTheme = isDarkTheme
            )
        )
    }

    ModalBottomSheet(
        onDismissRequest = {
            applyAndSave()
            onDismiss()
        },
        sheetState = sheetState,
        containerColor = colors.bgSurface,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = colors.brandPrimary
                    )
                    Text(
                        text = "Ustawienia",
                        style = MaterialTheme.typography.titleLarge,
                        color = colors.textSerifTitle
                    )
                }

                IconButton(onClick = {
                    applyAndSave()
                    onDismiss()
                }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Zamknij",
                        tint = colors.textMuted
                    )
                }
            }

            // Section 1: Daily Limit
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = colors.bgSurfaceElevated,
                border = BorderStroke(1.dp, colors.borderDefault),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Dzienny limit nowych słówek",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "$localLimit słówek",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = colors.brandPrimary
                        )
                    }

                    Text(
                        text = "Liczba nowych haseł wprowadzanych do nauki każdego dnia.",
                        fontSize = 11.5.sp,
                        color = colors.textMuted
                    )

                    Slider(
                        value = localLimit.toFloat(),
                        onValueChange = {
                            localLimit = it.toInt()
                            applyAndSave()
                        },
                        valueRange = 3f..15f,
                        steps = 11,
                        colors = SliderDefaults.colors(
                            thumbColor = colors.brandPrimary,
                            activeTrackColor = colors.brandPrimary,
                            inactiveTrackColor = colors.progressTrack
                        )
                    )
                }
            }

            // Section 2: Appearance & Theme
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = colors.bgSurfaceElevated,
                border = BorderStroke(1.dp, colors.borderDefault),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "WYGLĄD I DOSTĘPNOŚĆ",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.textMuted,
                        letterSpacing = 0.5.sp
                    )

                    // Dark Theme Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Ciemny motyw",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Głębokie, stonowane szałwiowe barwy nocne.",
                                fontSize = 11.5.sp,
                                color = colors.textMuted
                            )
                        }

                        Switch(
                            checked = isDarkTheme,
                            onCheckedChange = {
                                isDarkTheme = it
                                applyAndSave()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = colors.btnPrimaryText,
                                checkedTrackColor = colors.brandPrimary
                            )
                        )
                    }

                    HorizontalDivider(color = colors.borderDefault)

                    // High Contrast Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Wysoki kontrast",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Mocniejsze obramowania i wyraźniejsze teksty.",
                                fontSize = 11.5.sp,
                                color = colors.textMuted
                            )
                        }

                        Switch(
                            checked = localHighContrast,
                            onCheckedChange = {
                                localHighContrast = it
                                applyAndSave()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = colors.btnPrimaryText,
                                checkedTrackColor = colors.brandPrimary
                            )
                        )
                    }

                    HorizontalDivider(color = colors.borderDefault)

                    // Text Size Segmented Control
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Wielkość tekstu",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            TextSizeLevel.values().forEach { level ->
                                val isSelected = localTextSize == level
                                val label = when (level) {
                                    TextSizeLevel.SMALL -> "Standardowy"
                                    TextSizeLevel.MEDIUM -> "Średni"
                                    TextSizeLevel.LARGE -> "Duży"
                                }

                                Surface(
                                    onClick = {
                                        localTextSize = level
                                        applyAndSave()
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) colors.brandPrimary else colors.bgSurface,
                                    border = BorderStroke(1.dp, if (isSelected) colors.brandPrimary else colors.borderDefault)
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) colors.btnPrimaryText else colors.textPrimary,
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = colors.borderDefault)

                    // Reduced Motion Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Redukcja ruchu",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Wyłącza animacje i przejścia.",
                                fontSize = 11.5.sp,
                                color = colors.textMuted
                            )
                        }

                        Switch(
                            checked = localReducedMotion,
                            onCheckedChange = {
                                localReducedMotion = it
                                applyAndSave()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = colors.btnPrimaryText,
                                checkedTrackColor = colors.brandPrimary
                            )
                        )
                    }
                }
            }

            // Section 3: Reset Progress
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = colors.badgeRoseBg.copy(alpha = 0.4f),
                border = BorderStroke(1.dp, colors.badgeRoseBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "RESETOWANIE POSTĘPÓW",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.badgeRoseText,
                        letterSpacing = 0.5.sp
                    )

                    Text(
                        text = "Usuwa całą historię nauki, powtórki i serię dni.",
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )

                    if (!isResetConfirmOpen) {
                        Surface(
                            onClick = { isResetConfirmOpen = true },
                            shape = RoundedCornerShape(8.dp),
                            color = colors.badgeRoseBg,
                            border = BorderStroke(1.dp, colors.badgeRoseBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RestartAlt,
                                    contentDescription = null,
                                    tint = colors.badgeRoseText,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Resetuj postępy nauki",
                                    color = colors.badgeRoseText,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = if (resetCountdown > 0) "Potwierdź reset za ($resetCountdown s)..." else "Czy na pewno chcesz usunąć wszystkie postępy?",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.badgeRoseText
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Surface(
                                    onClick = {
                                        if (resetCountdown == 0) {
                                            onResetProgress()
                                            isResetConfirmOpen = false
                                            onDismiss()
                                        }
                                    },
                                    enabled = resetCountdown == 0,
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (resetCountdown == 0) colors.grade0Text else colors.textMuted
                                ) {
                                    Text(
                                        text = if (resetCountdown > 0) "Odczekaj $resetCountdown s" else "Tak, zresetuj wszystko",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    )
                                }

                                TextButton(onClick = { isResetConfirmOpen = false }) {
                                    Text(text = "Anuluj", color = colors.textPrimary, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Footer info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "v0.12.1 · ",
                    fontSize = 11.5.sp,
                    color = colors.textMuted
                )
                Text(
                    text = "Polityka prywatności",
                    fontSize = 11.5.sp,
                    color = colors.brandPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onOpenPrivacy() }
                )
            }
        }
    }
}
