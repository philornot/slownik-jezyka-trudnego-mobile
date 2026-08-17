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

/**
 * Builds and shows the daily study-reminder notification, and owns its
 * notification channel.
 */
object NotificationHelper {

    const val CHANNEL_ID = "sjt_daily_reminder_channel"
    private const val NOTIFICATION_ID = 1001

    private val FALLBACK_MESSAGES = listOf(
        "Twoje trudne słówka czekają na dzisiejszą powtórkę.",
        "Kilka minut nauki utrwali dzisiejsze słówka na dłużej.",
        "Czas na krótką sesję ze Słownikiem Języka Trudnego!",
        "Nie przerywaj passy — dzisiejsza powtórka czeka."
    )

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
     * Builds and shows the reminder notification. Picks a random dictionary
     * word to give each notification a little variety, falling back to
     * a generic message if the dictionary is empty for some reason.
     *
     * Caller is responsible for having verified the POST_NOTIFICATIONS
     * permission is granted (required on API 33+) before invoking this.
     */
    fun showDailyReminder(context: Context) {
        ensureChannel(context)

        val randomWord = DictionaryWordsData.WORDS.randomOrNull()
        val body = randomWord?.let { "Sprawdź, czy pamiętasz jeszcze: \"${it.word}\"" }
            ?: FALLBACK_MESSAGES.random()

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

        // NotificationManagerCompat.notify() itself checks the POST_NOTIFICATIONS permission on
        // API 33+ and throws SecurityException if missing — guard defensively in case settings
        // and the OS permission state have drifted apart (e.g. user revoked it in system settings).
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
