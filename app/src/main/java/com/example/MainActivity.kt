package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.core.content.ContextCompat
import com.example.ads.AdMobBannerView
import com.example.ads.AdMobManager
import com.example.model.PersonaType
import com.example.notification.DailyNotificationManager
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.ApexLifeTheme
import com.example.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Create Android Notification Channel for daily rewards
        DailyNotificationManager.createNotificationChannel(this)

        // Initialize Google Mobile Ads SDK (AdMob) with 120s interstitial timer and rewarded ads
        AdMobManager.initialize(this)

        setContent {
            val state by viewModel.uiState.collectAsState()
            var showDailyRewardDialog by remember { mutableStateOf(false) }
            var showFlexCardDialog by remember { mutableStateOf(false) }
            var showBackupDialog by remember { mutableStateOf(false) }
            var lockedModeTarget by remember { mutableStateOf<PersonaType?>(null) }
            val isDailyRewardAvailable = viewModel.isDailyRewardAvailable()

            // Notification permission request for Android 13+
            val notificationPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { /* granted */ }

            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }

            ApexLifeTheme(
                accent = state.themeAccent,
                darkTheme = state.isDarkTheme
            ) {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("main_screen_scaffold"),
                    topBar = {
                        Column {
                            TopAppHUD(
                                state = state,
                                isRewardAvailable = isDailyRewardAvailable,
                                onOpenDailyReward = { showDailyRewardDialog = true },
                                onWatchRewardedAd = { viewModel.watchRewardedAdForCoin(this@MainActivity) },
                                onToggleSound = { viewModel.toggleSound() },
                                onToggleHaptics = { viewModel.toggleHaptics() },
                                onToggleDarkMode = { viewModel.toggleDarkMode() },
                                onOpenBackup = { showBackupDialog = true },
                                onOpenFlexCard = { showFlexCardDialog = true }
                            )
                            PersonaSelectorBar(
                                state = state,
                                currentPersona = state.currentPersona,
                                onSelectPersona = { newPersona ->
                                    viewModel.openModeWithRewardedAd(this@MainActivity, newPersona)
                                },
                                onLockedModeClicked = { lockedPersona ->
                                    lockedModeTarget = lockedPersona
                                }
                            )
                        }
                    },
                    bottomBar = {
                        // Banner Ad unit placed at the bottom of the app
                        AdMobBannerView(
                            modifier = Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding()
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (state.currentPersona) {
                            PersonaType.STUDENT -> StudentScreen(state = state, viewModel = viewModel)
                            PersonaType.BUSINESS -> BusinessScreen(state = state, viewModel = viewModel)
                            PersonaType.ATHLETE -> AthleteScreen(state = state, viewModel = viewModel)
                            PersonaType.LOOKSMAX -> LooksmaxScreen(state = state, viewModel = viewModel)
                            PersonaType.MALA_JAAP -> MalaJaapScreen(state = state, viewModel = viewModel)
                            PersonaType.GAMES -> GamesScreen(state = state, viewModel = viewModel)
                            PersonaType.LEADERBOARD -> LeaderboardScreen(state = state, viewModel = viewModel)
                            PersonaType.PROFILE -> ProfileScreen(state = state, viewModel = viewModel)
                        }
                    }

                    // Modals
                    if (lockedModeTarget != null) {
                        ModeLockedDialog(
                            persona = lockedModeTarget!!,
                            currentCoins = state.coins,
                            onUnlockWithCoins = {
                                viewModel.unlockModeWithCoins(lockedModeTarget!!)
                                viewModel.setPersona(lockedModeTarget!!)
                                lockedModeTarget = null
                            },
                            onNavigateToGames = {
                                viewModel.setPersona(PersonaType.GAMES)
                                lockedModeTarget = null
                            },
                            onWatchAdForCoins = {
                                viewModel.watchRewardedAdForCoin(this@MainActivity)
                            },
                            onUnlockWithAdPass = {
                                viewModel.unlockModeWithRewardedAdPass(this@MainActivity, lockedModeTarget!!)
                                lockedModeTarget = null
                            },
                            onDismiss = { lockedModeTarget = null }
                        )
                    }

                    if (showDailyRewardDialog) {
                        DailyRewardDialog(
                            streakDays = state.streakDays,
                            isRewardAvailable = isDailyRewardAvailable,
                            onClaimReward = { viewModel.claimDailyReward() },
                            onDismiss = { showDailyRewardDialog = false }
                        )
                    }

                    if (showFlexCardDialog) {
                        FlexCardDialog(
                            state = state,
                            onDismiss = { showFlexCardDialog = false }
                        )
                    }

                    if (showBackupDialog) {
                        BackupRestoreDialog(
                            exportJsonString = viewModel.getExportJson(),
                            onImportJson = { viewModel.importJson(it) },
                            onResetBaseline = { viewModel.resetToZeroBaseline() },
                            onDismiss = { showBackupDialog = false }
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // App open ads on resume disabled to maintain 120s clean interstitial schedule and prevent ad loops
    }


    override fun onDestroy() {
        super.onDestroy()
        AdMobManager.stopInterstitialTimer()
    }
}
