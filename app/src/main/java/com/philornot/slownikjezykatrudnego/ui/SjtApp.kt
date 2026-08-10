package com.philornot.slownikjezykatrudnego.ui

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import com.philornot.slownikjezykatrudnego.domain.SuperMemoEngine
import com.philornot.slownikjezykatrudnego.ui.account.AccountBottomSheet
import com.philornot.slownikjezykatrudnego.ui.account.AuthBottomSheet
import com.philornot.slownikjezykatrudnego.ui.catalog.CatalogScreen
import com.philornot.slownikjezykatrudnego.ui.common.PrivacyBottomSheet
import com.philornot.slownikjezykatrudnego.ui.components.SjtBottomNavBar
import com.philornot.slownikjezykatrudnego.ui.components.SjtTab
import com.philornot.slownikjezykatrudnego.ui.components.SjtTopBar
import com.philornot.slownikjezykatrudnego.ui.lesson.LessonScreen
import com.philornot.slownikjezykatrudnego.ui.settings.SettingsBottomSheet
import com.philornot.slownikjezykatrudnego.ui.stats.StatsScreen
import com.philornot.slownikjezykatrudnego.ui.theme.CircularRevealThemeWrapper
import com.philornot.slownikjezykatrudnego.ui.theme.RevealOrigin
import com.philornot.slownikjezykatrudnego.ui.theme.SjtTheme
import com.philornot.slownikjezykatrudnego.ui.theme.SlownikJezykaTrudnegoTheme
import com.philornot.slownikjezykatrudnego.ui.theme.animateThemeReveal
import com.philornot.slownikjezykatrudnego.ui.theme.rememberThemeTransitionState
import kotlinx.coroutines.launch

// Web Client ID from Firebase Console (OAuth 2.0 → Web Application Client ID)
// TODO: Replace with your actual Web Client ID from Firebase Console
private const val GOOGLE_WEB_CLIENT_ID =
    "YOUR_WEB_CLIENT_ID_FROM_FIREBASE_CONSOLE.apps.googleusercontent.com"

/**
 * Root Composable wrapping navigation Scaffold, top bar, bottom bar, screens, and modal sheets.
 * Includes circular reveal animation for theme transitions (matching the web version).
 *
 * @param viewModel The main ViewModel.
 * @param activity  The Activity context (required for Google Sign-In via Credential Manager).
 */
@Composable
fun SjtApp(
    viewModel: SjtViewModel,
    activity: Activity
) {
    val settings by viewModel.settings.collectAsState()
    val activeTab by viewModel.activeTab.collectAsState()
    val progressMap by viewModel.progressMap.collectAsState()
    val sessionCompleted by viewModel.sessionCompleted.collectAsState()
    val sessionPhase by viewModel.sessionPhase.collectAsState()
    val newWordsToLearn by viewModel.newWordsToLearn.collectAsState()
    val sessionCards by viewModel.sessionCards.collectAsState()
    val currentCardIndex by viewModel.currentCardIndex.collectAsState()
    val cardsReviewedCount by viewModel.cardsReviewedInSession.collectAsState()
    val completionMessage by viewModel.completionMessage.collectAsState()
    val authState by viewModel.authState.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()

    val isSettingsOpen by viewModel.isSettingsOpen.collectAsState()
    val isAccountOpen by viewModel.isAccountOpen.collectAsState()
    val isPrivacyOpen by viewModel.isPrivacyOpen.collectAsState()
    val isAuthOpen by viewModel.isAuthOpen.collectAsState()

    val streakDays = remember(progressMap) {
        SuperMemoEngine.calculateStreak(progressMap)
    }

    // ─── Circular Reveal Theme Transition State ───
    val themeTransitionState = rememberThemeTransitionState()
    val coroutineScope = rememberCoroutineScope()
    var pendingThemeToggle by remember { mutableStateOf(false) }
    var revealOrigin by remember { mutableStateOf<RevealOrigin?>(null) }

    // We need to render both old and new theme content for the circular reveal.
    // The transition state drives which version is visible.
    CircularRevealThemeWrapper(
        state = themeTransitionState,
        skipAnimation = settings.reducedMotion,
        newContent = {
            // New theme content (revealed by expanding circle)
            SlownikJezykaTrudnegoTheme(settings = settings) {
                SjtAppScaffold(
                    viewModel = viewModel,
                    activity = activity,
                    settings = settings,
                    activeTab = activeTab,
                    progressMap = progressMap,
                    sessionCompleted = sessionCompleted,
                    sessionPhase = sessionPhase,
                    newWordsToLearn = newWordsToLearn,
                    sessionCards = sessionCards,
                    currentCardIndex = currentCardIndex,
                    cardsReviewedCount = cardsReviewedCount,
                    completionMessage = completionMessage,
                    authState = authState,
                    userProfile = userProfile,
                    isSyncing = isSyncing,
                    streakDays = streakDays,
                    isSettingsOpen = isSettingsOpen,
                    isAccountOpen = isAccountOpen,
                    isPrivacyOpen = isPrivacyOpen,
                    isAuthOpen = isAuthOpen,
                    onToggleTheme = { offset ->
                        revealOrigin = offset?.let { RevealOrigin(it.x, it.y) }
                        coroutineScope.launch {
                            animateThemeReveal(
                                state = themeTransitionState,
                                origin = revealOrigin,
                                skipAnimation = settings.reducedMotion,
                                onStart = {
                                    viewModel.toggleTheme()
                                }
                            )
                        }
                    }
                )
            }
        },
        oldContent = {
            // Old theme content — shown underneath while circle expands
            SlownikJezykaTrudnegoTheme(settings = settings) {
                SjtAppScaffold(
                    viewModel = viewModel,
                    activity = activity,
                    settings = settings,
                    activeTab = activeTab,
                    progressMap = progressMap,
                    sessionCompleted = sessionCompleted,
                    sessionPhase = sessionPhase,
                    newWordsToLearn = newWordsToLearn,
                    sessionCards = sessionCards,
                    currentCardIndex = currentCardIndex,
                    cardsReviewedCount = cardsReviewedCount,
                    completionMessage = completionMessage,
                    authState = authState,
                    userProfile = userProfile,
                    isSyncing = isSyncing,
                    streakDays = streakDays,
                    isSettingsOpen = isSettingsOpen,
                    isAccountOpen = isAccountOpen,
                    isPrivacyOpen = isPrivacyOpen,
                    isAuthOpen = isAuthOpen,
                    onToggleTheme = null // Clicks disabled during transition
                )
            }
        }
    )
}

/**
 * Internal scaffold composable used for both old and new theme layers in the circular reveal.
 *
 * @param onToggleTheme Callback for theme toggle with optional offset; null = disabled.
 */
@Composable
private fun SjtAppScaffold(
    viewModel: SjtViewModel,
    activity: Activity,
    settings: com.philornot.slownikjezykatrudnego.data.model.UserSettings,
    activeTab: SjtTab,
    progressMap: Map<String, com.philornot.slownikjezykatrudnego.data.model.UserWordProgress>,
    sessionCompleted: Boolean,
    sessionPhase: com.philornot.slownikjezykatrudnego.data.model.SessionPhase,
    newWordsToLearn: List<com.philornot.slownikjezykatrudnego.data.model.DictionaryWord>,
    sessionCards: List<com.philornot.slownikjezykatrudnego.data.model.SessionCard>,
    currentCardIndex: Int,
    cardsReviewedCount: Int,
    completionMessage: com.philornot.slownikjezykatrudnego.domain.SessionManager.CompletionMessage,
    authState: com.philornot.slownikjezykatrudnego.data.model.AuthState,
    userProfile: com.philornot.slownikjezykatrudnego.data.model.UserProfile?,
    isSyncing: Boolean,
    streakDays: Int,
    isSettingsOpen: Boolean,
    isAccountOpen: Boolean,
    isPrivacyOpen: Boolean,
    isAuthOpen: Boolean,
    onToggleTheme: ((Offset?) -> Unit)?
) {
    val colors = SjtTheme.colors

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgApp),
        containerColor = colors.bgApp,
        topBar = {
            SjtTopBar(
                streakDays = streakDays,
                isDarkTheme = colors.isDark,
                onToggleTheme = onToggleTheme,
                onOpenSettings = { viewModel.openSettings() },
                onOpenAccount = { viewModel.openAccount() }
            )
        },
        bottomBar = {
            SjtBottomNavBar(
                currentTab = activeTab,
                onTabSelected = { viewModel.setActiveTab(it) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(colors.bgApp)
        ) {
            when (activeTab) {
                SjtTab.LESSON -> {
                    LessonScreen(
                        sessionCompleted = sessionCompleted,
                        sessionPhase = sessionPhase,
                        newWordsToLearn = newWordsToLearn,
                        sessionCards = sessionCards,
                        currentCardIndex = currentCardIndex,
                        cardsReviewedCount = cardsReviewedCount,
                        streakDays = streakDays,
                        completionMessage = completionMessage,
                        onFinishShowcase = { viewModel.finishShowcase() },
                        onGradeCard = { viewModel.gradeCard(it) },
                        onNavigateCatalog = { viewModel.setActiveTab(SjtTab.CATALOG) },
                        onNavigateStats = { viewModel.setActiveTab(SjtTab.STATS) }
                    )
                }
                SjtTab.CATALOG -> {
                    CatalogScreen(
                        words = viewModel.allWords,
                        progressMap = progressMap
                    )
                }
                SjtTab.STATS -> {
                    StatsScreen(
                        words = viewModel.allWords,
                        progressMap = progressMap,
                        streakDays = streakDays
                    )
                }
            }
        }

        // Bottom Sheets
        if (isSettingsOpen) {
            SettingsBottomSheet(
                settings = settings,
                onSaveSettings = { viewModel.saveSettings(it) },
                onResetProgress = { viewModel.resetProgress() },
                onOpenPrivacy = {
                    viewModel.closeSettings()
                    viewModel.openPrivacy()
                },
                onDismiss = { viewModel.closeSettings() }
            )
        }

        if (isAccountOpen) {
            AccountBottomSheet(
                authState = authState,
                userProfile = userProfile,
                isSyncing = isSyncing,
                currentDeviceId = viewModel.getDeviceId(),
                onOpenAuth = {
                    viewModel.closeAccount()
                    viewModel.openAuth()
                },
                onSignOut = {
                    viewModel.signOut()
                    viewModel.closeAccount()
                },
                onSaveUsername = { username, onSuccess, onError ->
                    viewModel.saveUsername(username, onSuccess, onError)
                },
                onLogoutAllDevices = { onSuccess, onError ->
                    viewModel.logoutAllDevices(onSuccess, onError)
                },
                onDeleteAccount = { onSuccess, onError ->
                    viewModel.deleteAccount(onSuccess, onError)
                },
                onDismiss = { viewModel.closeAccount() }
            )
        }

        if (isAuthOpen) {
            AuthBottomSheet(
                onSignInWithEmail = { email, password, onError ->
                    viewModel.signInWithEmail(email, password, {
                        viewModel.closeAuth()
                    }, onError)
                },
                onRegisterWithEmail = { email, password, onError ->
                    viewModel.registerWithEmail(email, password, {
                        viewModel.closeAuth()
                    }, onError)
                },
                onSignInWithGoogle = { onError ->
                    viewModel.signInWithGoogle(activity, GOOGLE_WEB_CLIENT_ID, {
                        viewModel.closeAuth()
                    }, onError)
                },
                onOpenPrivacy = {
                    viewModel.closeAuth()
                    viewModel.openPrivacy()
                },
                onDismiss = { viewModel.closeAuth() }
            )
        }

        if (isPrivacyOpen) {
            PrivacyBottomSheet(
                onDismiss = { viewModel.closePrivacy() }
            )
        }
    }
}
