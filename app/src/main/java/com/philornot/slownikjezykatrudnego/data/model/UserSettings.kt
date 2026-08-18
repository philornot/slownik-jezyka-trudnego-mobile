package com.philornot.slownikjezykatrudnego.data.model

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable

/**
 * Text size level for accessibility support.
 */
@OptIn(InternalSerializationApi::class)
@Serializable
enum class TextSizeLevel {
    SMALL,
    MEDIUM,
    LARGE
}

/**
 * Broad part of the day the user prefers to receive their daily study
 * reminder in. The exact minute the notification fires is randomized
 * within the slot's [startHour, endHour) window on each day, so the
 * reminder doesn't arrive at a predictable, easy-to-ignore time.
 *
 * @property startHour Inclusive start of the window (24h format).
 * @property endHour Exclusive end of the window (24h format).
 * @property label Human-readable Polish label shown in settings UI.
 */
@OptIn(InternalSerializationApi::class)
@Serializable
enum class NotificationTimeSlot(val startHour: Int, val endHour: Int, val label: String) {
    MORNING(startHour = 7, endHour = 11, label = "Rano"),
    DAYTIME(startHour = 11, endHour = 17, label = "W ciągu dnia"),
    EVENING(startHour = 17, endHour = 21, label = "Wieczorem")
}

/**
 * User configuration and accessibility preferences.
 *
 * @property notificationTimeSlot Part of the day in which the daily
 *    reminder should fire at a random time.
 * @property notificationsEnabled Whether notifications are enabled.
 * @property dailyNewWordsLimit Target number of new words to introduce
 *    daily (default 3).
 * @property isDarkTheme Theme preference (true = dark, false = light, null
 *    = follow system).
 * @property highContrast High contrast mode for enhanced readability.
 * @property reducedMotion Reduces and disables non-essential UI
 *    animations.
 * @property textSize Font scaling level (SMALL = 100%, MEDIUM = 118%,
 *    LARGE = 136%).
 */
@OptIn(InternalSerializationApi::class)
@Serializable
data class UserSettings(
    val notificationTimeSlot: NotificationTimeSlot = NotificationTimeSlot.MORNING,
    val notificationsEnabled: Boolean = false,
    val dailyNewWordsLimit: Int = 3,
    val isDarkTheme: Boolean? = null,
    val highContrast: Boolean = false,
    val reducedMotion: Boolean = false,
    val textSize: TextSizeLevel = TextSizeLevel.SMALL,
)