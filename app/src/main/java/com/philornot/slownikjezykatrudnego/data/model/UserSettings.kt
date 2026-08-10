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
 * User configuration and accessibility preferences.
 *
 * @property preferredNotificationHour Hour of day (0-23) for daily study reminders.
 * @property notificationsEnabled Whether notifications are enabled.
 * @property dailyNewWordsLimit Target number of new words to introduce daily (default 3).
 * @property isDarkTheme Theme preference (true = dark, false = light, null = follow system).
 * @property highContrast High contrast mode for enhanced readability.
 * @property reducedMotion Reduces and disables non-essential UI animations.
 * @property textSize Font scaling level (SMALL = 100%, MEDIUM = 112.5%, LARGE = 125%).
 */
@OptIn(InternalSerializationApi::class)
@Serializable
data class UserSettings(
    val preferredNotificationHour: Int = 9,
    val notificationsEnabled: Boolean = false,
    val dailyNewWordsLimit: Int = 3,
    val isDarkTheme: Boolean? = null,
    val highContrast: Boolean = false,
    val reducedMotion: Boolean = false,
    val textSize: TextSizeLevel = TextSizeLevel.SMALL
)
