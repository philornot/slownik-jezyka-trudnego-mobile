package com.philornot.slownikjezykatrudnego.data.model

/**
 * Represents a registered device session in the user's Firestore profile.
 *
 * @property id       Persistent device UUID generated locally.
 * @property name     Human-readable device label (e.g. "Pixel 9 Pro (Android 15)").
 * @property lastActive ISO 8601 timestamp of the last session activity.
 * @property createdAt  ISO 8601 timestamp when the device first logged in.
 * @property isCurrent  True if this entry represents the current device.
 */
data class DeviceSession(
    val id: String,
    val name: String,
    val lastActive: String,
    val createdAt: String,
    val isCurrent: Boolean = false
)
