package pl.slownikjezykatrudnego.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import pl.slownikjezykatrudnego.app.domain.SuperMemoEngine
import pl.slownikjezykatrudnego.app.ui.account.AccountBottomSheet
import pl.slownikjezykatrudnego.app.ui.catalog.CatalogScreen
import pl.slownikjezykatrudnego.app.ui.common.PrivacyBottomSheet
import pl.slownikjezykatrudnego.app.ui.components.SjtBottomNavBar
import pl.slownikjezykatrudnego.app.ui.components.SjtTab
import pl.slownikjezykatrudnego.app.ui.components.SjtTopBar
import pl.slownikjezykatrudnego.app.ui.lesson.LessonScreen
import pl.slownikjezykatrudnego.app.ui.settings.SettingsBottomSheet
import pl.slownikjezykatrudnego.app.ui.stats.StatsScreen
import pl.slownikjezykatrudnego.app.ui.theme.SjtTheme
import pl.slownikjezykatrudnego.app.ui.theme.SlownikJezykaTrudnegoTheme

/**
 * Root Composable wrapping navigation Scaffold, top bar, bottom bar, screens, and modal sheets.
 */
@Composable
fun SjtApp(
    viewModel: SjtViewModel
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

    val isSettingsOpen by viewModel.isSettingsOpen.collectAsState()
    val isAccountOpen by viewModel.isAccountOpen.collectAsState()
    val isPrivacyOpen by viewModel.isPrivacyOpen.collectAsState()

    val streakDays = remember(progressMap) {
        SuperMemoEngine.calculateStreak(progressMap)
    }

    SlownikJezykaTrudnegoTheme(settings = settings) {
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
                    onToggleTheme = { viewModel.toggleTheme() },
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
                    deviceId = viewModel.getDeviceId(),
                    onDismiss = { viewModel.closeAccount() }
                )
            }

            if (isPrivacyOpen) {
                PrivacyBottomSheet(
                    onDismiss = { viewModel.closePrivacy() }
                )
            }
        }
    }
}
