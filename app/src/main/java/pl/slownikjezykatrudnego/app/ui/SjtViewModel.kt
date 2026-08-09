package pl.slownikjezykatrudnego.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pl.slownikjezykatrudnego.app.data.datasource.DictionaryWordsData
import pl.slownikjezykatrudnego.app.data.model.DictionaryWord
import pl.slownikjezykatrudnego.app.data.model.ReviewGrade
import pl.slownikjezykatrudnego.app.data.model.SessionCard
import pl.slownikjezykatrudnego.app.data.model.SessionPhase
import pl.slownikjezykatrudnego.app.data.model.SessionState
import pl.slownikjezykatrudnego.app.data.model.UserSettings
import pl.slownikjezykatrudnego.app.data.model.UserWordProgress
import pl.slownikjezykatrudnego.app.data.repository.PreferencesRepository
import pl.slownikjezykatrudnego.app.domain.SessionManager
import pl.slownikjezykatrudnego.app.domain.SuperMemoEngine
import pl.slownikjezykatrudnego.app.ui.components.SjtTab

/**
 * Main ViewModel managing learning state, SuperMemo SM-2 flow, settings, and navigation.
 */
class SjtViewModel(
    private val repository: PreferencesRepository
) : ViewModel() {

    val allWords: List<DictionaryWord> = DictionaryWordsData.WORDS

    val progressMap: StateFlow<Map<String, UserWordProgress>> = repository.progressMapFlow
    val settings: StateFlow<UserSettings> = repository.settingsFlow

    private val _activeTab = MutableStateFlow(SjtTab.LESSON)
    val activeTab: StateFlow<SjtTab> = _activeTab.asStateFlow()

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
    val completionMessage: StateFlow<SessionManager.CompletionMessage> = _completionMessage.asStateFlow()

    // Modals visibility
    private val _isSettingsOpen = MutableStateFlow(false)
    val isSettingsOpen: StateFlow<Boolean> = _isSettingsOpen.asStateFlow()

    private val _isAccountOpen = MutableStateFlow(false)
    val isAccountOpen: StateFlow<Boolean> = _isAccountOpen.asStateFlow()

    private val _isPrivacyOpen = MutableStateFlow(false)
    val isPrivacyOpen: StateFlow<Boolean> = _isPrivacyOpen.asStateFlow()

    init {
        startSession()
    }

    /**
     * Initializes the daily study session based on SM-2 due cards and adaptive limits.
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

    fun finishShowcase() {
        _sessionPhase.value = SessionPhase.QUIZ
        persistActiveSessionState()
    }

    fun gradeCard(grade: ReviewGrade) {
        val cards = _sessionCards.value
        val currentIndex = _currentCardIndex.value
        if (currentIndex !in cards.indices) return

        val currentCard = cards[currentIndex]

        // 1. Calculate updated SM-2 parameters
        val updatedProgress = SuperMemoEngine.calculateSM2(
            wordId = currentCard.word.id,
            grade = grade,
            currentProgress = progressMap.value[currentCard.word.id]
        )

        // 2. Persist locally
        viewModelScope.launch {
            repository.saveWordProgress(updatedProgress)
        }

        _cardsReviewedInSession.value += 1

        val updatedCards = cards.toMutableList()

        // If grade is AGAIN (0), re-queue the card at the end of the current session
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

        // Advance to next card or complete session
        if (currentIndex + 1 < updatedCards.size) {
            _currentCardIndex.value += 1
        } else {
            _sessionCompleted.value = true
            _completionMessage.value = SessionManager.getDailyCompletionMessage()
        }

        persistActiveSessionState()
    }

    fun saveSettings(newSettings: UserSettings) {
        val oldLimit = settings.value.dailyNewWordsLimit
        viewModelScope.launch {
            repository.saveSettings(newSettings)
        }
        if (newSettings.dailyNewWordsLimit != oldLimit) {
            startSession(forceNew = true)
        }
    }

    fun toggleTheme() {
        val current = settings.value.isDarkTheme ?: false
        saveSettings(settings.value.copy(isDarkTheme = !current))
    }

    fun resetProgress() {
        viewModelScope.launch {
            repository.clearAllProgress()
            startSession(forceNew = true)
        }
    }

    fun setActiveTab(tab: SjtTab) {
        _activeTab.value = tab
    }

    fun openSettings() { _isSettingsOpen.value = true }
    fun closeSettings() { _isSettingsOpen.value = false }

    fun openAccount() { _isAccountOpen.value = true }
    fun closeAccount() { _isAccountOpen.value = false }

    fun openPrivacy() { _isPrivacyOpen.value = true }
    fun closePrivacy() { _isPrivacyOpen.value = false }

    fun getDeviceId(): String = repository.getDeviceId()
}
