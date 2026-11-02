package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import com.example.ui.screens.AddEditTitleScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.RouletteScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TitleDetailDialog
import com.example.ui.theme.SeraAkisosTheme
import com.example.viewmodel.NavDestination
import com.example.viewmodel.SeraAkisosViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: SeraAkisosViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
    val snackbarHostState = remember { SnackbarHostState() }

    // Collect snackbar messages
    LaunchedEffect(viewModel) {
        viewModel.messageEvents.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    // Android System Back Handling
    BackHandler(enabled = selectedTitle != null || currentNav != NavDestination.HOME) {
        if (selectedTitle != null) {
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
            }
        )
    }
}
