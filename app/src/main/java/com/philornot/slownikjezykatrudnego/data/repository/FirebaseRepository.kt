package com.philornot.slownikjezykatrudnego.data.repository

import android.content.Context
import android.os.Build
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.philornot.slownikjezykatrudnego.data.model.AuthState
import com.philornot.slownikjezykatrudnego.data.model.DeviceSession
import com.philornot.slownikjezykatrudnego.data.model.ReviewHistoryItem
import com.philornot.slownikjezykatrudnego.data.model.UserProfile
import com.philornot.slownikjezykatrudnego.data.model.UserSettings
import com.philornot.slownikjezykatrudnego.data.model.UserWordProgress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Repository encapsulating all Firebase Authentication and Firestore operations.
 * Mirrors the web version's `storage.ts` functionality for a consistent cross-platform experience.
 *
 * @property context Application context (for Credential Manager).
 * @property preferencesRepository Local persistence repository for device ID and settings.
 */
class FirebaseRepository(
    private val context: Context,
    private val preferencesRepository: PreferencesRepository
) {

    private val auth: FirebaseAuth? = try {
        Firebase.auth
    } catch (e: Exception) {
        android.util.Log.e("FirebaseRepository", "Firebase Auth not available", e)
        null
    }

    private val db = try {
        Firebase.firestore
    } catch (e: Exception) {
        android.util.Log.e("FirebaseRepository", "Firestore not available", e)
        null
    }

    // ─────────────────────── Auth State ───────────────────────

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    /** The currently authenticated Firebase user (null if not logged in or Firebase not available). */
    val currentUser: FirebaseUser? get() = auth?.currentUser

    init {
        // Listen to Firebase Auth state changes and update our state flow.
        auth?.addAuthStateListener { firebaseAuth ->
            _authState.value = when (val user = firebaseAuth.currentUser) {
                null -> AuthState.Unauthenticated
                else -> AuthState.Authenticated(user)
            }
        } ?: run {
            _authState.value = AuthState.Unauthenticated
        }
    }

    // ─────────────────────── Authentication ───────────────────────

    /**
     * Signs in with email and password.
     *
     * @param email User email address.
     * @param password User password.
     * @return The authenticated [FirebaseUser] on success.
     * @throws Exception on Firebase Auth error.
     */
    suspend fun signInWithEmail(email: String, password: String): FirebaseUser {
        val auth = auth ?: throw Exception("Usługa Firebase nie jest dostępna.")
        val result = auth.signInWithEmailAndPassword(email, password).await()
        return result.user ?: throw Exception("Logowanie nie powiodło się.")
    }

    /**
     * Registers a new user with email and password.
     *
     * @param email New user's email address.
     * @param password New user's password.
     * @return The newly created [FirebaseUser].
     * @throws Exception on Firebase Auth error.
     */
    suspend fun registerWithEmail(email: String, password: String): FirebaseUser {
        val auth = auth ?: throw Exception("Usługa Firebase nie jest dostępna.")
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        return result.user ?: throw Exception("Rejestracja nie powiodła się.")
    }

    /**
     * Signs in with Google using the Credential Manager API (Android 14+).
     * Falls back gracefully on older API levels.
     *
     * @param activityContext Activity context required by Credential Manager.
     * @param webClientId     OAuth 2.0 Web Client ID from Firebase Console.
     * @return The authenticated [FirebaseUser] on success.
     * @throws GetCredentialCancellationException if the user cancelled the dialog.
     * @throws Exception on any other error.
     */
    suspend fun signInWithGoogle(activityContext: Context, webClientId: String): FirebaseUser {
        android.util.Log.d("SjtAuth", "Rozpoczynanie logowania Google. WebClientId: $webClientId")
        
        val auth = auth ?: run {
            android.util.Log.e("SjtAuth", "FirebaseAuth jest null!")
            throw Exception("Usługa Firebase nie jest dostępna.")
        }
        
        val credentialManager = CredentialManager.create(activityContext)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        try {
            android.util.Log.d("SjtAuth", "Wywołanie credentialManager.getCredential...")
            val credentialResponse = credentialManager.getCredential(
                request = request,
                context = activityContext
            )

            val credential = credentialResponse.credential
            android.util.Log.d("SjtAuth", "Otrzymano credential typu: ${credential.type}")

            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                android.util.Log.d("SjtAuth", "Token ID odebrany pomyślnie.")
                
                val firebaseCredential = GoogleAuthProvider.getCredential(
                    googleIdTokenCredential.idToken, null
                )
                
                android.util.Log.d("SjtAuth", "Logowanie do Firebase za pomocą credentiali...")
                val result = auth.signInWithCredential(firebaseCredential).await()
                
                android.util.Log.d("SjtAuth", "Logowanie Firebase zakończone sukcesem: ${result.user?.uid}")
                return result.user ?: throw Exception("Google Sign-In nie powiódł się.")
            }

            android.util.Log.e("SjtAuth", "Nieoczekiwany typ credential: ${credential.type}")
            throw Exception("Nieoczekiwany typ danych uwierzytelniających.")
        } catch (e: GetCredentialException) {
            android.util.Log.e("SjtAuth", "Błąd Credential Manager: ${e.message}", e)
            throw Exception("Błąd autoryzacji Google: ${e.message}")
        } catch (e: Exception) {
            android.util.Log.e("SjtAuth", "Nieoczekiwany błąd podczas logowania Google", e)
            throw e
        }
    }

    /**
     * Signs the current user out of Firebase Auth.
     */
    fun signOut() {
        auth?.signOut()
    }

    /**
     * Sends a password reset email to the given address.
     *
     * @param email Email address to send the reset link to.
     */
    suspend fun sendPasswordResetEmail(email: String) {
        val auth = auth ?: throw Exception("Usługa Firebase nie jest dostępna.")
        auth.sendPasswordResetEmail(email).await()
    }

    // ─────────────────────── Firestore: Progress ───────────────────────

    private var syncJob: Job? = null

    /**
     * Schedules a debounced sync of the local progress map to Firestore (2.5 second delay).
     * Identical to the web version's `syncProgressToCloud` debounce pattern.
     *
     * @param userId      Firebase UID of the authenticated user.
     * @param progressMap The complete word progress map to sync.
     * @param scope       CoroutineScope in which to schedule the debounce.
     */
    fun scheduleSyncProgress(
        userId: String,
        progressMap: Map<String, UserWordProgress>,
        scope: CoroutineScope
    ) {
        syncJob?.cancel()
        syncJob = scope.launch {
            delay(2500)
            syncProgressToCloud(userId, progressMap)
        }
    }

    /**
     * Immediately flushes any pending progress sync to Firestore.
     *
     * @param userId      Firebase UID.
     * @param progressMap Word progress map to persist.
     */
    suspend fun syncProgressToCloud(
        userId: String,
        progressMap: Map<String, UserWordProgress>
    ) = withContext(Dispatchers.IO) {
        val db = db ?: return@withContext
        try {
            val serializable = progressMap.mapValues { (_, p) ->
                mapOf(
                    "wordId" to p.wordId,
                    "repetitions" to p.repetitions,
                    "easeFactor" to p.easeFactor,
                    "interval" to p.interval,
                    "nextReviewDate" to p.nextReviewDate,
                    "lastReviewedAt" to p.lastReviewedAt,
                    "history" to p.history.map { h ->
                        mapOf("date" to h.date, "grade" to h.grade)
                    }
                )
            }
            db.collection("users").document(userId)
                .set(
                    mapOf(
                        "progressMap" to serializable,
                        "updatedAt" to java.time.Instant.now().toString()
                    ),
                    com.google.firebase.firestore.SetOptions.merge()
                ).await()
        } catch (e: Exception) {
            // Non-fatal — local progress is still preserved.
            android.util.Log.w("FirebaseRepository", "Failed to sync progress to cloud", e)
        }
    }

    /**
     * Loads the user's word progress map from Firestore.
     *
     * @param userId Firebase UID.
     * @return Progress map or null if empty/unavailable.
     */
    suspend fun loadProgressFromCloud(userId: String): Map<String, UserWordProgress>? =
        withContext(Dispatchers.IO) {
            val db = db ?: return@withContext null
            try {
                val snap = db.collection("users").document(userId).get().await()
                if (snap.exists()) {
                    @Suppress("UNCHECKED_CAST")
                    val rawMap = snap.get("progressMap") as? Map<String, Map<String, Any>>
                        ?: return@withContext null
                    return@withContext rawMap.mapValues { (_, v) ->
                        @Suppress("UNCHECKED_CAST")
                        val historyRaw = v["history"] as? List<Map<String, Any>> ?: emptyList()
                        UserWordProgress(
                            wordId = v["wordId"] as? String ?: "",
                            repetitions = (v["repetitions"] as? Number)?.toInt() ?: 0,
                            easeFactor = (v["easeFactor"] as? Number)?.toDouble() ?: 2.5,
                            interval = (v["interval"] as? Number)?.toInt() ?: 0,
                            nextReviewDate = v["nextReviewDate"] as? String ?: "",
                            lastReviewedAt = v["lastReviewedAt"] as? String ?: "",
                            history = historyRaw.map { h ->
                                ReviewHistoryItem(
                                    date = h["date"] as? String ?: "",
                                    grade = (h["grade"] as? Number)?.toInt() ?: 0
                                )
                            }
                        )
                    }
                }
            } catch (e: Exception) {
                android.util.Log.w("FirebaseRepository", "Failed to load progress from cloud", e)
            }
            null
        }

    /**
     * Merges local and cloud progress maps using "latest timestamp wins" logic.
     * Identical to the web version's `mergeProgressMaps` function in storage.ts.
     *
     * @param local Local progress map from SharedPreferences.
     * @param cloud Cloud progress map from Firestore.
     * @return Merged map where each word's most recent review wins.
     */
    fun mergeProgressMaps(
        local: Map<String, UserWordProgress>,
        cloud: Map<String, UserWordProgress>
    ): Map<String, UserWordProgress> {
        val allIds = local.keys + cloud.keys
        return allIds.associateWith { wordId ->
            val localEntry = local[wordId]
            val cloudEntry = cloud[wordId]
            when {
                localEntry != null && cloudEntry != null -> {
                    val localTime = localEntry.lastReviewedAt
                    val cloudTime = cloudEntry.lastReviewedAt
                    if (cloudTime > localTime) cloudEntry else localEntry
                }
                localEntry != null -> localEntry
                else -> cloudEntry!!
            }
        }
    }

    // ─────────────────────── Firestore: Settings ───────────────────────

    /**
     * Saves user settings to Firestore (excludes device-specific settings like notifications).
     *
     * @param userId   Firebase UID.
     * @param settings User settings to persist.
     */
    suspend fun saveSettingsToCloud(userId: String, settings: UserSettings) =
        withContext(Dispatchers.IO) {
            val db = db ?: return@withContext
            try {
                db.collection("users").document(userId)
                    .set(
                        mapOf(
                            "settings" to mapOf(
                                "dailyNewWordsLimit" to settings.dailyNewWordsLimit,
                                "highContrast" to settings.highContrast,
                                "reducedMotion" to settings.reducedMotion,
                                "textSize" to settings.textSize.name.lowercase()
                            ),
                            "updatedAt" to java.time.Instant.now().toString()
                        ),
                        com.google.firebase.firestore.SetOptions.merge()
                    ).await()
            } catch (e: Exception) {
                android.util.Log.w("FirebaseRepository", "Failed to save settings to cloud", e)
            }
        }

    /**
     * Loads user settings from Firestore.
     *
     * @param userId Firebase UID.
     * @return UserSettings or null if not found.
     */
    suspend fun loadSettingsFromCloud(userId: String): UserSettings? = withContext(Dispatchers.IO) {
        val db = db ?: return@withContext null
        try {
            val snap = db.collection("users").document(userId).get().await()
            if (snap.exists()) {
                @Suppress("UNCHECKED_CAST")
                val raw = snap.get("settings") as? Map<String, Any> ?: return@withContext null
                val textSizeStr = raw["textSize"] as? String ?: "small"
                val textSize = com.philornot.slownikjezykatrudnego.data.model.TextSizeLevel.entries
                    .firstOrNull { it.name.equals(textSizeStr, ignoreCase = true) }
                    ?: com.philornot.slownikjezykatrudnego.data.model.TextSizeLevel.SMALL
                return@withContext UserSettings(
                    dailyNewWordsLimit = (raw["dailyNewWordsLimit"] as? Number)?.toInt() ?: 5,
                    highContrast = raw["highContrast"] as? Boolean ?: false,
                    reducedMotion = raw["reducedMotion"] as? Boolean ?: false,
                    textSize = textSize
                )
            }
        } catch (e: Exception) {
            android.util.Log.w("FirebaseRepository", "Failed to load settings from cloud", e)
        }
        null
    }

    // ─────────────────────── Firestore: Profile & Devices ───────────────────────

    /**
     * Registers the current device session in the user's Firestore document.
     * Creates or refreshes the device entry with current timestamp.
     *
     * @param userId    Firebase UID.
     * @param userEmail Optional user email for display purposes.
     * @return The registered [DeviceSession] or null on failure.
     */
    suspend fun registerDeviceSession(
        userId: String,
        userEmail: String? = null
    ): DeviceSession? = withContext(Dispatchers.IO) {
        val db = db ?: return@withContext null
        try {
            val deviceId = preferencesRepository.getDeviceId()
            val deviceName = getDeviceInfo()
            val now = java.time.Instant.now().toString()

            val snap = db.collection("users").document(userId).get().await()
            @Suppress("UNCHECKED_CAST")
            val existingDevices = (snap.get("devices") as? Map<String, Map<String, Any>>)
                ?: emptyMap()

            val createdAt = (existingDevices[deviceId]?.get("createdAt") as? String) ?: now

            val deviceData = mapOf(
                "id" to deviceId,
                "name" to deviceName,
                "lastActive" to now,
                "createdAt" to createdAt
            )

            val updatedDevices = existingDevices.toMutableMap()
            updatedDevices[deviceId] = deviceData

            val payload = mutableMapOf<String, Any>(
                "devices" to updatedDevices,
                "updatedAt" to now
            )
            if (userEmail != null) payload["email"] = userEmail

            db.collection("users").document(userId)
                .set(payload, com.google.firebase.firestore.SetOptions.merge()).await()

            DeviceSession(
                id = deviceId,
                name = deviceName,
                lastActive = now,
                createdAt = createdAt,
                isCurrent = true
            )
        } catch (e: Exception) {
            android.util.Log.w("FirebaseRepository", "Failed to register device session", e)
            null
        }
    }

    /**
     * Loads the user's full profile from Firestore, including device list.
     *
     * @param userId Firebase UID.
     * @return [UserProfile] or null if not found.
     */
    suspend fun loadUserProfile(userId: String): UserProfile? = withContext(Dispatchers.IO) {
        val db = db ?: return@withContext null
        try {
            val snap = db.collection("users").document(userId).get().await()
            if (snap.exists()) {
                val data = snap.data ?: return@withContext null
                val currentDeviceId = preferencesRepository.getDeviceId()
                @Suppress("UNCHECKED_CAST")
                val devicesRaw = data["devices"] as? Map<String, Map<String, Any>> ?: emptyMap()
                val devices = devicesRaw.mapValues { (id, v) ->
                    DeviceSession(
                        id = id,
                        name = v["name"] as? String ?: "Urządzenie",
                        lastActive = v["lastActive"] as? String ?: "",
                        createdAt = v["createdAt"] as? String ?: "",
                        isCurrent = id == currentDeviceId
                    )
                }
                return@withContext UserProfile(
                    uid = userId,
                    email = data["email"] as? String,
                    username = (data["username"] ?: data["displayName"]) as? String,
                    devices = devices,
                    sessionRevokedAt = data["sessionRevokedAt"] as? String,
                    updatedAt = data["updatedAt"] as? String
                )
            }
        } catch (e: Exception) {
            android.util.Log.w("FirebaseRepository", "Failed to load user profile", e)
        }
        null
    }

    /**
     * Saves or updates the username in both Firestore and Firebase Auth profile.
     *
     * @param userId      Firebase UID.
     * @param newUsername New display username.
     */
    suspend fun saveUsername(userId: String, newUsername: String) = withContext(Dispatchers.IO) {
        val db = db ?: return@withContext
        val auth = auth ?: return@withContext
        val trimmed = newUsername.trim()
        db.collection("users").document(userId)
            .set(
                mapOf(
                    "username" to trimmed,
                    "displayName" to trimmed,
                    "updatedAt" to java.time.Instant.now().toString()
                ),
                com.google.firebase.firestore.SetOptions.merge()
            ).await()

        auth.currentUser?.updateProfile(
            com.google.firebase.auth.UserProfileChangeRequest.Builder()
                .setDisplayName(trimmed)
                .build()
        )?.await()
    }

    /**
     * Revokes all device sessions by clearing the devices map and setting a revocation timestamp.
     *
     * @param userId Firebase UID.
     */
    suspend fun logoutAllDevices(userId: String) = withContext(Dispatchers.IO) {
        val db = db ?: return@withContext
        val now = java.time.Instant.now().toString()
        db.collection("users").document(userId)
            .set(
                mapOf(
                    "devices" to emptyMap<String, Any>(),
                    "sessionRevokedAt" to now,
                    "updatedAt" to now
                ),
                com.google.firebase.firestore.SetOptions.merge()
            ).await()
    }

    /**
     * Permanently deletes the user's Firestore data and Firebase Auth account.
     *
     * @param userId Firebase UID.
     */
    suspend fun deleteUserAccount(userId: String) = withContext(Dispatchers.IO) {
        db?.collection("users")?.document(userId)?.delete()?.await()
        auth?.currentUser?.delete()?.await()
    }

    /**
     * Resets all progress in Firestore for the given user.
     *
     * @param userId Firebase UID.
     */
    suspend fun clearProgressInCloud(userId: String) = withContext(Dispatchers.IO) {
        val db = db ?: return@withContext
        try {
            db.collection("users").document(userId)
                .set(
                    mapOf(
                        "progressMap" to emptyMap<String, Any>(),
                        "updatedAt" to java.time.Instant.now().toString()
                    ),
                    com.google.firebase.firestore.SetOptions.merge()
                ).await()
        } catch (e: Exception) {
            android.util.Log.w("FirebaseRepository", "Failed to clear progress in cloud", e)
        }
    }

    // ─────────────────────── Helpers ───────────────────────

    /**
     * Returns a human-readable label for the current Android device.
     *
     * @return Device label string, e.g. "Pixel 9 Pro (Android 15)".
     */
    private fun getDeviceInfo(): String {
        val manufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercase() }
        val model = Build.MODEL
        val apiLevel = Build.VERSION.SDK_INT
        val displayModel = if (model.startsWith(manufacturer, ignoreCase = true)) model else "$manufacturer $model"
        return "$displayModel (Android API $apiLevel)"
    }
}
