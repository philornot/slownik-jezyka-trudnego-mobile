package com.philornot.slownikjezykatrudnego.data.model

/**
 * Represents the user's cloud profile stored in Firestore.
 *
 * @property uid              Firebase Auth UID.
 * @property email            User email address (nullable).
 * @property username         Display username set by the user (nullable).
 * @property devices          Map of device ID → DeviceSession for cross-device tracking.
 * @property sessionRevokedAt ISO 8601 timestamp of last "logout all devices" action (nullable).
 * @property updatedAt        ISO 8601 timestamp of last Firestore document update.
 */
data class UserProfile(
    val uid: String,
    val email: String? = null,
    val username: String? = null,
    val devices: Map<String, DeviceSession> = emptyMap(),
    val sessionRevokedAt: String? = null,
    val updatedAt: String? = null
)
