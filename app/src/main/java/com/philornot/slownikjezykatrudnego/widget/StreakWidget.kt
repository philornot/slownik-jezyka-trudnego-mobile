package com.philornot.slownikjezykatrudnego.widget

import android.content.Context
import android.content.Intent
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.philornot.slownikjezykatrudnego.MainActivity
import com.philornot.slownikjezykatrudnego.R
import com.philornot.slownikjezykatrudnego.data.repository.PreferencesRepository

/**
 * Modern, ultra-clean Duolingo-style Streak Widget built with Jetpack Glance.
 * Clean, minimalistic design focused on flame, streak count, and weekly timeline.
 */
class StreakWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val prefsRepo = PreferencesRepository(context)
        val progressMap = prefsRepo.loadProgressMap()
        val data = StreakWidgetData.fromProgress(progressMap)

        provideContent {
            GlanceTheme {
                StreakWidgetContent(data = data)
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun StreakWidgetContent(data: StreakWidgetData) {
    val context = LocalContext.current
    val size = LocalSize.current

    val launchIntent = Intent(context, MainActivity::class.java).apply {
        action = Intent.ACTION_VIEW
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        if (!data.isCompletedToday) {
            putExtra(MainActivity.EXTRA_AUTO_START_LESSON, true)
        }
    }
    val clickModifier = GlanceModifier.clickable(actionStartActivity(launchIntent))

    val isOneRow = size.height < 105.dp

    if (isOneRow) {
        when {
            size.width >= 260.dp -> OneRowVeryWideWidget(data = data, clickModifier = clickModifier)
            size.width >= 185.dp -> OneRowMediumWideWidget(data = data, clickModifier = clickModifier)
            else -> OneRowCompactWidget(data = data, clickModifier = clickModifier)
        }
    } else {
        when {
            size.width >= 195.dp -> MultiRowWideWidget(
                data = data,
                widgetWidth = size.width,
                clickModifier = clickModifier
            )
            else -> MultiRowSquareWidget(data = data, clickModifier = clickModifier)
        }
    }
}

/**
 * 1-Row Compact layout (e.g. 2x1): Flame + Streak Count.
 */
@androidx.compose.runtime.Composable
private fun OneRowCompactWidget(
    data: StreakWidgetData,
    clickModifier: GlanceModifier
) {
    val flameRes = getFlameResource(data)
    val appearance = getWidgetAppearance(data)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(appearance.bg)
            .cornerRadius(20.dp)
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .then(clickModifier),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                provider = ImageProvider(flameRes),
                contentDescription = "Płomień serii",
                modifier = GlanceModifier.size(26.dp)
            )
            Spacer(modifier = GlanceModifier.width(8.dp))
            Text(
                text = "${data.streakDays}",
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = if (data.streakDays == 1) " dzień serii" else " dni serii",
                style = TextStyle(
                    color = GlanceTheme.colors.onSurfaceVariant,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            )
            Spacer(modifier = GlanceModifier.defaultWeight())
        }
    }
}

/**
 * 1-Row Medium-Wide layout (e.g. 3x1): Flame + Streak ("1 dzień") + 7 Days timeline.
 */
@androidx.compose.runtime.Composable
private fun OneRowMediumWideWidget(
    data: StreakWidgetData,
    clickModifier: GlanceModifier
) {
    val flameRes = getFlameResource(data)
    val appearance = getWidgetAppearance(data)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(appearance.bg)
            .cornerRadius(20.dp)
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .then(clickModifier),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                provider = ImageProvider(flameRes),
                contentDescription = "Płomień serii",
                modifier = GlanceModifier.size(24.dp)
            )
            Spacer(modifier = GlanceModifier.width(6.dp))
            Text(
                text = "${data.streakDays}",
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = if (data.streakDays == 1) " dzień" else " dni",
                style = TextStyle(
                    color = GlanceTheme.colors.onSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            )

            Spacer(modifier = GlanceModifier.defaultWeight())

            // 7 Days timeline
            WeekTimelineRow(
                weekDays = data.weekDays,
                dotSize = 13.dp,
                dotHorizontalPadding = 2.dp,
                textSize = 8.sp
            )
        }
    }
}

/**
 * 1-Row Very-Wide layout (e.g. 4x1, 5x1): Flame + Full Streak + 7 Days timeline.
 */
@androidx.compose.runtime.Composable
private fun OneRowVeryWideWidget(
    data: StreakWidgetData,
    clickModifier: GlanceModifier
) {
    val flameRes = getFlameResource(data)
    val appearance = getWidgetAppearance(data)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(appearance.bg)
            .cornerRadius(20.dp)
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .then(clickModifier),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                provider = ImageProvider(flameRes),
                contentDescription = "Płomień serii",
                modifier = GlanceModifier.size(26.dp)
            )
            Spacer(modifier = GlanceModifier.width(8.dp))
            Text(
                text = "${data.streakDays}",
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = if (data.streakDays == 1) " dzień serii" else " dni serii",
                style = TextStyle(
                    color = GlanceTheme.colors.onSurfaceVariant,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            )

            Spacer(modifier = GlanceModifier.defaultWeight())

            WeekTimelineRow(
                weekDays = data.weekDays,
                dotSize = 14.dp,
                dotHorizontalPadding = 3.dp,
                textSize = 9.sp
            )
        }
    }
}

/**
 * 2x2 Square tile layout (height >= 105dp, width < 195dp).
 */
@androidx.compose.runtime.Composable
private fun MultiRowSquareWidget(
    data: StreakWidgetData,
    clickModifier: GlanceModifier
) {
    val flameRes = getFlameResource(data)
    val appearance = getWidgetAppearance(data)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(appearance.bg)
            .cornerRadius(24.dp)
            .padding(14.dp)
            .then(clickModifier),
        contentAlignment = Alignment.TopStart
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.Top
        ) {
            // Top: Flame
            Image(
                provider = ImageProvider(flameRes),
                contentDescription = "Płomień serii",
                modifier = GlanceModifier.size(32.dp)
            )

            Spacer(modifier = GlanceModifier.defaultWeight())

            // Center: Big Streak Count
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "${data.streakDays}",
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurface,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = GlanceModifier.width(6.dp))
                Text(
                    text = if (data.streakDays == 1) "dzień" else "dni",
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurfaceVariant,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = GlanceModifier.padding(bottom = 4.dp)
                )
            }

            Spacer(modifier = GlanceModifier.height(2.dp))

            // Bottom Subtitle
            Text(
                text = appearance.subtitleText,
                style = TextStyle(
                    color = GlanceTheme.colors.onSurfaceVariant,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal
                ),
                maxLines = 1
            )
        }
    }
}

/**
 * Multi-Row Wide layout (3x2, 4x2, 5x2): Header + (Flame/Streak on Left) + (7 Days on Right).
 */
@androidx.compose.runtime.Composable
private fun MultiRowWideWidget(
    data: StreakWidgetData,
    widgetWidth: Dp,
    clickModifier: GlanceModifier
) {
    val flameRes = getFlameResource(data)
    val appearance = getWidgetAppearance(data)

    val isMediumWidth = widgetWidth < 250.dp
    val dotPadding = if (isMediumWidth) 2.dp else 4.dp
    val dotSize = if (isMediumWidth) 14.dp else 16.dp

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(appearance.bg)
            .cornerRadius(24.dp)
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .then(clickModifier),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Header Row: App Name
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SŁOWNIK JĘZYKA TRUDNEGO",
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurfaceVariant,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = GlanceModifier.height(6.dp))

            // Main Content Row: Left (Flame + Streak) | Right (7-Day timeline)
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Flame and streak counter
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        provider = ImageProvider(flameRes),
                        contentDescription = "Płomień serii",
                        modifier = GlanceModifier.size(if (isMediumWidth) 30.dp else 34.dp)
                    )
                    Spacer(modifier = GlanceModifier.width(8.dp))
                    Column {
                        Text(
                            text = "${data.streakDays}",
                            style = TextStyle(
                                color = GlanceTheme.colors.onSurface,
                                fontSize = if (isMediumWidth) 24.sp else 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = if (data.streakDays == 1) "dzień serii" else "dni serii",
                            style = TextStyle(
                                color = GlanceTheme.colors.onSurfaceVariant,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }

                Spacer(modifier = GlanceModifier.defaultWeight())

                // Right: 7-Day timeline
                WeekTimelineRow(
                    weekDays = data.weekDays,
                    dotSize = dotSize,
                    dotHorizontalPadding = dotPadding,
                    textSize = if (isMediumWidth) 9.sp else 10.sp
                )
            }
        }
    }
}

/**
 * Shared 7-Day timeline component.
 */
@androidx.compose.runtime.Composable
private fun WeekTimelineRow(
    weekDays: List<DayActivityStatus>,
    dotSize: Dp,
    dotHorizontalPadding: Dp,
    textSize: androidx.compose.ui.unit.TextUnit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        weekDays.forEach { day ->
            val dotRes = when {
                day.isCompleted -> R.drawable.ic_widget_dot_done
                day.isToday -> R.drawable.ic_widget_dot_today_pending
                else -> R.drawable.ic_widget_dot_pending
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = GlanceModifier.padding(horizontal = dotHorizontalPadding)
            ) {
                Text(
                    text = day.dayName,
                    style = TextStyle(
                        color = if (day.isToday) GlanceTheme.colors.primary else GlanceTheme.colors.onSurfaceVariant,
                        fontSize = textSize,
                        fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Normal
                    )
                )
                Spacer(modifier = GlanceModifier.height(2.dp))
                Image(
                    provider = ImageProvider(dotRes),
                    contentDescription = day.dayName,
                    modifier = GlanceModifier.size(dotSize)
                )
            }
        }
    }
}

private fun getFlameResource(data: StreakWidgetData): Int {
    return when {
        data.streakDays == 0 -> R.drawable.ic_widget_flame_muted
        data.isUrgentEvening -> R.drawable.ic_widget_flame_urgent
        else -> R.drawable.ic_widget_flame
    }
}

private fun getWidgetAppearance(data: StreakWidgetData): WidgetStateAppearance {
    return when {
        data.isCompletedToday -> WidgetThemeColors.completedState()
        data.isUrgentEvening -> WidgetThemeColors.urgentState()
        else -> WidgetThemeColors.pendingState()
    }
}

/**
 * Helper container holding theme colors and subtitle text.
 */
private data class WidgetStateAppearance(
    val bg: ColorProvider,
    val subtitleText: String
)

private object WidgetThemeColors {

    fun completedState(): WidgetStateAppearance {
        return WidgetStateAppearance(
            bg = ColorProvider(R.color.widget_bg_completed),
            subtitleText = "Świetna robota na dziś!"
        )
    }

    fun urgentState(): WidgetStateAppearance {
        return WidgetStateAppearance(
            bg = ColorProvider(R.color.widget_bg_urgent),
            subtitleText = "Powtórz słówka przed północą"
        )
    }

    fun pendingState(): WidgetStateAppearance {
        return WidgetStateAppearance(
            bg = ColorProvider(R.color.widget_bg_pending),
            subtitleText = "Kliknij, aby rozpocząć naukę"
        )
    }
}
