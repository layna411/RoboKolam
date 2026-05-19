package com.simats.kolam.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.simats.kolam.ui.components.GlassCard
import com.simats.kolam.ui.theme.*
import com.simats.kolam.viewmodel.KolamViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DesignsScreen(
    viewModel: KolamViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToDevices: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val userImages by viewModel.userImages.collectAsState()
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "My Designs",
                        style = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkText
                        )
                    )
                },
                actions = {
                    IconButton(
                        onClick = { },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .background(Color.White.copy(alpha = 0.6f), CircleShape)
                            .size(40.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add", tint = VioletPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = { DesignsBottomNavigation(onNavigateToHome, onNavigateToDevices, onNavigateToSettings) },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(BackgroundStart, BackgroundEnd)
                    )
                )
        ) {
            Box(modifier = Modifier
                .offset(x = (-50).dp, y = 150.dp)
                .size(250.dp)
                .background(TealAccent.copy(alpha = 0.15f), CircleShape)
                .blur(70.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (userImages.isEmpty()) {
                    Text("No designs found", color = GrayText, modifier = Modifier.padding(16.dp))
                } else {
                    userImages.forEach { img ->
                        val fullUrl = "http://10.0.2.2:5000${img.url}"
                        GlassCard(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .background(Color.White, RoundedCornerShape(12.dp))
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AsyncImage(
                                        model = fullUrl,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = img.filename, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkText, maxLines = 1)
                                    Text(text = "Status: ${img.status}", fontSize = 12.sp, color = GrayText)
                                }
                                IconButton(onClick = {}) {
                                    Icon(Icons.Default.MoreVert, contentDescription = null, tint = DarkText)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DesignsBottomNavigation(onHomeClick: () -> Unit, onDevicesClick: () -> Unit, onSettingsClick: () -> Unit) {
    NavigationBar(
        containerColor = Color.White.copy(alpha = 0.8f),
        tonalElevation = 0.dp,
        modifier = Modifier.background(Color.White.copy(alpha = 0.9f))
    ) {
        NavigationBarItem(
            selected = false,
            onClick = onHomeClick,
            icon = { Icon(Icons.Outlined.Home, contentDescription = "Home") },
            label = { Text("Home", fontWeight = FontWeight.Medium) },
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = GrayText,
                unselectedTextColor = GrayText
            )
        )
        NavigationBarItem(
            selected = true,
            onClick = { },
            icon = { Icon(Icons.Default.Folder, contentDescription = "Designs") },
            label = { Text("Designs", fontWeight = FontWeight.Medium) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = VioletPrimary,
                selectedTextColor = VioletPrimary,
                unselectedIconColor = GrayText,
                unselectedTextColor = GrayText,
                indicatorColor = VioletPrimary.copy(alpha = 0.1f)
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = onDevicesClick,
            icon = { Icon(Icons.Outlined.Bluetooth, contentDescription = "Devices") },
            label = { Text("Devices", fontWeight = FontWeight.Medium) },
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = GrayText,
                unselectedTextColor = GrayText
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = onSettingsClick,
            icon = { Icon(Icons.Outlined.Settings, contentDescription = "Settings") },
            label = { Text("Settings", fontWeight = FontWeight.Medium) },
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = GrayText,
                unselectedTextColor = GrayText
            )
        )
    }
}
