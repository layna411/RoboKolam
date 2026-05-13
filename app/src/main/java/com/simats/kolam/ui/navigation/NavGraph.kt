package com.simats.kolam.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.simats.kolam.ui.screens.*

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
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(onTimeout = {
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            })
        }
        composable(Screen.Login.route) {
            LoginScreen(
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
                onNavigateToUpload = { navController.navigate(Screen.UploadImage.route) },
                onNavigateToDesigns = { navController.navigate(Screen.Designs.route) },
                onNavigateToDevices = { navController.navigate(Screen.Devices.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }
        composable(Screen.Designs.route) {
            DesignsScreen(
                onBackClick = { navController.popBackStack() },
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
                onBackClick = { navController.popBackStack() },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onNavigateToDesigns = { navController.navigate(Screen.Designs.route) },
                onNavigateToDevices = { navController.navigate(Screen.Devices.route) },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.UploadImage.route) {
            UploadImageScreen(
                onBackClick = { navController.popBackStack() },
                onContinueClick = { navController.navigate(Screen.ImageToGCode.route) }
            )
        }
        composable(Screen.ImageToGCode.route) {
            ImageToGCodeScreen(
                onBackClick = { navController.popBackStack() },
                onContinueClick = { navController.navigate(Screen.GCodePreview.route) }
            )
        }
        composable(Screen.GCodePreview.route) {
            GCodePreviewScreen(
                onBackClick = { navController.popBackStack() },
                onContinueClick = { navController.navigate(Screen.SetColors.route) }
            )
        }
        composable(Screen.SetColors.route) {
            SetColorsScreen(
                onBackClick = { navController.popBackStack() },
                onContinueClick = { /* Next: Connect Device */ }
            )
        }
    }
}
