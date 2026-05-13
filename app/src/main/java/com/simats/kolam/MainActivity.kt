package com.simats.kolam

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.simats.kolam.ui.screens.LoginScreen
import com.simats.kolam.ui.screens.SignupScreen
import com.simats.kolam.ui.screens.SplashScreen
import com.simats.kolam.ui.theme.KolamTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KolamTheme {
                KolamApp()
            }
        }
    }
}

@Composable
fun KolamApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen(onTimeout = {
                navController.navigate("login") {
                    popUpTo("splash") { inclusive = true }
                }
            })
        }
        composable("login") {
            LoginScreen(
                onNavigateToSignup = { navController.navigate("signup") },
                onLoginSuccess = { /* Navigate to Home */ }
            )
        }
        composable("signup") {
            SignupScreen(
                onNavigateToLogin = { navController.navigate("login") },
                onSignupSuccess = { /* Navigate to Home or Login */ }
            )
        }
    }
}
