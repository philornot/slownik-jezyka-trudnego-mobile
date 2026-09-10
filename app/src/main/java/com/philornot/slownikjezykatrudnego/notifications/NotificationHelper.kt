package com.philornot.slownikjezykatrudnego.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.philornot.slownikjezykatrudnego.MainActivity
import com.philornot.slownikjezykatrudnego.R
import com.philornot.slownikjezykatrudnego.data.datasource.DictionaryWordsData
import com.philornot.slownikjezykatrudnego.data.repository.PreferencesRepository
import com.philornot.slownikjezykatrudnego.domain.SessionManager
import com.philornot.slownikjezykatrudnego.domain.SuperMemoEngine

/**
 * Builds and displays daily study-reminder notifications with rich
 * contextual messages.
 */
object NotificationHelper {

    const val CHANNEL_ID = "sjt_daily_reminder_channel"
    private const val NOTIFICATION_ID = 1001
    private const val STREAK_SAVER_NOTIFICATION_ID = 1002

    /**
     * Formats Polish grammatical pluralization for the word count.
     *
     * @param count Number of words.
     * @return Formatted string with appropriate Polish noun inflection (e.g.
     *    "1 słówko", "3 słówka", "5 słówek").
     */
    fun formatWordCountPlural(count: Int): String {
        return when {
            count == 1 -> "1 słówko"
            count % 10 in 2..4 && count % 100 !in 12..14 -> "$count słówka"
            else -> "$count słówek"
        }
    }

    /**
     * Generates a random friendly notification title for daily reminders.
     *
     * @param context Application context.
     * @return Selected notification title.
     */
    fun generateReminderTitle(context: Context): String {
        val titles = context.resources.getStringArray(R.array.notification_reminder_titles)
        return titles.random()
    }

    /**
     * Generates a random friendly notification title for streak saver reminders.
     *
     * @param context Application context.
     * @return Selected notification title.
     */
    fun generateStreakSaverTitle(context: Context): String {
        val titles = context.resources.getStringArray(R.array.notification_streak_saver_titles)
        return titles.random()
    }

    /**
     * Generates an urgent yet friendly streak protection notification text.
     *
     * @param context Application context.
     * @param streak Current study streak in consecutive days.
     * @param username Display name of the user if set.
     * @return Selected streak saver notification text.
     */
    fun generateStreakSaverText(context: Context, streak: Int, username: String?): String {
        val cleanName = username?.trim()
        val hasName = !cleanName.isNullOrBlank()
        
        val candidates = mutableListOf<String>()
        val noNameTexts = context.resources.getStringArray(R.array.notification_streak_saver_texts_no_name)
        noNameTexts.forEach { candidates.add(String.format(it, streak)) }

        if (hasName) {
            val withNameTexts = context.resources.getStringArray(R.array.notification_streak_saver_texts_with_name)
            withNameTexts.forEach { candidates.add(String.format(it, streak, cleanName)) }
        }

        return candidates.random()
    }

    /**
     * Checks if today's study session is already completed.
     *
     * @param context Application context.
     * @return True if today's lesson is completed or has no remaining cards.
     */
    fun isTodaySessionCompleted(context: Context): Boolean {
        val repository = PreferencesRepository(context)
        val progressMap = repository.loadProgressMap()
        val settings = repository.loadSettings()
        val today = SuperMemoEngine.getTodayDateString()

        val savedState = repository.loadSessionState()
        if (savedState != null && savedState.date == today && savedState.sessionCompleted) {
            return true
        }

        val session = SessionManager.createDailySession(
            progressMap = progressMap,
            settings = settings,
            allWords = DictionaryWordsData.WORDS,
            todayStr = today
        )

        if (session.cards.isEmpty()) {
            return true
        }

        if (savedState != null && savedState.date == today) {
            return savedState.currentCardIndex >= session.cards.size
        }

        return false
    }

    /**
     * Generates a contextual reminder notification message from a pool of
     * dynamic templates.
     *
     * @param context Application context.
     * @param streak Current study streak in consecutive days.
     * @param sessionWords Words appearing in the upcoming daily lesson cards.
     * @param reviewDueCount Number of words remaining in the session.
     * @param username Display name of the user if set.
     * @return Selected notification text.
     */
    fun generateReminderText(
        context: Context,
        streak: Int,
        sessionWords: List<String>,
        reviewDueCount: Int,
        username: String?,
    ): String {
        val candidates = mutableListOf<String>()

        // 1. Actively learned word recall
        val validSessionWords = sessionWords.filter { it.isNotBlank() }.distinct()
        if (validSessionWords.isNotEmpty()) {
            val randomWord = validSessionWords.random()
            val wordTexts = context.resources.getStringArray(R.array.notification_reminder_texts_words)
            wordTexts.forEach { candidates.add(String.format(it, randomWord)) }
        }

        // 2. Upcoming review count
        if (reviewDueCount > 0) {
            val countFormatted = formatWordCountPlural(reviewDueCount)
            val reviewTexts = context.resources.getStringArray(R.array.notification_reminder_texts_review_count)
            reviewTexts.forEach { candidates.add(String.format(it, countFormatted)) }
        }

        // 3. Streak-based motivational messages
        when {
            streak <= 0 -> {
                val texts = context.resources.getStringArray(R.array.notification_reminder_texts_streak_0)
                texts.forEach { candidates.add(it) }
            }
            streak in 1..6 -> {
                val texts = context.resources.getStringArray(R.array.notification_reminder_texts_streak_1_6)
                texts.forEach { candidates.add(String.format(it, streak)) }
            }
            else -> { // streak >= 7
                val texts = context.resources.getStringArray(R.array.notification_reminder_texts_streak_7)
                texts.forEach { candidates.add(String.format(it, streak)) }
            }
        }

        // 4. Personalized username greetings
        val cleanName = username?.trim()
        if (!cleanName.isNullOrBlank()) {
            val nameTexts = context.resources.getStringArray(R.array.notification_reminder_texts_username)
            nameTexts.forEach { candidates.add(String.format(it, cleanName)) }
        }

        // 5. General encouragement
        val generalTexts = context.resources.getStringArray(R.array.notification_reminder_texts_general)
        generalTexts.forEach { candidates.add(it) }

        return candidates.random()
    }

    /**
     * Creates the notification channel. Safe to call repeatedly; no-op if it
     * already exists.
     */
    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Codzienne przypomnienia",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Przypomnienie o codziennej powtórce trudnych słówek"
        }

        val manager = context.getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(channel)
    }

    /**
     * Builds and shows the reminder notification with dynamic contextual copy.
     *
     * Caller is responsible for having verified the POST_NOTIFICATIONS
     * permission is granted (required on API 33+) before invoking this.
     */
    fun showDailyReminder(context: Context) {
        ensureChannel(context)

        val repository = PreferencesRepository(context)
        val settings = repository.loadSettings()
        val progressMap = repository.loadProgressMap()
        val streak = SuperMemoEngine.calculateStreak(progressMap)

        val session = SessionManager.createDailySession(
            progressMap = progressMap,
            settings = settings,
            allWords = DictionaryWordsData.WORDS
        )

        val savedState = repository.loadSessionState()
        val today = SuperMemoEngine.getTodayDateString()
        val remainingCards = if (savedState != null && savedState.date == today) {
            if (savedState.sessionCompleted) {
                emptyList()
            } else {
                session.cards.drop(savedState.currentCardIndex.coerceIn(0, session.cards.size))
            }
        } else {
            session.cards
        }

        val sessionWords = remainingCards.map { it.word.word }

        val username = repository.getCachedUsername() ?: try {
            com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.displayName
        } catch (_: Throwable) {
            null
        }

        val title = generateReminderTitle(context)
        val body = generateReminderText(
            context = context,
            streak = streak,
            sessionWords = sessionWords,
            reviewDueCount = remainingCards.size,
            username = username
        )

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_monochrome)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            android.util.Log.w(
                "NotificationHelper",
                "POST_NOTIFICATIONS not granted, skipping reminder",
                e
            )
        }
    }

    /**
     * Builds and shows the streak saver notification.
     *
     * Caller is responsible for having verified the POST_NOTIFICATIONS
     * permission is granted before invoking this.
     */
    fun showStreakSaverReminder(context: Context) {
        ensureChannel(context)

        val repository = PreferencesRepository(context)
        val progressMap = repository.loadProgressMap()
        val streak = SuperMemoEngine.calculateStreak(progressMap)

        if (streak <= 0) return

        val username = repository.getCachedUsername() ?: try {
            com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.displayName
        } catch (_: Throwable) {
            null
        }

        val title = generateStreakSaverTitle(context)
        val body = generateStreakSaverText(context = context, streak = streak, username = username)

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_monochrome)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(STREAK_SAVER_NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            android.util.Log.w(
                "NotificationHelper",
                "POST_NOTIFICATIONS not granted, skipping streak saver reminder",
                e
            )
        }
    }
}
