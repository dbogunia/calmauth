package io.quiet.auth.ui.nav

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import io.quiet.auth.data.BackupIO
import io.quiet.auth.data.PinRepository
import io.quiet.auth.ui.screens.BackupProcessingScreen
import io.quiet.auth.ui.screens.DangerZoneScreen
import io.quiet.auth.ui.screens.OnboardingScreen
import io.quiet.auth.ui.screens.PinScreen
import io.quiet.auth.ui.viewmodel.PinViewModel
import io.quiet.auth.ui.viewmodel.TwoFAViewModel

private fun NavController.navigateToUnlockPin() {
    navigate(Routes.pin(PinRouteMode.UNLOCK)) {
        popUpTo(Routes.TWOFAS) { inclusive = false }
    }
}

@Composable
fun AppNav(
    pinViewModel: PinViewModel,
    twoFAViewModel: TwoFAViewModel,
    backupIO: BackupIO,
    pinRepository: PinRepository,
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.START) {
        composable(Routes.START) {
            val pinState by pinViewModel.state.collectAsState()
            var bootstrapped by remember { mutableStateOf(false) }
            LaunchedEffect(pinState.isLoading) {
                if (bootstrapped || pinState.isLoading) return@LaunchedEffect
                bootstrapped = true
                val onboardingDone = pinRepository.isOnboardingCompleted()
                val target = when {
                    !onboardingDone -> Routes.ONBOARDING
                    pinState.isPinEnabled && !pinState.isUnlocked ->
                        Routes.pin(PinRouteMode.UNLOCK)
                    else -> Routes.TWOFAS
                }
                navController.navigate(target) {
                    popUpTo(Routes.START) { inclusive = true }
                }
            }
            Box(Modifier.fillMaxSize())
        }
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onContinueWithoutPin = {
                    pinRepository.setOnboardingCompleted()
                    navController.navigate(Routes.TWOFAS) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                },
                onProtectWithPin = {
                    navController.navigate(Routes.pin(PinRouteMode.SETUP))
                },
                onDeveloperMode = { navController.navigate(Routes.DEVELOPER_MODE) },
            )
        }
        composable(
            route = Routes.PIN_ROUTE,
            arguments = listOf(navArgument(Routes.ARG_PIN_MODE) { type = NavType.StringType }),
        ) { entry ->
            val mode = entry.arguments?.getString(Routes.ARG_PIN_MODE)
                .takeUnless { it.isNullOrEmpty() }
                ?: PinRouteMode.UNLOCK
            PinScreen(
                viewModel = pinViewModel,
                mode = mode,
                onFinished = {
                    when (mode) {
                        PinRouteMode.VERIFY_DISABLE -> navController.popBackStack()
                        PinRouteMode.SETUP -> if (pinRepository.isOnboardingCompleted()) {
                            navController.popBackStack()
                        } else {
                            pinRepository.setOnboardingCompleted()
                            navController.navigate(Routes.TWOFAS) {
                                popUpTo(Routes.ONBOARDING) { inclusive = true }
                            }
                        }
                        else -> navController.navigate(Routes.TWOFAS) {
                            launchSingleTop = true
                        }
                    }
                },
            )
        }
        composable(Routes.TWOFAS) {
            MainTabHost(
                pinViewModel = pinViewModel,
                twoFAViewModel = twoFAViewModel,
                pinRepository = pinRepository,
                onNavigateToPin = { pinMode ->
                    navController.navigate(Routes.pin(pinMode))
                },
                onNavigateToBackupProcessing = { action ->
                    navController.navigate(Routes.backupProcessing(action))
                },
                onLocked = { navController.navigateToUnlockPin() },
                onAfterReset = {
                    navController.navigate(Routes.ONBOARDING) {
                        popUpTo(Routes.TWOFAS) { inclusive = true }
                    }
                },
            )
        }
        composable(
            route = Routes.BACKUP_PROCESSING,
            arguments = listOf(navArgument(Routes.ARG_ACTION) { type = NavType.StringType }),
        ) { backStackEntry ->
            val action = backStackEntry.arguments?.getString(Routes.ARG_ACTION) ?: "create"
            BackupProcessingScreen(
                action = action,
                pinViewModel = pinViewModel,
                twoFAViewModel = twoFAViewModel,
                backupIO = backupIO,
                onFinished = { navController.popBackStack() },
                onLocked = { navController.navigateToUnlockPin() },
            )
        }
        composable(Routes.DEVELOPER_MODE) {
            DangerZoneScreen(
                pinViewModel = pinViewModel,
                twoFAViewModel = twoFAViewModel,
                pinRepository = pinRepository,
                onBack = { navController.popBackStack() },
                onAfterReset = {
                    navController.navigate(Routes.ONBOARDING) {
                        popUpTo(Routes.TWOFAS) { inclusive = true }
                    }
                },
            )
        }
    }

    val activity = LocalContext.current as FragmentActivity
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    BackHandler(enabled = currentRoute == Routes.TWOFAS) {
        activity.finish()
    }
}
