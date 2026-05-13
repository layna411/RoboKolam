package com.simats.kolam.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.kolam.ui.theme.Orange
import com.simats.kolam.ui.theme.Pink

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevicesScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToDesigns: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Devices",
                        style = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            brush = Brush.horizontalGradient(listOf(Orange, Pink))
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Outlined.HelpOutline, contentDescription = "Help")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = { DevicesBottomNavigation(onNavigateToHome, onNavigateToDesigns, onNavigateToSettings) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFFEF9FB))
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Connect and manage your RangoliBot device",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            ConnectedDeviceCard()

            Spacer(modifier = Modifier.height(24.dp))

            AvailableDevicesSection()

            Spacer(modifier = Modifier.height(24.dp))

            HowToConnectSection()

            Spacer(modifier = Modifier.height(24.dp))

            NeedHelpCard()
            
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun ConnectedDeviceCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(Color(0xFF2DCC70), CircleShape))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Connected", fontSize = 12.sp, color = Color(0xFF2DCC70), fontWeight = FontWeight.Medium)
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(Color(0xFFF6FFF9), RoundedCornerShape(20.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.PrecisionManufacturing,
                        contentDescription = null,
                        modifier = Modifier.size(60.dp),
                        tint = Orange.copy(alpha = 0.7f)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "RangoliBot X1", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Bluetooth, contentDescription = null, tint = Color(0xFF2DCC70), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Connected via Bluetooth", fontSize = 11.sp, color = Color(0xFF2DCC70))
                    }
                    Text(text = "Serial: RBX1-2487", fontSize = 11.sp, color = Color.Gray)
                    Text(text = "Firmware: v2.1.4", fontSize = 11.sp, color = Color.Gray)
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "100%", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.BatteryFull, contentDescription = null, tint = Color(0xFF2DCC70), modifier = Modifier.size(20.dp))
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                DeviceActionItem(Icons.Outlined.Info, "Device Info")
                DeviceActionItem(Icons.Outlined.Edit, "Test Draw")
                DeviceActionItem(Icons.Outlined.GpsFixed, "Calibrate")
                DeviceActionItem(Icons.Outlined.Settings, "Settings")
            }
        }
    }
}

@Composable
fun DeviceActionItem(icon: ImageVector, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .border(1.dp, Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
            .padding(vertical = 12.dp, horizontal = 16.dp)
    ) {
        Icon(icon, contentDescription = null, tint = Color(0xFF7B4DFF), modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun AvailableDevicesSection() {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Available Devices", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { }) {
                Icon(Icons.Default.Refresh, contentDescription = null, tint = Pink, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Scan for Devices", fontSize = 12.sp, color = Pink, fontWeight = FontWeight.Medium)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color(0xFFFFF0F5), RoundedCornerShape(16.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.SmartToy,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = Orange.copy(alpha = 0.7f)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "RangoliBot Mini", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.BluetoothDisabled, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Not Connected", fontSize = 11.sp, color = Color.Gray)
                    }
                    Text(text = "Serial: RBM-1032", fontSize = 11.sp, color = Color.Gray)
                }
                
                OutlinedButton(
                    onClick = { },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Pink),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Pink.copy(alpha = 0.3f)),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Text(text = "Connect", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
            }
        }
    }
}

@Composable
fun HowToConnectSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "How to Connect", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                ConnectStep(1, Icons.Default.Bluetooth, "Turn on\nBluetooth", Color(0xFFF0E8FF), Color(0xFF7B4DFF))
                StepConnector()
                ConnectStep(2, Icons.Default.SmartToy, "Power on\nyour RangoliBot", Color(0xFFFFF2E8), Color(0xFFFF9248))
                StepConnector()
                ConnectStep(3, Icons.Default.Search, "Select your device\nfrom the list", Color(0xFFFFE8EC), Color(0xFFFF4D6D))
                StepConnector()
                ConnectStep(4, Icons.Default.CheckCircle, "Connected!\nStart creating", Color(0xFFE8F9F0), Color(0xFF2DCC70))
            }
        }
    }
}

@Composable
fun ConnectStep(index: Int, icon: ImageVector, label: String, bg: Color, tint: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(70.dp)) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(bg, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.Top) {
            Box(modifier = Modifier.size(16.dp).background(tint, CircleShape), contentAlignment = Alignment.Center) {
                Text(text = index.toString(), color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = label, fontSize = 9.sp, lineHeight = 12.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Start)
        }
    }
}

@Composable
fun StepConnector() {
    Box(modifier = Modifier.height(40.dp), contentAlignment = Alignment.Center) {
        Text(text = "...", color = Color.LightGray)
    }
}

@Composable
fun NeedHelpCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9FA)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Pink.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFFFFE8EC), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Description, contentDescription = null, tint = Pink)
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Need Help?", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text(text = "View user manual or watch video tutorials", fontSize = 11.sp, color = Color.Gray)
            }
            
            OutlinedButton(
                onClick = { },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Pink),
                border = androidx.compose.foundation.BorderStroke(1.dp, Pink.copy(alpha = 0.3f)),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                Text(text = "Learn More", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun DevicesBottomNavigation(onHomeClick: () -> Unit, onDesignsClick: () -> Unit, onSettingsClick: () -> Unit) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = false,
            onClick = onHomeClick,
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") }
        )
        NavigationBarItem(
            selected = false,
            onClick = onDesignsClick,
            icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "Designs") },
            label = { Text("Designs") }
        )
        NavigationBarItem(
            selected = true,
            onClick = { },
            icon = { Icon(Icons.Default.SmartToy, contentDescription = "Devices") },
            label = { Text("Devices") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Pink,
                selectedTextColor = Pink,
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = onSettingsClick,
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            label = { Text("Settings") }
        )
    }
}
