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
}

@Composable
fun AppNavGraph(navController: NavHostController) {
    val sharedViewModel: KolamViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(onSplashFinished = {
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            })
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
                onNavigateToDesigns = { navController.navigate(Screen.Designs.route) },
                onNavigateToDevices = { navController.navigate(Screen.Devices.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }
        composable(Screen.Designs.route) {
            DesignsScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onNavigateToDevices = { navController.navigate(Screen.Devices.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }
        composable(Screen.Devices.route) {
            DevicesScreen(
                viewModel = sharedViewModel,
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onNavigateToDesigns = { navController.navigate(Screen.Designs.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onNavigateToDesigns = { navController.navigate(Screen.Designs.route) },
                onNavigateToDevices = { navController.navigate(Screen.Devices.route) }
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
