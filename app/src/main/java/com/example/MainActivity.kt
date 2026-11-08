package com.example

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.SeraBottomNav
import com.example.ui.components.SyncResultsDialog
import com.example.ui.screens.AddEditTitleScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.RouletteScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TitleDetailDialog
import com.example.ui.screens.WelcomeScreen
import com.example.ui.theme.SeraAkisosTheme
import com.example.util.NotificationHelper
import com.example.viewmodel.NavDestination
import com.example.viewmodel.SeraAkisosViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: SeraAkisosViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationHelper.createNotificationChannel(this)
        enableEdgeToEdge()
        setContent {
            val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()

            SeraAkisosTheme(darkTheme = isDarkTheme) {
                SeraAkisosApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun SeraAkisosApp(viewModel: SeraAkisosViewModel) {
    val currentNav by viewModel.currentNav.collectAsStateWithLifecycle()
    val selectedTitle by viewModel.selectedTitle.collectAsStateWithLifecycle()
    val syncResults by viewModel.syncResults.collectAsStateWithLifecycle()
    val hasSeenWelcome by viewModel.hasSeenWelcome.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Request notification permission on Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.setNotificationsEnabled(isGranted)
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Collect snackbar messages
    LaunchedEffect(viewModel) {
        viewModel.messageEvents.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    // Se o usuário ainda não viu ou reabriu a Apresentação Inicial, exibe WelcomeScreen com atalho .zip e botão Pular
    if (!hasSeenWelcome) {
        Box(modifier = Modifier.fillMaxSize()) {
            WelcomeScreen(
                viewModel = viewModel,
                onDismiss = { viewModel.dismissWelcome() }
            )
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(androidx.compose.ui.Alignment.BottomCenter)
            )
        }
        return
    }

    // Android System Back Handling
    BackHandler(enabled = syncResults != null || selectedTitle != null || currentNav != NavDestination.HOME) {
        if (syncResults != null) {
            viewModel.dismissSyncResults()
        } else if (selectedTitle != null) {
            viewModel.closeTitleDetails()
        } else if (currentNav != NavDestination.HOME) {
            viewModel.navigateTo(NavDestination.HOME)
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentWindowInsets = WindowInsets.statusBars,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            SeraBottomNav(
                currentNav = currentNav,
                onNavigate = { destination ->
                    if (destination == NavDestination.ADD && currentNav != NavDestination.ADD) {
                        viewModel.startNewTitleForm()
                    }
                    viewModel.navigateTo(destination)
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (currentNav) {
                NavDestination.HOME -> HomeScreen(viewModel = viewModel)
                NavDestination.LIBRARY -> LibraryScreen(viewModel = viewModel)
                NavDestination.ROULETTE -> RouletteScreen(viewModel = viewModel)
                NavDestination.ADD -> AddEditTitleScreen(viewModel = viewModel)
                NavDestination.SETTINGS -> SettingsScreen(viewModel = viewModel)
            }
        }
    }

    // Detail Dialog overlay when a title is selected
    selectedTitle?.let { title ->
        TitleDetailDialog(
            title = title,
            onDismiss = { viewModel.closeTitleDetails() },
            onAdvanceEpisode = { delta -> viewModel.advanceEpisode(title, delta) },
            onToggleMovieWatched = { viewModel.toggleMovieWatched(title) },
            onUpdateStatus = { newStatus -> viewModel.updateStatus(title, newStatus) },
            onEdit = {
                viewModel.closeTitleDetails()
                viewModel.startEditTitleForm(title)
            },
            onDelete = {
                viewModel.deleteTitle(title)
            },
            onToggleIgnore = {
                viewModel.toggleIgnoreTitle(title)
            },
            onCheckUpdates = {
                viewModel.syncSingleTitle(title)
            }
        )
    }

    // Sync Results Dialog
    syncResults?.let { results ->
        SyncResultsDialog(
            results = results,
            onDismiss = { viewModel.dismissSyncResults() }
        )
    }
}
