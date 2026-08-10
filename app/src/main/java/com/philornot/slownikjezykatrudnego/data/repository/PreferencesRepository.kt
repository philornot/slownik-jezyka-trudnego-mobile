package com.philornot.slownikjezykatrudnego.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import com.philornot.slownikjezykatrudnego.data.model.SessionState
import com.philornot.slownikjezykatrudnego.data.model.UserSettings
import com.philornot.slownikjezykatrudnego.data.model.UserWordProgress
import java.util.UUID

/**
 * Local-first persistence repository backed by SharedPreferences with JSON serialization.
 */
class PreferencesRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val _progressMapFlow = MutableStateFlow<Map<String, UserWordProgress>>(emptyMap())
    val progressMapFlow: StateFlow<Map<String, UserWordProgress>> = _progressMapFlow.asStateFlow()

    private val _settingsFlow = MutableStateFlow(UserSettings())
    val settingsFlow: StateFlow<UserSettings> = _settingsFlow.asStateFlow()

    init {
        _progressMapFlow.value = loadProgressMap()
        _settingsFlow.value = loadSettings()
    }

    /**
     * Loads the entire progress map from local storage.
     */
    fun loadProgressMap(): Map<String, UserWordProgress> {
        val raw = prefs.getString(KEY_PROGRESS_MAP, null) ?: return emptyMap()
        return try {
            json.decodeFromString<Map<String, UserWordProgress>>(raw)
        } catch (e: Exception) {
            emptyMap()
        }
    }

    /**
     * Persists the complete progress map to local storage.
     */
    suspend fun saveProgressMap(map: Map<String, UserWordProgress>) = withContext(Dispatchers.IO) {
        try {
            val encoded = json.encodeToString(map)
            prefs.edit().putString(KEY_PROGRESS_MAP, encoded).apply()
            _progressMapFlow.value = map
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Updates and saves progress for a single word.
     */
    suspend fun saveWordProgress(progress: UserWordProgress) {
        val current = _progressMapFlow.value.toMutableMap()
        current[progress.wordId] = progress
        saveProgressMap(current)
    }

    /**
     * Loads user configuration settings from local storage.
     */
    fun loadSettings(): UserSettings {
        val raw = prefs.getString(KEY_USER_SETTINGS, null) ?: return UserSettings()
        return try {
            json.decodeFromString<UserSettings>(raw)
        } catch (e: Exception) {
            UserSettings()
        }
    }

    /**
     * Persists user settings to local storage.
     */
    suspend fun saveSettings(settings: UserSettings) {
        _settingsFlow.value = settings
        withContext(Dispatchers.IO) {
            try {
                val encoded = json.encodeToString(settings)
                prefs.edit().putString(KEY_USER_SETTINGS, encoded).apply()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Loads active session state snapshot if available.
     */
    fun loadSessionState(): SessionState? {
        val raw = prefs.getString(KEY_SESSION_STATE, null) ?: return null
        return try {
            json.decodeFromString<SessionState>(raw)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Persists active session state.
     */
    fun saveSessionState(state: SessionState) {
        try {
            val encoded = json.encodeToString(state)
            prefs.edit().putString(KEY_SESSION_STATE, encoded).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Clears active session state.
     */
    fun clearSessionState() {
        prefs.edit().remove(KEY_SESSION_STATE).apply()
    }

    /**
     * Wipes all learning progress (for the reset progress feature).
     */
    suspend fun clearAllProgress() = withContext(Dispatchers.IO) {
        prefs.edit().remove(KEY_PROGRESS_MAP).remove(KEY_SESSION_STATE).apply()
        _progressMapFlow.value = emptyMap()
    }

    /**
     * Checks if user has already been prompted to enable notifications after completing a lesson.
     */
    fun hasPromptedForNotifications(): Boolean {
        return prefs.getBoolean(KEY_PROMPTED_NOTIFICATIONS, false)
    }

    /**
     * Records that user has been prompted to enable notifications.
     */
    fun setPromptedForNotifications(prompted: Boolean = true) {
        prefs.edit().putBoolean(KEY_PROMPTED_NOTIFICATIONS, prompted).apply()
    }

    /**
     * Retrieves or generates a persistent device UUID.
     */
    fun getDeviceId(): String {
        var id = prefs.getString(KEY_DEVICE_ID, null)
        if (id == null) {
            id = UUID.randomUUID().toString()
            prefs.edit().putString(KEY_DEVICE_ID, id).apply()
        }
        return id
    }

    companion object {
        private const val PREFS_NAME = "sjt_prefs_v1"
        private const val KEY_PROGRESS_MAP = "sjt_user_progress"
        private const val KEY_USER_SETTINGS = "sjt_user_settings"
        private const val KEY_SESSION_STATE = "sjt_session_state"
        private const val KEY_DEVICE_ID = "sjt_device_id"
        private const val KEY_PROMPTED_NOTIFICATIONS = "sjt_prompted_notifications"
    }
}
