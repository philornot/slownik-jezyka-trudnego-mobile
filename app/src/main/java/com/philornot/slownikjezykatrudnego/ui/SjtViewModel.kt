package com.philornot.slownikjezykatrudnego.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.philornot.slownikjezykatrudnego.data.datasource.DictionaryWordsData
import com.philornot.slownikjezykatrudnego.data.model.AuthState
import com.philornot.slownikjezykatrudnego.data.model.DictionaryWord
import com.philornot.slownikjezykatrudnego.data.model.ReviewGrade
import com.philornot.slownikjezykatrudnego.data.model.SessionCard
import com.philornot.slownikjezykatrudnego.data.model.SessionPhase
import com.philornot.slownikjezykatrudnego.data.model.SessionState
import com.philornot.slownikjezykatrudnego.data.model.UserProfile
import com.philornot.slownikjezykatrudnego.data.model.UserSettings
import com.philornot.slownikjezykatrudnego.data.model.UserWordProgress
import com.philornot.slownikjezykatrudnego.data.repository.FirebaseRepository
import com.philornot.slownikjezykatrudnego.data.repository.PreferencesRepository
import com.philornot.slownikjezykatrudnego.domain.SessionManager
import com.philornot.slownikjezykatrudnego.domain.SuperMemoEngine
import com.philornot.slownikjezykatrudnego.notifications.NotificationScheduler
import com.philornot.slownikjezykatrudnego.ui.components.SjtTab
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Main ViewModel managing learning state, SuperMemo SM-2 flow, settings,
 * Firebase Auth, Firestore sync, and navigation.
 *
 * @property repository Local persistence repository (SharedPreferences).
 * @property firebaseRepository Firebase Auth and Firestore repository.
 * @property appContext Application context, used only to (re)schedule the
 *    WorkManager-backed daily reminder notification when settings change.
 *    Deliberately typed as the application context (not an Activity
 *    context) to avoid leaking a UI context from the ViewModel.
 */
class SjtViewModel(
    private val repository: PreferencesRepository,
    private val firebaseRepository: FirebaseRepository,
    private val appContext: Context,
) : ViewModel() {

    val allWords: List<DictionaryWord> = DictionaryWordsData.WORDS

    val progressMap: StateFlow<Map<String, UserWordProgress>> = repository.progressMapFlow
    val settings: StateFlow<UserSettings> = repository.settingsFlow

    // ─────────────────────── Auth State ───────────────────────

    val authState: StateFlow<AuthState> = firebaseRepository.authState

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    // ─────────────────────── Navigation ───────────────────────

    private val _activeTab = MutableStateFlow(SjtTab.LESSON)
    val activeTab: StateFlow<SjtTab> = _activeTab.asStateFlow()

    // ─────────────────────── Session State ───────────────────────

    private val _sessionPhase = MutableStateFlow(SessionPhase.SHOWCASE)
    val sessionPhase: StateFlow<SessionPhase> = _sessionPhase.asStateFlow()

    private val _newWordsToLearn = MutableStateFlow<List<DictionaryWord>>(emptyList())
    val newWordsToLearn: StateFlow<List<DictionaryWord>> = _newWordsToLearn.asStateFlow()

    private val _sessionCards = MutableStateFlow<List<SessionCard>>(emptyList())
    val sessionCards: StateFlow<List<SessionCard>> = _sessionCards.asStateFlow()

    private val _currentCardIndex = MutableStateFlow(0)
    val currentCardIndex: StateFlow<Int> = _currentCardIndex.asStateFlow()

    private val _sessionCompleted = MutableStateFlow(false)
    val sessionCompleted: StateFlow<Boolean> = _sessionCompleted.asStateFlow()

    private val _cardsReviewedInSession = MutableStateFlow(0)
    val cardsReviewedInSession: StateFlow<Int> = _cardsReviewedInSession.asStateFlow()

    private val _completionMessage = MutableStateFlow(SessionManager.getDailyCompletionMessage())
    val completionMessage: StateFlow<SessionManager.CompletionMessageType> =
        _completionMessage.asStateFlow()

    // ─────────────────────── Modals ───────────────────────

    private val _isSettingsOpen = MutableStateFlow(false)
    val isSettingsOpen: StateFlow<Boolean> = _isSettingsOpen.asStateFlow()

    private val _isAccountOpen = MutableStateFlow(false)
    val isAccountOpen: StateFlow<Boolean> = _isAccountOpen.asStateFlow()

    private val _isPrivacyOpen = MutableStateFlow(false)
    val isPrivacyOpen: StateFlow<Boolean> = _isPrivacyOpen.asStateFlow()

    private val _isAuthOpen = MutableStateFlow(false)
    val isAuthOpen: StateFlow<Boolean> = _isAuthOpen.asStateFlow()

    private val _showNotificationPrompt = MutableStateFlow(false)
    val showNotificationPrompt: StateFlow<Boolean> = _showNotificationPrompt.asStateFlow()

    init {
        startSession()
        // Observe auth state changes — load profile on login, clear on logout.
        viewModelScope.launch {
            authState.collect { state ->
                when (state) {
                    is AuthState.Authenticated -> onUserLoggedIn(state.user)
                    is AuthState.Unauthenticated -> {
                        _userProfile.value = null
                    }
                    AuthState.Loading -> {}
                }
            }
        }
    }

    // ─────────────────────── Auth Actions ───────────────────────

    /**
     * Signs in with email and password. On success, merges local and cloud progress.
     *
     * @param email User email.
     * @param password User password.
     * @param onSuccess Called after successful sign-in and sync.
     * @param onError Called with a localized error message on failure.
     */
    fun signInWithEmail(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                firebaseRepository.signInWithEmail(email, password)
                onSuccess()
            } catch (e: Exception) {
                onError(mapFirebaseError(e))
            }
        }
    }

    /**
     * Registers a new account with email and password.
     *
     * @param email New account email.
     * @param password New account password.
     * @param onSuccess Called after successful registration and sync.
     * @param onError Called with a localized error message on failure.
     */
    fun registerWithEmail(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                firebaseRepository.registerWithEmail(email, password)
                onSuccess()
            } catch (e: Exception) {
                onError(mapFirebaseError(e))
            }
        }
    }

    /**
     * Signs in with Google using Credential Manager.
     *
     * @param activityContext Activity context required by Credential Manager.
     * @param webClientId OAuth 2.0 Web Client ID from Firebase Console.
     * @param onSuccess Called after successful sign-in.
     * @param onError Called with a localized error message on failure.
     */
    fun signInWithGoogle(
        activityContext: Context,
        webClientId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                firebaseRepository.signInWithGoogle(activityContext, webClientId)
                onSuccess()
            } catch (e: androidx.credentials.exceptions.GetCredentialCancellationException) {
                // User cancelled — no error message needed
            } catch (e: Exception) {
                onError(mapFirebaseError(e))
            }
        }
    }

    /**
     * Signs the current user out.
     */
    fun signOut() {
        firebaseRepository.signOut()
    }

    /**
     * Sends a password reset email.
     *
     * @param email Email address to send the reset link to.
     * @param onSuccess Called on success.
     * @param onError Called with error message on failure.
     */
    fun sendPasswordResetEmail(email: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                firebaseRepository.sendPasswordResetEmail(email)
                onSuccess()
            } catch (e: Exception) {
                onError(mapFirebaseError(e))
            }
        }
    }

    /**
     * Called when a user successfully logs in.
     * Registers the device, loads the cloud profile, and merges progress.
     */
    private suspend fun onUserLoggedIn(user: FirebaseUser) {
        _isSyncing.value = true
        try {
            // Register current device
            firebaseRepository.registerDeviceSession(user.uid, user.email)

            // Load profile
            _userProfile.value = firebaseRepository.loadUserProfile(user.uid)

            // Load cloud settings (merge with local — cloud wins for cross-device consistency)
            val cloudSettings = firebaseRepository.loadSettingsFromCloud(user.uid)
            if (cloudSettings != null) {
                val merged = cloudSettings.copy(
                    notificationsEnabled = settings.value.notificationsEnabled,
                    isDarkTheme = settings.value.isDarkTheme
                )
                repository.saveSettings(merged)
            }

            // Merge local and cloud progress
            val cloudProgress = firebaseRepository.loadProgressFromCloud(user.uid)
            if (cloudProgress != null) {
                val merged = firebaseRepository.mergeProgressMaps(
                    local = progressMap.value,
                    cloud = cloudProgress
                )
                repository.saveProgressMap(merged)
                // Push the merged result back to cloud
                firebaseRepository.syncProgressToCloud(user.uid, merged)
            } else {
                // First login — push local progress to cloud
                if (progressMap.value.isNotEmpty()) {
                    firebaseRepository.syncProgressToCloud(user.uid, progressMap.value)
                }
            }

            // Restart session with fresh merged data
            startSession(forceNew = true)
        } catch (e: Exception) {
            android.util.Log.w("SjtViewModel", "Post-login sync failed", e)
        } finally {
            _isSyncing.value = false
        }
    }

    /**
     * Saves a new username to Firestore and Firebase Auth profile.
     *
     * @param newUsername New display username.
     * @param onSuccess Called on success.
     * @param onError Called with error message on failure.
     */
    fun saveUsername(newUsername: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val uid = firebaseRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                firebaseRepository.saveUsername(uid, newUsername)
                _userProfile.value = _userProfile.value?.copy(username = newUsername.trim())
                onSuccess()
            } catch (e: Exception) {
                onError("Nie udało się zapisać nazwy użytkownika.")
            }
        }
    }

    /**
     * Logs out all devices by revoking all sessions in Firestore.
     *
     * @param onSuccess Called on success.
     * @param onError Called with error message on failure.
     */
    fun logoutAllDevices(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val uid = firebaseRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                firebaseRepository.logoutAllDevices(uid)
                firebaseRepository.signOut()
                onSuccess()
            } catch (e: Exception) {
                onError("Nie udało się wylogować wszystkich urządzeń.")
            }
        }
    }

    /**
     * Deletes the user account and all associated data.
     *
     * @param onSuccess Called on success.
     * @param onError Called with error message on failure.
     */
    fun deleteAccount(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val uid = firebaseRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                firebaseRepository.deleteUserAccount(uid)
                repository.clearAllProgress()
                onSuccess()
            } catch (e: Exception) {
                onError("Nie udało się usunąć konta.")
            }
        }
    }

    // ─────────────────────── Session ───────────────────────

    /**
     * Initializes the daily study session based on SM-2 due cards and adaptive limits.
     *
     * @param forceNew When true, clears saved session state and starts fresh.
     */
    fun startSession(forceNew: Boolean = false) {
        if (forceNew) {
            repository.clearSessionState()
        }

        val sessionData = SessionManager.createDailySession(
            progressMap = progressMap.value,
            settings = settings.value,
            allWords = allWords
        )

        val cards = sessionData.cards
        _sessionCards.value = cards

        val savedState = if (!forceNew) repository.loadSessionState() else null
        val today = SuperMemoEngine.getTodayDateString()

        if (savedState != null && savedState.date == today) {
            _sessionPhase.value = savedState.sessionPhase
            _currentCardIndex.value = savedState.currentCardIndex.coerceIn(0, maxOf(0, cards.size - 1))
            _cardsReviewedInSession.value = savedState.cardsReviewedInSession
            _sessionCompleted.value = savedState.sessionCompleted
            _newWordsToLearn.value = cards.filter { it.isNew }.map { it.word }
        } else {
            _currentCardIndex.value = 0
            _sessionCompleted.value = cards.isEmpty()
            _cardsReviewedInSession.value = 0

            val newWords = cards.filter { it.isNew }.map { it.word }
            if (newWords.isNotEmpty()) {
                _newWordsToLearn.value = newWords
                _sessionPhase.value = SessionPhase.SHOWCASE
            } else {
                _newWordsToLearn.value = emptyList()
                _sessionPhase.value = SessionPhase.QUIZ
            }

            persistActiveSessionState()
        }
    }

    private fun persistActiveSessionState() {
        repository.saveSessionState(
            SessionState(
                date = SuperMemoEngine.getTodayDateString(),
                sessionPhase = _sessionPhase.value,
                currentCardIndex = _currentCardIndex.value,
                cardsReviewedInSession = _cardsReviewedInSession.value,
                sessionCompleted = _sessionCompleted.value
            )
        )
    }

    /** Advances from SHOWCASE phase to QUIZ phase. */
    fun finishShowcase() {
        _sessionPhase.value = SessionPhase.QUIZ
        persistActiveSessionState()
    }

    /**
     * Grades the current flashcard and advances the session.
     * Triggers a debounced cloud sync if the user is logged in.
     *
     * @param grade The SM-2 review grade (0, 3, 4, or 5).
     */
    fun gradeCard(grade: ReviewGrade) {
        val cards = _sessionCards.value
        val currentIndex = _currentCardIndex.value
        if (currentIndex !in cards.indices) return

        val currentCard = cards[currentIndex]

        val updatedProgress = SuperMemoEngine.calculateSM2(
            wordId = currentCard.word.id,
            grade = grade,
            currentProgress = progressMap.value[currentCard.word.id]
        )

        viewModelScope.launch {
            repository.saveWordProgress(updatedProgress)
            // Schedule debounced cloud sync if logged in
            val uid = firebaseRepository.currentUser?.uid
            if (uid != null) {
                firebaseRepository.scheduleSyncProgress(uid, progressMap.value, viewModelScope)
            }
        }

        _cardsReviewedInSession.value += 1

        val updatedCards = cards.toMutableList()

        if (grade == ReviewGrade.AGAIN) {
            updatedCards.add(
                SessionCard(
                    word = currentCard.word,
                    isNew = false,
                    userProgress = updatedProgress,
                    options = currentCard.options
                )
            )
            _sessionCards.value = updatedCards
        }

        if (currentIndex + 1 < updatedCards.size) {
            _currentCardIndex.value += 1
        } else {
            _sessionCompleted.value = true
            _completionMessage.value = SessionManager.getDailyCompletionMessage()
            if (!repository.hasPromptedForNotifications() && !settings.value.notificationsEnabled) {
                _showNotificationPrompt.value = true
            }
        }

        persistActiveSessionState()
    }

    /** Enables daily reminders and dismisses the notification prompt. */
    fun enableNotifications() {
        saveSettings(settings.value.copy(notificationsEnabled = true))
        repository.setPromptedForNotifications(true)
        _showNotificationPrompt.value = false
    }

    /** Dismisses notification prompt without enabling notifications. */
    fun dismissNotificationPrompt() {
        repository.setPromptedForNotifications(true)
        _showNotificationPrompt.value = false
    }

    /**
     * Saves user settings locally and optionally syncs to cloud.
     *
     * @param newSettings Updated settings object.
     */
    fun saveSettings(newSettings: UserSettings) {
        val oldLimit = settings.value.dailyNewWordsLimit
        val oldSettings = settings.value
        viewModelScope.launch {
            repository.saveSettings(newSettings)
            val uid = firebaseRepository.currentUser?.uid
            if (uid != null) {
                firebaseRepository.saveSettingsToCloud(uid, newSettings)
            }
        }
        if (newSettings.dailyNewWordsLimit != oldLimit) {
            startSession(forceNew = true)
        }
        if (newSettings.notificationsEnabled != oldSettings.notificationsEnabled ||
            newSettings.notificationTimeSlot != oldSettings.notificationTimeSlot
        ) {
            NotificationScheduler.scheduleDailyReminder(appContext, newSettings)
        }
    }

    /** Toggles between dark and light theme. */
    fun toggleTheme() {
        val current = settings.value.isDarkTheme ?: false
        saveSettings(settings.value.copy(isDarkTheme = !current))
    }

    /**
     * Resets all learning progress (local and cloud if logged in).
     */
    fun resetProgress() {
        viewModelScope.launch {
            val uid = firebaseRepository.currentUser?.uid
            if (uid != null) {
                firebaseRepository.clearProgressInCloud(uid)
            }
            repository.clearAllProgress()
            startSession(forceNew = true)
        }
    }

    fun setActiveTab(tab: SjtTab) { _activeTab.value = tab }

    fun openSettings() { _isSettingsOpen.value = true }
    fun closeSettings() { _isSettingsOpen.value = false }

    fun openAccount() { _isAccountOpen.value = true }
    fun closeAccount() { _isAccountOpen.value = false }

    fun openPrivacy() { _isPrivacyOpen.value = true }
    fun closePrivacy() { _isPrivacyOpen.value = false }

    fun openAuth() { _isAuthOpen.value = true }
    fun closeAuth() { _isAuthOpen.value = false }

    fun getDeviceId(): String = repository.getDeviceId()

    // ─────────────────────── Helpers ───────────────────────

    /**
     * Maps Firebase error codes to user-friendly Polish messages.
     *
     * @param e Exception from Firebase SDK.
     * @return Localized error message string.
     */
    private fun mapFirebaseError(e: Exception): String {
        val message = e.message ?: ""
        return when {
            "INVALID_LOGIN_CREDENTIALS" in message ||
                    "invalid-credential" in message -> "Nieprawidłowy email lub hasło."
            "EMAIL_EXISTS" in message ||
                    "email-already-in-use" in message -> "Ten adres e-mail jest już używany."
            "WEAK_PASSWORD" in message ||
                    "weak-password" in message -> "Hasło musi mieć co najmniej 6 znaków."
            "INVALID_EMAIL" in message ||
                    "invalid-email" in message -> "Nieprawidłowy format adresu e-mail."
            "USER_NOT_FOUND" in message ||
                    "user-not-found" in message -> "Nie znaleziono konta z tym adresem e-mail."
            "TOO_MANY_ATTEMPTS_TRY_LATER" in message ||
                    "too-many-requests" in message -> "Zbyt wiele prób. Spróbuj ponownie za chwilę."
            "NETWORK_ERROR" in message ||
                    "network-request-failed" in message -> "Brak połączenia z internetem."
            else -> "Wystąpił błąd. Spróbuj ponownie."
        }
    }
}
