package com.simats.kolam.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.simats.kolam.ui.screens.*
import com.simats.kolam.viewmodel.KolamViewModel

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Signup : Screen("signup")
    object Home : Screen("home")
    object UploadImage : Screen("upload_image")
    object ImageToGCode : Screen("image_to_gcode")
    object GCodePreview : Screen("gcode_preview")
    object SetColors : Screen("set_colors")
    object Designs : Screen("designs")
    object Devices : Screen("devices")
    object Settings : Screen("settings")
    object Notifications : Screen("notifications")
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    sharedViewModel: KolamViewModel = viewModel()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                viewModel = sharedViewModel,
                onSplashFinished = { isLoggedIn ->
                    val destination = if (isLoggedIn) Screen.Home.route else Screen.Login.route
                    navController.navigate(destination) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = sharedViewModel,
                onNavigateToSignup = { navController.navigate(Screen.Signup.route) },
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Signup.route) {
            SignupScreen(
                viewModel = sharedViewModel,
                onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                onSignupSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Signup.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = sharedViewModel,
                onNavigateToUpload = { navController.navigate(Screen.UploadImage.route) },
                onNavigateToProcessing = { navController.navigate(Screen.ImageToGCode.route) },
                onNavigateToDesigns = { navController.navigate(Screen.Designs.route) },
                onNavigateToDevices = { navController.navigate(Screen.Devices.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) }
            )
        }
        composable(Screen.Notifications.route) {
            NotificationsScreen(
                viewModel = sharedViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(Screen.Designs.route) {
            DesignsScreen(
                viewModel = sharedViewModel,
                onNavigateToHome = { navController.navigate(Screen.Home.route) { popUpTo(Screen.Home.route) { inclusive = true } } },
                onNavigateToProcessing = { navController.navigate(Screen.ImageToGCode.route) },
                onNavigateToDevices = { navController.navigate(Screen.Devices.route) { popUpTo(Screen.Home.route) } },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) { popUpTo(Screen.Home.route) } }
            )
        }
        composable(Screen.Devices.route) {
            DevicesScreen(
                viewModel = sharedViewModel,
                onNavigateToHome = { navController.navigate(Screen.Home.route) { popUpTo(Screen.Home.route) { inclusive = true } } },
                onNavigateToDesigns = { navController.navigate(Screen.Designs.route) { popUpTo(Screen.Home.route) } },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) { popUpTo(Screen.Home.route) } }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                viewModel = sharedViewModel,
                onNavigateToHome = { navController.navigate(Screen.Home.route) { popUpTo(Screen.Home.route) { inclusive = true } } },
                onNavigateToDesigns = { navController.navigate(Screen.Designs.route) { popUpTo(Screen.Home.route) } },
                onNavigateToDevices = { navController.navigate(Screen.Devices.route) { popUpTo(Screen.Home.route) } },
                onNavigateToLogin = { navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } } }
            )
        }
        composable(Screen.UploadImage.route) {
            UploadImageScreen(
                viewModel = sharedViewModel,
                onBackClick = { navController.popBackStack() },
                onContinueClick = { navController.navigate(Screen.ImageToGCode.route) }
            )
        }
        composable(Screen.ImageToGCode.route) {
            ImageToGCodeScreen(
                viewModel = sharedViewModel,
                onBackClick = { navController.popBackStack() },
                onContinueClick = { 
                    sharedViewModel.generateGCode()
                    navController.navigate(Screen.GCodePreview.route) 
                }
            )
        }
        composable(Screen.GCodePreview.route) {
            GCodePreviewScreen(
                viewModel = sharedViewModel,
                onBackClick = { navController.popBackStack() },
                onContinueClick = { navController.navigate(Screen.SetColors.route) }
            )
        }
        composable(Screen.SetColors.route) {
            SetColorsScreen(
                onBackClick = { navController.popBackStack() },
                onContinueClick = { navController.navigate(Screen.Devices.route) }
            )
        }
    }
}
