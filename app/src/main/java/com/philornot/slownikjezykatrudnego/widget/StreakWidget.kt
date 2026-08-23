package com.philornot.slownikjezykatrudnego.widget

import android.content.Context
import android.content.Intent
import androidx.compose.ui.unit.DpSize
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
 * Modern, clean Duolingo-style Streak Widget built with Jetpack Glance.
 * Supports responsive layouts (1-row height and 2+ rows height across all widths)
 * and dual-theme Material You dynamic colors.
 */
class StreakWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(
            SMALL_1_ROW,
            WIDE_1_ROW,
            SMALL_2_ROWS,
            WIDE_2_ROWS
        )
    )

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

    companion object {
        val SMALL_1_ROW = DpSize(120.dp, 48.dp)
        val WIDE_1_ROW = DpSize(220.dp, 48.dp)
        val SMALL_2_ROWS = DpSize(120.dp, 90.dp)
        val WIDE_2_ROWS = DpSize(220.dp, 90.dp)
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

    val isOneRow = size.height < 85.dp
    val isWide = size.width >= 210.dp

    when {
        isOneRow && isWide -> WideOneRowStreakWidget(data = data, clickModifier = clickModifier)
        isOneRow && !isWide -> CompactOneRowStreakWidget(data = data, clickModifier = clickModifier)
        !isOneRow && isWide -> WideMultiRowStreakWidget(data = data, clickModifier = clickModifier)
        else -> CompactSquareStreakWidget(data = data, clickModifier = clickModifier)
    }
}

/**
 * 1-Row Compact layout (e.g. 2x1).
 */
@androidx.compose.runtime.Composable
private fun CompactOneRowStreakWidget(
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
            StatusBadge(text = appearance.statusText, bg = appearance.badgeBg, textColor = appearance.badgeText, isSmall = true)
        }
    }
}

/**
 * 1-Row Wide layout (e.g. 3x1, 4x1, 5x1).
 */
@androidx.compose.runtime.Composable
private fun WideOneRowStreakWidget(
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
            // Left: Flame + Streak
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

            // Center: 7-day compact dots
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                data.weekDays.forEach { day ->
                    val dotRes = when {
                        day.isCompleted -> R.drawable.ic_widget_dot_done
                        day.isToday -> R.drawable.ic_widget_dot_today_pending
                        else -> R.drawable.ic_widget_dot_pending
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = GlanceModifier.padding(horizontal = 3.dp)
                    ) {
                        Text(
                            text = day.dayName,
                            style = TextStyle(
                                color = if (day.isToday) GlanceTheme.colors.primary else GlanceTheme.colors.onSurfaceVariant,
                                fontSize = 8.sp,
                                fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                        Spacer(modifier = GlanceModifier.height(1.dp))
                        Image(
                            provider = ImageProvider(dotRes),
                            contentDescription = day.dayName,
                            modifier = GlanceModifier.size(13.dp)
                        )
                    }
                }
            }

            Spacer(modifier = GlanceModifier.defaultWeight())

            // Right: Status Badge
            StatusBadge(text = appearance.statusText, bg = appearance.badgeBg, textColor = appearance.badgeText, isSmall = true)
        }
    }
}

/**
 * 2x2 Square tile layout.
 */
@androidx.compose.runtime.Composable
private fun CompactSquareStreakWidget(
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
            // Top Row: Flame + Status Badge
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    provider = ImageProvider(flameRes),
                    contentDescription = "Płomień serii",
                    modifier = GlanceModifier.size(28.dp)
                )
                Spacer(modifier = GlanceModifier.defaultWeight())
                StatusBadge(text = appearance.statusText, bg = appearance.badgeBg, textColor = appearance.badgeText, isSmall = false)
            }

            Spacer(modifier = GlanceModifier.defaultWeight())

            // Center: Streak Number + Label
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "${data.streakDays}",
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurface,
                        fontSize = 34.sp,
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

            // Subtitle
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
 * Wide Multi-Row layout (3x2, 4x2, 5x2) with 7-day timeline and full breathing room.
 */
@androidx.compose.runtime.Composable
private fun WideMultiRowStreakWidget(
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
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .then(clickModifier),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Header Row: App Name + Status Badge
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
                Spacer(modifier = GlanceModifier.defaultWeight())
                StatusBadge(text = appearance.statusText, bg = appearance.badgeBg, textColor = appearance.badgeText, isSmall = false)
            }

            Spacer(modifier = GlanceModifier.height(6.dp))

            // Main Content Row: Left (Flame + Big Streak) | Right (7-Day timeline)
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
                        modifier = GlanceModifier.size(34.dp)
                    )
                    Spacer(modifier = GlanceModifier.width(8.dp))
                    Column {
                        Text(
                            text = "${data.streakDays}",
                            style = TextStyle(
                                color = GlanceTheme.colors.onSurface,
                                fontSize = 26.sp,
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    data.weekDays.forEach { day ->
                        val dotRes = when {
                            day.isCompleted -> R.drawable.ic_widget_dot_done
                            day.isToday -> R.drawable.ic_widget_dot_today_pending
                            else -> R.drawable.ic_widget_dot_pending
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = GlanceModifier.padding(horizontal = 4.dp)
                        ) {
                            Text(
                                text = day.dayName,
                                style = TextStyle(
                                    color = if (day.isToday) GlanceTheme.colors.primary else GlanceTheme.colors.onSurfaceVariant,
                                    fontSize = 10.sp,
                                    fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                            Spacer(modifier = GlanceModifier.height(3.dp))
                            Image(
                                provider = ImageProvider(dotRes),
                                contentDescription = day.dayName,
                                modifier = GlanceModifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun StatusBadge(
    text: String,
    bg: ColorProvider,
    textColor: ColorProvider,
    isSmall: Boolean
) {
    Box(
        modifier = GlanceModifier
            .background(bg)
            .cornerRadius(if (isSmall) 10.dp else 12.dp)
            .padding(
                horizontal = if (isSmall) 6.dp else 8.dp,
                vertical = if (isSmall) 2.dp else 3.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = TextStyle(
                color = textColor,
                fontSize = if (isSmall) 10.sp else 11.sp,
                fontWeight = FontWeight.Bold
            )
        )
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
        else -> WidgetThemeColors.pendingState(data.streakDays)
    }
}

/**
 * Helper container holding theme colors and state text for the 3 dynamic states.
 */
private data class WidgetStateAppearance(
    val bg: ColorProvider,
    val badgeBg: ColorProvider,
    val badgeText: ColorProvider,
    val statusText: String,
    val subtitleText: String
)

private object WidgetThemeColors {

    fun completedState(): WidgetStateAppearance {
        return WidgetStateAppearance(
            bg = ColorProvider(R.color.widget_bg_completed),
            badgeBg = ColorProvider(R.color.widget_badge_bg_completed),
            badgeText = ColorProvider(R.color.widget_badge_text_completed),
            statusText = "Zabezpieczona",
            subtitleText = "Świetna robota na dziś!"
        )
    }

    fun urgentState(): WidgetStateAppearance {
        return WidgetStateAppearance(
            bg = ColorProvider(R.color.widget_bg_urgent),
            badgeBg = ColorProvider(R.color.widget_badge_bg_urgent),
            badgeText = ColorProvider(R.color.widget_badge_text_urgent),
            statusText = "Uratuj serię!",
            subtitleText = "Powtórz słówka przed północą"
        )
    }

    fun pendingState(streakDays: Int): WidgetStateAppearance {
        return WidgetStateAppearance(
            bg = ColorProvider(R.color.widget_bg_pending),
            badgeBg = ColorProvider(R.color.widget_badge_bg_pending),
            badgeText = ColorProvider(R.color.widget_badge_text_pending),
            statusText = if (streakDays > 0) "Do zrobienia" else "Zacznij serię",
            subtitleText = "Kliknij, aby rozpocząć naukę"
        )
    }
}
