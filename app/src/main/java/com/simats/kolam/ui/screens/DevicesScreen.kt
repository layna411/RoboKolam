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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.kolam.ui.components.GlassCard
import com.simats.kolam.ui.theme.*
import com.simats.kolam.viewmodel.KolamViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevicesScreen(
    viewModel: KolamViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToDesigns: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val isConnected by viewModel.isConnected.collectAsState()
    val connectionStatus by viewModel.connectionStatus.collectAsState()
    val isDrawing by viewModel.isDrawing.collectAsState()
    val drawingProgress by viewModel.drawingProgress.collectAsState()
    val currentX by viewModel.currentX.collectAsState()
    val currentY by viewModel.currentY.collectAsState()
    val activeColor by viewModel.activeColor.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Machine Control",
                        style = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkText
                        )
                    )
                },
                actions = {
                    IconButton(
                        onClick = { 
                            if (isConnected) viewModel.disconnectDevice() else viewModel.connectDevice()
                        },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .background(Color.White.copy(alpha = 0.6f), CircleShape)
                            .size(40.dp)
                    ) {
                        Icon(Icons.Outlined.Bluetooth, contentDescription = "Bluetooth", tint = if (isConnected) Color(0xFF34C759) else VioletPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = { DevicesBottomNavigation(onNavigateToHome, onNavigateToDesigns, onNavigateToSettings) },
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
                ConnectedDeviceCard(
                    isConnected = isConnected,
                    connectionStatus = connectionStatus,
                    isDrawing = isDrawing,
                    onStart = { viewModel.startDrawing() },
                    onStop = { viewModel.stopDrawing() },
                    onConnect = { viewModel.connectDevice() }
                )

                Spacer(modifier = Modifier.height(24.dp))
                
                MachineStatusPanel(
                    currentX = currentX,
                    currentY = currentY,
                    progress = drawingProgress
                )

                Spacer(modifier = Modifier.height(24.dp))

                AxisControlPanel()

                Spacer(modifier = Modifier.height(24.dp))

                ColorSelectionPanel(activeColor = activeColor)
                
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@Composable
fun ConnectedDeviceCard(
    isConnected: Boolean, 
    connectionStatus: String,
    isDrawing: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onConnect: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp).background(if (isConnected) Color(0xFF34C759) else Color.Gray, CircleShape))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = connectionStatus, fontSize = 13.sp, color = if (isConnected) Color(0xFF34C759) else Color.Gray, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color.White.copy(alpha = 0.7f), RoundedCornerShape(20.dp))
                        .padding(16.dp)
                        .clickable { if (!isConnected) onConnect() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.PrecisionManufacturing,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        tint = VioletPrimary
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "RangoliBot Pro X1", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = DarkText)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "ESP32 CNC Controller", fontSize = 13.sp, color = GrayText)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = if (isConnected) "Signal: -65 dBm | 100% Bat" else "Not Paired", fontSize = 12.sp, color = VioletSecondary, fontWeight = FontWeight.Medium)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                DeviceActionItem(Icons.Default.PlayArrow, if(isDrawing) "Drawing" else "Start", Color(0xFF34C759), onClick = onStart)
                DeviceActionItem(Icons.Default.Pause, "Pause", Color(0xFFFF9F0A), onClick = onStop)
                DeviceActionItem(Icons.Default.Stop, "E-Stop", Color(0xFFFF3B30), onClick = onStop)
                DeviceActionItem(Icons.Default.Home, "Home Pos", VioletPrimary, onClick = {})
            }
        }
    }
}

@Composable
fun DeviceActionItem(icon: ImageVector, label: String, color: Color, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 16.dp)
            .width(60.dp)
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DarkText)
    }
}

@Composable
fun MachineStatusPanel(currentX: Float, currentY: Float, progress: Float) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = "Live Telemetry", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkText, modifier = Modifier.padding(start = 4.dp))
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            TelemetryCard(title = "X Axis", value = String.format("%.1f", currentX), unit = "mm", modifier = Modifier.weight(1f))
            TelemetryCard(title = "Y Axis", value = String.format("%.1f", currentY), unit = "mm", modifier = Modifier.weight(1f))
            TelemetryCard(title = "Progress", value = "${(progress * 100).toInt()}", unit = "%", modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun TelemetryCard(title: String, value: String, unit: String, modifier: Modifier = Modifier) {
    GlassCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, fontSize = 12.sp, color = GrayText, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DarkText)
            Text(text = unit, fontSize = 10.sp, color = VioletPrimary, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun AxisControlPanel() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = "Manual Jog Controls", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkText, modifier = Modifier.padding(start = 4.dp))
        Spacer(modifier = Modifier.height(12.dp))
        
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Y+
                JogButton(icon = Icons.Default.KeyboardArrowUp, label = "Y+")
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // X-
                    JogButton(icon = Icons.Default.KeyboardArrowLeft, label = "X-")
                    
                    Spacer(modifier = Modifier.width(32.dp))
                    
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(Brush.radialGradient(listOf(Color.White, Color.White.copy(alpha = 0.5f))), CircleShape)
                            .border(2.dp, VioletPrimary.copy(alpha = 0.3f), CircleShape)
                            .clickable { },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Reset", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = VioletPrimary)
                    }
                    
                    Spacer(modifier = Modifier.width(32.dp))
                    
                    // X+
                    JogButton(icon = Icons.Default.KeyboardArrowRight, label = "X+")
                }
                
                // Y-
                JogButton(icon = Icons.Default.KeyboardArrowDown, label = "Y-")
            }
        }
    }
}

@Composable
fun JogButton(icon: ImageVector, label: String) {
    Box(
        modifier = Modifier
            .size(70.dp)
            .background(Color.White.copy(alpha = 0.8f), RoundedCornerShape(20.dp))
            .border(1.dp, VioletPrimary.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
            .clickable { },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = DarkText, modifier = Modifier.size(32.dp))
            Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = VioletPrimary)
        }
    }
}

@Composable
fun ColorSelectionPanel(activeColor: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = "Z-Axis Tool Status", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkText, modifier = Modifier.padding(start = 4.dp))
        Spacer(modifier = Modifier.height(12.dp))
        
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(20.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ToolButton("Z1", "Red", Color(0xFFFF3B30), activeColor.contains("Z1"))
                ToolButton("Z2", "Yellow", Color(0xFFFFCC00), activeColor.contains("Z2"))
                ToolButton("Z3", "Blue", Color(0xFF007AFF), activeColor.contains("Z3"))
            }
        }
    }
}

@Composable
fun ToolButton(axis: String, colorName: String, color: Color, isSelected: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(if(isSelected) color.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .border(2.dp, if(isSelected) color else Color.Transparent, RoundedCornerShape(16.dp))
            .padding(16.dp)
            .width(70.dp)
    ) {
        Box(modifier = Modifier.size(24.dp).background(color, CircleShape))
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = axis, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = DarkText)
        Text(text = colorName, fontSize = 11.sp, color = GrayText, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun DevicesBottomNavigation(onHomeClick: () -> Unit, onDesignsClick: () -> Unit, onSettingsClick: () -> Unit) {
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
            selected = true,
            onClick = { },
            icon = { Icon(Icons.Default.Bluetooth, contentDescription = "Devices") },
            label = { Text("Devices", fontWeight = FontWeight.Medium) },
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
