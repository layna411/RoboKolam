package com.simats.kolam

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.simats.kolam.ui.navigation.AppNavGraph
import com.simats.kolam.ui.theme.KolamTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.kolam.viewmodel.KolamViewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KolamTheme {
                val viewModel: KolamViewModel = viewModel()
                val context = LocalContext.current
                
                LaunchedEffect(Unit) {
                    viewModel.initPrefs(context)
                }
                
                val navController = rememberNavController()
                AppNavGraph(navController = navController, sharedViewModel = viewModel)
            }
        }
    }
}
