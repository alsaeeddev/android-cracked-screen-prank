package com.alsaeeddev.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.alsaeeddev.FakeCrackedScreenApp
import com.alsaeeddev.presentation.home.HomeScreen
import com.alsaeeddev.presentation.home.HomeViewModel
import com.alsaeeddev.presentation.settings.SettingsScreen
import com.alsaeeddev.presentation.settings.SettingsViewModel

object Destinations {
    const val HOME = "home"
    const val SETTINGS = "settings"
}

@Composable
fun FakeCrackedScreenNavGraph(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val app = context.applicationContext as FakeCrackedScreenApp
    val container = app.appContainer

    NavHost(
        navController = navController,
        startDestination = Destinations.HOME,
        modifier = modifier
    ) {
        composable(Destinations.HOME) {
            val homeViewModel: HomeViewModel = viewModel {
                HomeViewModel(
                    settingsRepository = container.settingsRepository,
                    observeShakeEventsUseCase = container.observeShakeEventsUseCase,
                    triggerPrankUseCase = container.triggerPrankUseCase,
                    soundEffectPlayer = container.soundEffectPlayer,
                    prankModeRepository = container.prankModeRepository,
                    startPrankModeUseCase = container.startPrankModeUseCase,
                    stopPrankModeUseCase = container.stopPrankModeUseCase,
                    overlayPermissionRepository = container.overlayPermissionRepository
                )
            }

            HomeScreen(
                viewModel = homeViewModel,
                onNavigateToSettings = {
                    navController.navigate(Destinations.SETTINGS)
                }
            )
        }

        composable(Destinations.SETTINGS) {
            val settingsViewModel: SettingsViewModel = viewModel {
                SettingsViewModel(
                    settingsRepository = container.settingsRepository,
                    soundEffectPlayer = container.soundEffectPlayer,
                    overlayPermissionRepository = container.overlayPermissionRepository,
                    prankModeRepository = container.prankModeRepository,
                    startPrankModeUseCase = container.startPrankModeUseCase,
                    stopPrankModeUseCase = container.stopPrankModeUseCase
                )
            }

            SettingsScreen(
                viewModel = settingsViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
