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
     * Generates a contextual reminder notification message from a pool of
     * dynamic templates.
     *
     * Selects from templates targeting:
     * 1. Active words currently in study or scheduled in today's lesson
     * 2. Number of review items due in today's session
     * 3. Streak counts (zero days, small streaks 1-6 days, mastery streaks 7+
     *    days)
     * 4. Personalized username greeting (when available)
     * 5. General erudition and rhetorical motivation
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
            candidates.add("Pamiętasz jeszcze, co oznacza „$randomWord”? Sprawdź się w dzisiejszej lekcji!")
            candidates.add("Czy potrafisz poprawnie użyć słowa „$randomWord”? Czas na krótką powtórkę.")
            candidates.add("Słowo „$randomWord” czeka na utrwalenie w Twojej pamięci!")
            candidates.add("Dziś w Twoim planie jest m.in. „$randomWord”. Zobacz, czy pamiętasz jego znaczenie.")
        }

        // 2. Upcoming review count in today's session queue
        if (reviewDueCount > 0) {
            val countFormatted = formatWordCountPlural(reviewDueCount)
            candidates.add("Masz dzisiaj $countFormatted do powtórzenia. Wystarczą 2 minuty!")
            candidates.add("W Twojej kolejce czeka $countFormatted. Sprawdź, ile z nich pamiętasz!")
            candidates.add("Czeka na Ciebie dzisiejsza sesja z $countFormatted. Opanuj je przed końcem dnia!")
        }

        // 3. Streak-based motivational messages
        when {
            streak <= 0 -> {
                candidates.add("Każda wielka seria zaczyna się od pierwszego dnia. Rozpocznij swój streak już dziś!")
                candidates.add("Dziś jest idealny moment na powtórkę. Zbuduj swój codzienny nawyk nauki!")
                candidates.add("Zrób pierwszy krok ku bogatszemu słownictwu – Twoja dzisiejsza sesja czeka!")
            }

            streak in 1..6 -> {
                candidates.add("Świetnie Ci idzie! Masz już $streak dni serii z rzędu – utrzymaj tę passę!")
                candidates.add("Już $streak dni regularnej nauki! Nie pozwól na przerwanie passy – zrób szybką sesję.")
                candidates.add("To Twój $streak. dzień z rzędu! Krótka powtórka i seria trwa dalej.")
            }

            else -> { // streak >= 7
                candidates.add("Imponująca seria: aż $streak dni z rzędu! Twoja erudycja stale rośnie.")
                candidates.add("Mistrzowska dyscyplina! To już $streak dni bez przerw. Czas na dzisiejszą dawkę trudnych słów.")
                candidates.add("Aż $streak dni w serii! Nie zwalniaj tempa – kolejna porcja wiedzy jest gotowa.")
            }
        }

        // 4. Personalized username greetings
        val cleanName = username?.trim()
        if (!cleanName.isNullOrBlank()) {
            candidates.add("Cześć $cleanName! Czas na Twoją codzienną porcję wyrafinowanego języka.")
            candidates.add("$cleanName, Twoje trudne słówka czekają na dzisiejszy trening umysłu.")
            candidates.add("Gotowy na dzisiejsze wyzwanie językowe, $cleanName?")
        }

        // 5. General erudition and rhetoric motivation (always available)
        candidates.add("Chwila dla umysłu – wzbogać swój język o nowe, wyszukane konstrukcje.")
        candidates.add("Systematyczność to klucz do bogatego słownictwa. Gotowy na 3-minutową sesję?")
        candidates.add("Precyzja słowa to potęga myśli. Zobacz dzisiejsze propozycje w Słowniku!")
        candidates.add("Kilka minut nauki dziennie wystarczy, by wypowiadać się z niezwykłą swobodą.")
        candidates.add("Twoje trudne słówka czekają na dzisiejszą powtórkę.")
        candidates.add("Czas na krótką sesję ze Słownikiem Języka Trudnego!")

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
            .setContentTitle("Pora na powtórkę!")
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
}
