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
     * @return Selected notification title.
     */
    fun generateReminderTitle(): String {
        val titles = listOf(
            "Pora na lekcję!",
            "Słownik Języka Trudnego",
            "Twoja codzienna lekcja",
            "Czas na słówka!"
        )
        return titles.random()
    }

    /**
     * Generates a random friendly notification title for streak saver reminders.
     *
     * @return Selected notification title.
     */
    fun generateStreakSaverTitle(): String {
        val titles = listOf(
            "Uratuj swoją serię!",
            "Twoja seria jest zagrożona!",
            "Nie trać serii!",
            "Szybka lekcja przed końcem dnia?"
        )
        return titles.random()
    }

    /**
     * Generates an urgent yet friendly streak protection notification text.
     *
     * @param streak Current study streak in consecutive days.
     * @param username Display name of the user if set.
     * @return Selected streak saver notification text.
     */
    fun generateStreakSaverText(streak: Int, username: String?): String {
        val candidates = mutableListOf<String>()

        candidates.add("Masz serię $streak dni! Zrób szybką lekcję przed północą, żeby jej nie stracić.")
        candidates.add("Szkoda byłoby przerwać passę $streak dni. Wpadnij na 2 minuty przed końcem dnia!")
        candidates.add("Dzień powoli się kończy, a Twoja seria ($streak dni) czeka na podtrzymanie.")
        candidates.add("Zostało jeszcze trochę czasu. Krótka powtórka i Twoja seria $streak dni jest bezpieczna!")
        candidates.add("Tylko jedna krótka lekcja dzieli Cię od uratowania serii $streak dni!")

        val cleanName = username?.trim()
        if (!cleanName.isNullOrBlank()) {
            candidates.add("$cleanName, Twoja seria $streak dni czeka na uratowanie! Wystarczą 2 minuty.")
            candidates.add("Hej $cleanName! Nie pozwól przepaść serii $streak dni. Zrób szybką powtórkę.")
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
     * Selects from templates targeting:
     * 1. Active words currently in study or scheduled in today's lesson
     * 2. Number of review items due in today's session
     * 3. Streak counts (zero days, small streaks 1-6 days, mastery streaks 7+
     *    days)
     * 4. Personalized username greeting (when available)
     * 5. General erudition and rhetoric motivation
     *
     * @param streak Current study streak in consecutive days.
     * @param sessionWords Words appearing in the upcoming daily lesson cards.
     * @param reviewDueCount Number of words remaining in the session.
     * @param username Display name of the user if set.
     * @return Selected notification text.
     */
    fun generateReminderText(
        streak: Int,
        sessionWords: List<String>,
        reviewDueCount: Int,
        username: String?,
    ): String {
        val candidates = mutableListOf<String>()

        // 1. Actively learned word recall (strictly from words appearing in today's lesson)
        val validSessionWords = sessionWords.filter { it.isNotBlank() }.distinct()
        if (validSessionWords.isNotEmpty()) {
            val randomWord = validSessionWords.random()
            candidates.add("Co dokładnie znaczy „$randomWord”? Otwórz lekcję i przypomnij sobie!")
            candidates.add("„$randomWord” - użyjesz tego słowa w rozmowie? Sprawdź w dzisiejszej lekcji.")
            candidates.add("Dziś w lekcji pojawi się „$randomWord”. Pamiętasz, co to znaczy?")
            candidates.add("„$randomWord” wraca w dzisiejszej lekcji. Idealna okazja, żeby je utrwalić.")
        }

        // 2. Upcoming review count in today's session queue
        if (reviewDueCount > 0) {
            val countFormatted = formatWordCountPlural(reviewDueCount)
            candidates.add("$countFormatted na dziś. Szybka lekcja i masz to z głowy!")
            candidates.add("Tylko $countFormatted do powtórki. Wpadnij na szybką lekcję!")
            candidates.add("Dzisiejsza lekcja: $countFormatted. Dasz radę w kilka minut!")
        }

        // 3. Streak-based motivational messages
        when {
            streak <= 0 -> {
                candidates.add("Czas zacząć nową serię! Jedna lekcja to wszystko, czego potrzebujesz.")
                candidates.add("Idealny moment na szybką lekcję. Zacznij serię od dziś!")
                candidates.add("Nowe słówka czekają. Otwórz lekcję i zacznij budować serię!")
            }

            streak in 1..6 -> {
                candidates.add("Już $streak dni z rzędu! Otwórz lekcję i przedłuż swoją serię.")
                candidates.add("$streak dni serii, tak trzymaj! Dzisiejsza lekcja podtrzyma Twoje tempo.")
                candidates.add("Twoja seria: $streak dni! Szybka lekcja i leci dalej.")
            }

            else -> { // streak >= 7
                candidates.add("$streak dni z rzędu, brawo! Nie zatrzymuj się teraz.")
                candidates.add("Seria $streak dni! To robi wrażenie. Otwórz dzisiejszą lekcję!")
                candidates.add("Wow, $streak dni bez przerwy! Dzisiejsza lekcja już na Ciebie czeka.")
            }
        }

        // 4. Personalized username greetings
        val cleanName = username?.trim()
        if (!cleanName.isNullOrBlank()) {
            candidates.add("Hej $cleanName! Twoja dzisiejsza lekcja jest gotowa.")
            candidates.add("$cleanName, masz dziś nowe słówka do odkrycia!")
            candidates.add("Cześć $cleanName! Wpadnij na szybką lekcję?")
        }

        // 5. General encouragement and habit motivation (always available)
        candidates.add("Masz minutę? Twoja lekcja jest gotowa.")
        candidates.add("Nowe słówka do nauki. Otwórz lekcję, kiedy masz chwilę!")
        candidates.add("Krótka lekcja teraz to mniejsza powtórka jutro!")
        candidates.add("Codziennie trochę, za miesiąc dużo. Czas na lekcję!")
        candidates.add("Twoje słówka na dziś są przygotowane. Wpadnij na lekcję!")
        candidates.add("Szybka lekcja przed dalszym dniem?")

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

        val title = generateReminderTitle()
        val body = generateReminderText(
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

        val title = generateStreakSaverTitle()
        val body = generateStreakSaverText(streak = streak, username = username)

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
