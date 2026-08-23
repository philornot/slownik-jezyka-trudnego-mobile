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
 * Supports responsive layouts (Compact 2x2 and Wide 4x2) and Dynamic Color.
 */
class StreakWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(
            SMALL_SIZE,
            WIDE_SIZE
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
        val SMALL_SIZE = DpSize(120.dp, 100.dp)
        val WIDE_SIZE = DpSize(240.dp, 100.dp)
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

    if (size.width >= 230.dp) {
        WideStreakWidget(data = data, clickModifier = clickModifier)
    } else {
        CompactStreakWidget(data = data, clickModifier = clickModifier)
    }
}

/**
 * Compact 2x2 square tile layout.
 */
@androidx.compose.runtime.Composable
private fun CompactStreakWidget(
    data: StreakWidgetData,
    clickModifier: GlanceModifier
) {
    val flameRes = when {
        data.streakDays == 0 -> R.drawable.ic_widget_flame_muted
        data.isUrgentEvening -> R.drawable.ic_widget_flame_urgent
        else -> R.drawable.ic_widget_flame
    }

    val appearance = when {
        data.isCompletedToday -> WidgetThemeColors.completedState()
        data.isUrgentEvening -> WidgetThemeColors.urgentState()
        else -> WidgetThemeColors.pendingState(data.streakDays)
    }

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
                Box(
                    modifier = GlanceModifier
                        .background(appearance.badgeBg)
                        .cornerRadius(12.dp)
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = appearance.statusText,
                        style = TextStyle(
                            color = appearance.badgeText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
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
 * Wide 4x2 banner layout with 7-day progress timeline and quick CTA.
 */
@androidx.compose.runtime.Composable
private fun WideStreakWidget(
    data: StreakWidgetData,
    clickModifier: GlanceModifier
) {
    val flameRes = when {
        data.streakDays == 0 -> R.drawable.ic_widget_flame_muted
        data.isUrgentEvening -> R.drawable.ic_widget_flame_urgent
        else -> R.drawable.ic_widget_flame
    }

    val appearance = when {
        data.isCompletedToday -> WidgetThemeColors.completedState()
        data.isUrgentEvening -> WidgetThemeColors.urgentState()
        else -> WidgetThemeColors.pendingState(data.streakDays)
    }

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
                Box(
                    modifier = GlanceModifier
                        .background(appearance.badgeBg)
                        .cornerRadius(10.dp)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = appearance.statusText,
                        style = TextStyle(
                            color = appearance.badgeText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = GlanceModifier.height(6.dp))

            // Main Content Row: Left (Flame + Streak) | Middle (7 Days) | Right (Action Button)
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Column: Flame and big streak count
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        provider = ImageProvider(flameRes),
                        contentDescription = "Płomień serii",
                        modifier = GlanceModifier.size(36.dp)
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

                // Middle: 7-Day timeline
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
                            modifier = GlanceModifier.padding(horizontal = 3.dp)
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

                Spacer(modifier = GlanceModifier.defaultWeight())

                // Right: Action Button
                val buttonText = if (data.isCompletedToday) "Otwórz" else "Ucz się"
                Box(
                    modifier = GlanceModifier
                        .background(GlanceTheme.colors.primary)
                        .cornerRadius(14.dp)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = buttonText,
                        style = TextStyle(
                            color = GlanceTheme.colors.onPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
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
