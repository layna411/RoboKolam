package com.simats.kolam.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.kolam.ui.components.GlassCard
import com.simats.kolam.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToDesigns: () -> Unit,
    onNavigateToDevices: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkText
                        )
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = { SettingsBottomNavigation(onNavigateToHome, onNavigateToDesigns, onNavigateToDevices) },
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
                .offset(x = 150.dp, y = (-50).dp)
                .size(250.dp)
                .background(VioletPrimary.copy(alpha = 0.15f), CircleShape)
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
                // Profile Section
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .background(Brush.linearGradient(listOf(VioletPrimary, VioletSecondary)), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "A", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Ananya", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkText)
                            Text(text = "ananya@example.com", fontSize = 13.sp, color = GrayText)
                        }
                        
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = GrayText)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Machine Settings Section
                Text(text = "Machine Settings", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkText, modifier = Modifier.fillMaxWidth().padding(start = 4.dp, bottom = 12.dp))
                
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        SettingItem(icon = Icons.Outlined.Bluetooth, title = "Bluetooth Preferences", subtitle = "Manage paired devices")
                        HorizontalDivider(color = Color.White.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 16.dp))
                        SettingItem(icon = Icons.Outlined.Speed, title = "Motor Calibration", subtitle = "Tune X, Y, Z axis steps")
                        HorizontalDivider(color = Color.White.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 16.dp))
                        SettingItem(icon = Icons.Outlined.Polyline, title = "Powder Flow Rate", subtitle = "Adjust Z-axis feed speeds")
                        HorizontalDivider(color = Color.White.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 16.dp))
                        SettingItem(icon = Icons.Outlined.SystemUpdateAlt, title = "Firmware Update", subtitle = "Version 2.1.4 (Up to date)")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                // App Settings Section
                Text(text = "App Settings", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkText, modifier = Modifier.fillMaxWidth().padding(start = 4.dp, bottom = 12.dp))
                
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        SettingItem(icon = Icons.Outlined.DarkMode, title = "Theme", subtitle = "System Default")
                        HorizontalDivider(color = Color.White.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 16.dp))
                        SettingItem(icon = Icons.Outlined.Notifications, title = "Notifications", subtitle = "On")
                        HorizontalDivider(color = Color.White.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 16.dp))
                        SettingItem(icon = Icons.Outlined.HelpOutline, title = "Help & Support", subtitle = "Manuals, FAQs, Contact")
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))
                
                TextButton(onClick = { }) {
                    Text(text = "Log Out", color = Color(0xFFFF3B30), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun SettingItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color.White, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = VioletPrimary, modifier = Modifier.size(20.dp))
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DarkText)
            Text(text = subtitle, fontSize = 12.sp, color = GrayText)
        }
        
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = GrayText)
    }
}

@Composable
fun SettingsBottomNavigation(onHomeClick: () -> Unit, onDesignsClick: () -> Unit, onDevicesClick: () -> Unit) {
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
            selected = false,
            onClick = onDesignsClick,
            icon = { Icon(Icons.Outlined.Folder, contentDescription = "Designs") },
            label = { Text("Designs", fontWeight = FontWeight.Medium) },
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = GrayText,
                unselectedTextColor = GrayText
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
            selected = true,
            onClick = { },
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            label = { Text("Settings", fontWeight = FontWeight.Medium) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = VioletPrimary,
                selectedTextColor = VioletPrimary,
                unselectedIconColor = GrayText,
                unselectedTextColor = GrayText,
                indicatorColor = VioletPrimary.copy(alpha = 0.1f)
            )
        )
    }
}
