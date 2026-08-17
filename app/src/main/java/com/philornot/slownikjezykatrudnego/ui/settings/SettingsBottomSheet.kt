package com.philornot.slownikjezykatrudnego.ui.settings

/**
 * Settings Bottom Sheet for configuring daily limits, theme,
 * accessibility, and resetting progress. Strictly aligned with the WEB
 * version styling and functionality.
 *
 * Theme toggle now uses circular reveal animation (same as web version).
 */

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.TextFormat
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.philornot.slownikjezykatrudnego.BuildConfig
import com.philornot.slownikjezykatrudnego.data.model.NotificationTimeSlot
import com.philornot.slownikjezykatrudnego.data.model.TextSizeLevel
import com.philornot.slownikjezykatrudnego.data.model.UserSettings
import com.philornot.slownikjezykatrudnego.ui.theme.CircularRevealThemeWrapper
import com.philornot.slownikjezykatrudnego.ui.theme.LocalThemeTransitionState
import com.philornot.slownikjezykatrudnego.ui.theme.SjtTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
    settings: UserSettings,
    onSaveSettings: (UserSettings) -> Unit,
    onResetProgress: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onToggleTheme: ((Offset?) -> Unit)?,
    onDismiss: () -> Unit
) {
    val colors = SjtTheme.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scrollState = rememberScrollState()
    val themeTransitionState = LocalThemeTransitionState.current
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var localLimit by remember { mutableIntStateOf(settings.dailyNewWordsLimit) }
    var localHighContrast by remember { mutableStateOf(settings.highContrast) }
    var localReducedMotion by remember { mutableStateOf(settings.reducedMotion) }
    var localTextSize by remember { mutableStateOf(settings.textSize) }
    var notificationsEnabled by remember { mutableStateOf(settings.notificationsEnabled) }
    var notificationTimeSlot by remember { mutableStateOf(settings.notificationTimeSlot) }

    fun applyAndSave() {
        onSaveSettings(
            settings.copy(
                dailyNewWordsLimit = localLimit,
                highContrast = localHighContrast,
                reducedMotion = localReducedMotion,
                textSize = localTextSize,
                notificationsEnabled = notificationsEnabled,
                notificationTimeSlot = notificationTimeSlot
            )
        )
    }

    // Sync notification permission state
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
        if (isGranted) {
            notificationsEnabled = true
            applyAndSave()
        } else {
            notificationsEnabled = false
            applyAndSave()
        }
    }

    // Refresh permission state on resume (e.g. back from system settings)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasNotificationPermission =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED
                    } else {
                        true
                    }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Reset progress confirmation timer
    var isResetConfirmOpen by remember { mutableStateOf(false) }
    var resetCountdown by remember { mutableIntStateOf(5) }

    // Credit dialog for Dawid Siekielski
    var showCreditDialog by remember { mutableStateOf(false) }

    // Position of the theme toggle button for circular reveal origin
    var themeButtonCenter by remember { mutableStateOf(Offset.Zero) }

    LaunchedEffect(isResetConfirmOpen) {
        if (isResetConfirmOpen) {
            resetCountdown = 5
            while (resetCountdown > 0) {
                delay(1000)
                resetCountdown--
            }
        }
    }

    // fun applyAndSave() was moved up

    // Credit dialog
    if (showCreditDialog) {
        AlertDialog(
            onDismissRequest = { showCreditDialog = false },
            containerColor = colors.bgSurface,
            title = {
                Text(
                    text = ":>",
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.textSerifTitle
                )
            },
            text = {
                Text(
                    text = "Autor logo strony: Dawid Siekielski",
                    color = colors.textPrimary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showCreditDialog = false }) {
                    Text(
                        text = "OK",
                        color = colors.brandPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }

    ModalBottomSheet(
        onDismissRequest = {
            applyAndSave()
            onDismiss()
        },
        sheetState = sheetState,
        containerColor = colors.bgSurface,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        dragHandle = {
            Surface(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 36.dp, height = 4.dp),
                color = colors.borderMuted,
                shape = RoundedCornerShape(2.dp)
            ) {}
        }
    ) {
        val sheetContent: @Composable () -> Unit = {
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
                    Column {
                        Text(
                            text = "Ustawienia",
                            style = MaterialTheme.typography.titleLarge,
                            color = colors.textSerifTitle
                        )
                        Text(
                            text = "Zmiany widoczne od razu · Zapisz, żeby zachować",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textMuted
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

                // Section 1: Learning Parameters
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoStories,
                            contentDescription = null,
                            tint = colors.textAmberBrand,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "PARAMETRY NAUKI",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = colors.textAmberBrand,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = colors.bgSurfaceElevated,
                        border = BorderStroke(1.dp, colors.borderDefault),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Nowe słowa na dzienną sesję",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "$localLimit",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = colors.textAmberBrand
                                )
                            }

                            Slider(
                                value = localLimit.toFloat(),
                                onValueChange = {
                                    localLimit = it.toInt()
                                    applyAndSave()
                                },
                                valueRange = 1f..20f,
                                steps = 19,
                                colors = SliderDefaults.colors(
                                    thumbColor = colors.brandPrimary,
                                    activeTrackColor = colors.brandPrimary,
                                    inactiveTrackColor = colors.progressTrack
                                )
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "1 słowo",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = colors.textMuted
                                )
                                Text(
                                    text = "10 słów",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = colors.textMuted
                                )
                                Text(
                                    text = "20 słów",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = colors.textMuted
                                )
                            }
                        }
                    }

                    // Notifications Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = colors.brandPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Column {
                                Text(
                                    text = "Powiadomienia o powtórkach",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "Codzienne przypomnienie w aplikacji",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.textMuted
                                )
                            }
                        }

                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = { checked ->
                                if (checked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                    ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.POST_NOTIFICATIONS
                                    ) != PackageManager.PERMISSION_GRANTED
                                ) {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    notificationsEnabled = checked
                                    applyAndSave()
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = colors.brandPrimary
                            )
                        )
                    }

                    // Warning if enabled but no permission
                    if (notificationsEnabled && !hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp, bottom = 4.dp)
                                .clickable {
                                    val intent =
                                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                            data =
                                                Uri.fromParts("package", context.packageName, null)
                                        }
                                    context.startActivity(intent)
                                },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = colors.badgeRoseText,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Brak uprawnień systemowych (dotknij, aby naprawić)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.badgeRoseText
                            )
                        }
                    }

                    // Time-of-day slot picker — only relevant while notifications are enabled.
                    AnimatedVisibility(visible = notificationsEnabled) {
                        Column(
                            modifier = Modifier.padding(top = 2.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Pora przypomnienia",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.textMuted
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                NotificationTimeSlot.entries.forEach { slot ->
                                    val selected = notificationTimeSlot == slot
                                    Surface(
                                        onClick = {
                                            notificationTimeSlot = slot
                                            applyAndSave()
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (selected) colors.brandPrimary else colors.bgSurfaceElevated,
                                        contentColor = if (selected) colors.btnPrimaryText else colors.textPrimary,
                                        border = if (selected) null else BorderStroke(
                                            1.dp,
                                            colors.borderDefault
                                        )
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(vertical = 10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = slot.label,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }
                            Text(
                                text = "Dostaniesz jedno powiadomienie dziennie, o losowej porze w tym przedziale.",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.textMuted
                            )
                        }
                    }
                }

                // Section 2: Appearance (Theme + Accessibility)
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    HorizontalDivider(color = colors.borderDefault)

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = colors.textAmberBrand,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "WYGLĄD",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = colors.textAmberBrand,
                            letterSpacing = 0.5.sp
                        )
                    }

                    // Theme Toggle Card (circular reveal — like web version)
                    val isDark = settings.isDarkTheme ?: false
                    Surface(
                        onClick = {
                            if (onToggleTheme != null) {
                                coroutineScope.launch {
                                    val origin =
                                        if (themeButtonCenter != Offset.Zero) themeButtonCenter else null
                                    android.util.Log.d(
                                        "ThemeTransition",
                                        "[UI] Theme setting clicked in SettingsBottomSheet. Origin: $origin"
                                    )
                                    val snapshot = try {
                                        themeTransitionState?.graphicsLayer?.toImageBitmap()
                                    } catch (e: Exception) {
                                        android.util.Log.e(
                                            "ThemeTransition",
                                            "Failed to capture graphicsLayer snapshot",
                                            e
                                        )
                                        null
                                    }
                                    if (snapshot != null) {
                                        android.util.Log.d(
                                            "ThemeTransition",
                                            "[STEP 0/5: CAPTURE] Composable graphicsLayer snapshot captured (${snapshot.width}x${snapshot.height}px)."
                                        )
                                    }
                                    themeTransitionState?.oldBitmap = snapshot
                                    onToggleTheme(origin)
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = colors.bgSurfaceElevated,
                        border = BorderStroke(1.dp, colors.borderDefault),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                                    contentDescription = null,
                                    tint = colors.brandPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = if (isDark) "Jasny motyw" else "Ciemny motyw",
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = if (isDark) "Przełącz na jasne barwy dzienne" else "Głębokie, stonowane szałwiowe barwy nocne",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colors.textMuted
                                    )
                                }
                            }

                            // Theme toggle button — captures position for circular reveal
                            Surface(
                                onClick = {
                                    if (onToggleTheme != null) {
                                        coroutineScope.launch {
                                            val origin =
                                                if (themeButtonCenter != Offset.Zero) themeButtonCenter else null
                                            android.util.Log.d(
                                                "ThemeTransition",
                                                "[UI] Theme icon button clicked in SettingsBottomSheet. Origin: $origin"
                                            )
                                            val snapshot = try {
                                                themeTransitionState?.graphicsLayer?.toImageBitmap()
                                            } catch (e: Exception) {
                                                android.util.Log.e(
                                                    "ThemeTransition",
                                                    "Failed to capture graphicsLayer snapshot",
                                                    e
                                                )
                                                null
                                            }
                                            if (snapshot != null) {
                                                android.util.Log.d(
                                                    "ThemeTransition",
                                                    "[STEP 0/5: CAPTURE] Composable graphicsLayer snapshot captured (${snapshot.width}x${snapshot.height}px)."
                                                )
                                            }
                                            themeTransitionState?.oldBitmap = snapshot
                                            onToggleTheme(origin)
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .size(44.dp)
                                    .onGloballyPositioned { coords ->
                                        val pos = coords.positionInRoot()
                                        val size = coords.size
                                        themeButtonCenter = Offset(
                                            pos.x + size.width / 2f,
                                            pos.y + size.height / 2f
                                        )
                                    },
                                shape = RoundedCornerShape(10.dp),
                                color = colors.brandPrimary,
                                enabled = onToggleTheme != null
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                                        contentDescription = "Przełącz motyw",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Section 3: Accessibility
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    HorizontalDivider(color = colors.borderDefault)

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = null,
                            tint = colors.textAmberBrand,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "DOSTĘPNOŚĆ",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = colors.textAmberBrand,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = colors.bgSurfaceElevated,
                        border = BorderStroke(1.dp, colors.borderDefault),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {

                            // High Contrast Toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Wysoki kontrast",
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = "Wyrazistsze kolory, grubsze obramowania",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
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
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = colors.brandPrimary
                                    )
                                )
                            }

                            HorizontalDivider(color = colors.borderDefault)

                            // Text Size
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.TextFormat,
                                        contentDescription = null,
                                        tint = colors.brandPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Wielkość tekstu",
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.textPrimary
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    TextSizeLevel.entries.forEach { level ->
                                        val isSelected = localTextSize == level
                                        val label = when (level) {
                                            TextSizeLevel.SMALL -> "Mały"
                                            TextSizeLevel.MEDIUM -> "Średni"
                                            TextSizeLevel.LARGE -> "Duży"
                                        }
                                        val subLabel = when (level) {
                                            TextSizeLevel.SMALL -> "Domyślny"
                                            TextSizeLevel.MEDIUM -> "Powiększony"
                                            TextSizeLevel.LARGE -> "Maksymalny"
                                        }

                                        Surface(
                                            onClick = {
                                                localTextSize = level
                                                applyAndSave()
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isSelected) colors.brandPrimary else colors.bgSurface,
                                            border = BorderStroke(
                                                2.dp,
                                                if (isSelected) colors.brandPrimary else colors.borderDefault
                                            )
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(vertical = 10.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(
                                                    text = label,
                                                    fontSize = when (level) {
                                                        TextSizeLevel.SMALL -> 12.sp
                                                        TextSizeLevel.MEDIUM -> 14.sp
                                                        TextSizeLevel.LARGE -> 16.sp
                                                    },
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = if (isSelected) Color.White else colors.textMuted
                                                )
                                                Text(
                                                    text = subLabel,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) Color.White.copy(alpha = 0.75f) else colors.textMuted.copy(
                                                        alpha = 0.75f
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(color = colors.borderDefault)

                            // Reduced Motion Toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Redukcja animacji",
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = "Wyłączenie animacji i przejść",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
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
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = colors.brandPrimary
                                    )
                                )
                            }
                        }
                    }
                }

                // Section 4: Reset Progress
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    HorizontalDivider(color = colors.borderDefault)

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = colors.badgeRoseBg.copy(alpha = 0.4f),
                        border = BorderStroke(1.dp, colors.badgeRoseBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (!isResetConfirmOpen) {
                                Surface(
                                    onClick = { isResetConfirmOpen = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    color = colors.badgeRoseBg,
                                    border = BorderStroke(1.dp, colors.badgeRoseBorder)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            Icons.Default.RestartAlt,
                                            contentDescription = null,
                                            tint = colors.badgeRoseText,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "Resetuj cały postęp nauki",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = colors.badgeRoseText,
                                            modifier = Modifier.padding(start = 8.dp)
                                        )
                                    }
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Icon(
                                            Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = colors.grade0Text,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Column {
                                            Text(
                                                text = "Czy na pewno chcesz zresetować całą historię?",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = colors.badgeRoseText
                                            )
                                            Text(
                                                text = "Ta operacja usunie wszystkie powtórki i rozpocznie naukę od zera. Nie można jej cofnąć.",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = colors.badgeRoseText
                                            )
                                        }
                                    }

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
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (resetCountdown == 0) colors.grade0Text else colors.badgeRoseBg,
                                            contentColor = if (resetCountdown == 0) Color.White else colors.badgeRoseText,
                                            border = if (resetCountdown != 0) BorderStroke(
                                                1.dp,
                                                colors.badgeRoseBorder
                                            ) else null
                                        ) {
                                            Box(
                                                modifier = Modifier.padding(vertical = 12.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = if (resetCountdown > 0) "Odczekaj $resetCountdown s..." else "Potwierdzam resetowanie",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        Surface(
                                            onClick = { isResetConfirmOpen = false },
                                            modifier = Modifier.weight(0.5f),
                                            shape = RoundedCornerShape(10.dp),
                                            color = colors.bgSurfaceElevated,
                                            contentColor = colors.textPrimary,
                                            border = BorderStroke(1.dp, colors.borderDefault)
                                        ) {
                                            Box(
                                                modifier = Modifier.padding(vertical = 12.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    "Anuluj",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Footer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "v${BuildConfig.VERSION_NAME}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textMuted.copy(alpha = 0.6f)
                        )
                        Text(text = "•", color = colors.textMuted.copy(alpha = 0.3f))
                        // Serduszko — credit dla Dawida Siekielskiego
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Autor logo",
                            tint = Color(0xFFE11D48).copy(alpha = 0.7f),
                            modifier = Modifier
                                .size(14.dp)
                                .clickable { showCreditDialog = true }
                        )
                    }

                    Text(
                        text = "Polityka prywatności",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.brandPrimary,
                        modifier = Modifier.clickable { onOpenPrivacy() }
                    )
                }
            }
        }

        if (themeTransitionState != null) {
            CircularRevealThemeWrapper(
                state = themeTransitionState,
                skipAnimation = settings.reducedMotion,
                modifier = Modifier.fillMaxWidth()
            ) {
                sheetContent()
            }
        } else {
            sheetContent()
        }
    }
}
