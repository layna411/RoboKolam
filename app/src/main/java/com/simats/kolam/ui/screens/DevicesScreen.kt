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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevicesScreen(
    viewModel: KolamViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToDesigns: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    val isConnected by viewModel.isConnected.collectAsState()
    val connectionStatus by viewModel.connectionStatus.collectAsState()
    val isDrawing by viewModel.isDrawing.collectAsState()
    val drawingProgress by viewModel.drawingProgress.collectAsState()
    val currentX by viewModel.currentX.collectAsState()
    val currentY by viewModel.currentY.collectAsState()
    val activeColor by viewModel.activeColor.collectAsState()
    val generatedGCode by viewModel.generatedGCode.collectAsState()
    
    val bondedDevices by viewModel.bondedDevices.collectAsState()
    var showDevicePicker by remember { mutableStateOf(false) }

    // Permissions to request based on Android SDK Version
    val permissionsToRequest = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                android.Manifest.permission.BLUETOOTH_SCAN,
                android.Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            arrayOf(
                android.Manifest.permission.BLUETOOTH,
                android.Manifest.permission.BLUETOOTH_ADMIN,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }

    // Helper to check if all necessary permissions are granted
    fun hasBluetoothPermissions(ctx: Context): Boolean {
        return permissionsToRequest.all { permission ->
            ContextCompat.checkSelfPermission(ctx, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    // Bluetooth enable intent launcher
    val enableBtLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            viewModel.fetchBondedDevices(context)
            showDevicePicker = true
            Toast.makeText(context, "Bluetooth enabled. Select your CNC device...", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Bluetooth must be enabled to connect to the CNC machine.", Toast.LENGTH_SHORT).show()
        }
    }

    // Helper to verify adapter state and request user to enable Bluetooth
    fun enableBluetoothAndConnect(ctx: Context) {
        val bluetoothManager = ctx.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter
        if (bluetoothAdapter == null) {
            Toast.makeText(ctx, "Bluetooth is not supported on this device", Toast.LENGTH_SHORT).show()
        } else if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            enableBtLauncher.launch(enableBtIntent)
        } else {
            viewModel.fetchBondedDevices(ctx)
            showDevicePicker = true
        }
    }

    // Runtime Permission Request Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            enableBluetoothAndConnect(context)
        } else {
            Toast.makeText(context, "Bluetooth permissions are required to connect to the CNC machine.", Toast.LENGTH_LONG).show()
        }
    }

    // Handle connection initiation cleanly
    fun triggerConnection() {
        if (hasBluetoothPermissions(context)) {
            enableBluetoothAndConnect(context)
        } else {
            permissionLauncher.launch(permissionsToRequest)
        }
    }

    // Parse the dynamic colors from the G-code design
    val activeZValues = remember(generatedGCode) {
        val set = mutableSetOf<String>()
        if (generatedGCode.isNotEmpty()) {
            generatedGCode.lines().forEach { line ->
                val trimmed = line.trim()
                if (trimmed.contains("Z1") || trimmed.contains("Color 1")) {
                    set.add("Z1")
                }
                if (trimmed.contains("Z2") || trimmed.contains("Color 2")) {
                    set.add("Z2")
                }
                if (trimmed.contains("Z3") || trimmed.contains("Color 3")) {
                    set.add("Z3")
                }
            }
        }
        set
    }

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
                            if (isConnected) viewModel.disconnectDevice() else triggerConnection()
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
                    onConnect = { triggerConnection() },
                    onHome = { viewModel.homeMachine() }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Realtime3DVisualizer(viewModel = viewModel)

                Spacer(modifier = Modifier.height(24.dp))
                
                MachineStatusPanel(
                    currentX = currentX,
                    currentY = currentY,
                    progress = drawingProgress
                )

                Spacer(modifier = Modifier.height(24.dp))

                AxisControlPanel(viewModel = viewModel, isConnected = isConnected)

                Spacer(modifier = Modifier.height(24.dp))

                ColorSelectionPanel(activeColor = activeColor, activeZValues = activeZValues)
                
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    if (showDevicePicker) {
        AlertDialog(
            onDismissRequest = { showDevicePicker = false },
            title = {
                Text(
                    text = "Connect to CNC Controller",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = DarkText
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = "Select a paired Bluetooth device (HC-05, HC-06, etc.) to start drawing:",
                        fontSize = 13.sp,
                        color = GrayText,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    if (bondedDevices.isEmpty()) {
                        Text(
                            text = "No paired Bluetooth devices found.\n\nPlease pair your HC-05 module first in Android Settings > Bluetooth.",
                            fontSize = 14.sp,
                            color = Color(0xFFFF3B30),
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 250.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            bondedDevices.forEach { device ->
                                val hasConnectPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
                                } else true
                                
                                val deviceName = if (hasConnectPermission) {
                                    @Suppress("MissingPermission")
                                    device.name ?: "Unknown Device"
                                } else {
                                    "Bluetooth Device"
                                }
                                
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.connectDevice(device, context)
                                            showDevicePicker = false
                                        }
                                        .padding(vertical = 12.dp, horizontal = 8.dp)
                                        .background(
                                            color = if (deviceName.contains("HC-05", ignoreCase = true) || deviceName.contains("HC-06", ignoreCase = true)) {
                                                VioletPrimary.copy(alpha = 0.05f)
                                            } else Color.Transparent,
                                            shape = RoundedCornerShape(8.dp)
                                        ),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Bluetooth,
                                        contentDescription = null,
                                        tint = if (deviceName.contains("HC-05", ignoreCase = true) || deviceName.contains("HC-06", ignoreCase = true)) {
                                            Color(0xFF34C759)
                                        } else VioletPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(text = deviceName, fontWeight = FontWeight.SemiBold, color = DarkText, fontSize = 14.sp)
                                        Text(text = device.address, fontSize = 11.sp, color = GrayText)
                                    }
                                }
                                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f), thickness = 0.5.dp)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showDevicePicker = false }) {
                    Text("Cancel", color = VioletPrimary, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
fun ConnectedDeviceCard(
    isConnected: Boolean, 
    connectionStatus: String,
    isDrawing: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onConnect: () -> Unit,
    onHome: () -> Unit
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
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DeviceActionItem(Icons.Default.PlayArrow, if(isDrawing) "Drawing" else "Start", Color(0xFF34C759), onClick = onStart, enabled = isConnected, modifier = Modifier.weight(1f))
                DeviceActionItem(Icons.Default.Pause, "Pause", Color(0xFFFF9F0A), onClick = onStop, enabled = isConnected, modifier = Modifier.weight(1f))
                DeviceActionItem(Icons.Default.Stop, "E-Stop", Color(0xFFFF3B30), onClick = onStop, enabled = isConnected, modifier = Modifier.weight(1f))
                DeviceActionItem(Icons.Default.Home, "Home Pos", VioletPrimary, onClick = onHome, enabled = isConnected, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun DeviceActionItem(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    val alpha = if (enabled) 1.0f else 0.35f
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .background(Color.White.copy(alpha = if (enabled) 0.5f else 0.2f), RoundedCornerShape(16.dp))
            .clickable(enabled = enabled) { onClick() }
            .padding(vertical = 12.dp, horizontal = 4.dp)
    ) {
        Icon(icon, contentDescription = null, tint = color.copy(alpha = alpha), modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = DarkText.copy(alpha = alpha),
            textAlign = TextAlign.Center,
            maxLines = 1
        )
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
fun AxisControlPanel(viewModel: KolamViewModel, isConnected: Boolean) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = "Manual Jog Controls", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkText, modifier = Modifier.padding(start = 4.dp))
        Spacer(modifier = Modifier.height(12.dp))
        
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Y+
                JogButton(icon = Icons.Default.KeyboardArrowUp, label = "Y+", enabled = isConnected) {
                    viewModel.jog("Y", 1f)
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // X-
                    JogButton(icon = Icons.Default.KeyboardArrowLeft, label = "X-", enabled = isConnected) {
                        viewModel.jog("X", -1f)
                    }
                    
                    Spacer(modifier = Modifier.width(32.dp))
                    
                    val resetAlpha = if (isConnected) 1.0f else 0.35f
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(Brush.radialGradient(listOf(Color.White.copy(alpha = resetAlpha), Color.White.copy(alpha = resetAlpha * 0.5f))), CircleShape)
                            .border(2.dp, VioletPrimary.copy(alpha = resetAlpha * 0.3f), CircleShape)
                            .clickable(enabled = isConnected) { viewModel.resetCoordinates() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Reset", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = VioletPrimary.copy(alpha = resetAlpha))
                    }
                    
                    Spacer(modifier = Modifier.width(32.dp))
                    
                    // X+
                    JogButton(icon = Icons.Default.KeyboardArrowRight, label = "X+", enabled = isConnected) {
                        viewModel.jog("X", 1f)
                    }
                }
                
                // Y-
                JogButton(icon = Icons.Default.KeyboardArrowDown, label = "Y-", enabled = isConnected) {
                    viewModel.jog("Y", -1f)
                }
            }
        }
    }
}

@Composable
fun JogButton(icon: ImageVector, label: String, enabled: Boolean, onClick: () -> Unit) {
    val alpha = if (enabled) 1.0f else 0.35f
    Box(
        modifier = Modifier
            .size(70.dp)
            .background(Color.White.copy(alpha = if (enabled) 0.8f else 0.3f), RoundedCornerShape(20.dp))
            .border(1.dp, VioletPrimary.copy(alpha = if (enabled) 0.1f else 0.03f), RoundedCornerShape(20.dp))
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = DarkText.copy(alpha = alpha), modifier = Modifier.size(32.dp))
            Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = VioletPrimary.copy(alpha = alpha))
        }
    }
}

@Composable
fun ColorSelectionPanel(activeColor: String, activeZValues: Set<String>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Z-Axis Tool Status",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = DarkText,
            modifier = Modifier.padding(start = 4.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            if (activeZValues.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No design loaded. Open a design in the 'Designs' tab to configure active tools.",
                        fontSize = 13.sp,
                        color = GrayText,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Row(
                    modifier = Modifier.padding(20.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
                ) {
                    if (activeZValues.contains("Z1")) {
                        ToolButton(
                            axis = "Z1",
                            colorName = "Red",
                            color = Color(0xFFFF3B30),
                            isSelected = activeColor.contains("Z1") || activeColor.contains("Red"),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (activeZValues.contains("Z2")) {
                        ToolButton(
                            axis = "Z2",
                            colorName = "Yellow",
                            color = Color(0xFFFFCC00),
                            isSelected = activeColor.contains("Z2") || activeColor.contains("Yellow"),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (activeZValues.contains("Z3")) {
                        ToolButton(
                            axis = "Z3",
                            colorName = "Blue",
                            color = Color(0xFF007AFF),
                            isSelected = activeColor.contains("Z3") || activeColor.contains("Blue"),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ToolButton(
    axis: String,
    colorName: String,
    color: Color,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .background(if(isSelected) color.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .border(2.dp, if(isSelected) color else Color.Transparent, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Box(modifier = Modifier.size(24.dp).background(color, CircleShape))
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = axis, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = DarkText, textAlign = TextAlign.Center)
        Text(text = colorName, fontSize = 11.sp, color = GrayText, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
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

@Composable
fun Realtime3DVisualizer(viewModel: KolamViewModel) {
    val toolpathLines by viewModel.toolpathLines.collectAsState()
    val currentLineIndex by viewModel.currentLineIndex.collectAsState()
    val currentX by viewModel.currentX.collectAsState()
    val currentY by viewModel.currentY.collectAsState()
    val activeColor by viewModel.activeColor.collectAsState()
    val isDrawing by viewModel.isDrawing.collectAsState()
    
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "3D Realtime Workspace",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = DarkText,
            modifier = Modifier.padding(start = 4.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    
                    // Center point of the isometric bed
                    val centerX = width / 2f
                    val centerY = height / 2f + 25.dp.toPx()
                    
                    // Isometric constants
                    val angleRad = Math.toRadians(30.0)
                    val cosA = Math.cos(angleRad).toFloat()
                    val sinA = Math.sin(angleRad).toFloat()
                    
                    // Map 200mmx200mm coordinates to fit beautifully on the Canvas viewport
                    val scaleX = (width * 0.42f) / 200f
                    val scaleY = (width * 0.42f) / 200f
                    val zScale = 0.6f
                    
                    // 3D Isometric projection function
                    fun project3D(x: Float, y: Float, z: Float): Offset {
                        val dx = (x - 100f) * scaleX
                        val dy = (y - 100f) * scaleY
                        val dz = z * zScale
                        
                        val px = centerX + (dx - dy) * cosA
                        val py = centerY + (dx + dy) * sinA - dz
                        return Offset(px, py)
                    }
                    
                    // 1. Draw 3D Frosted Glass Bed plate (Isometric 3D box)
                    val bedCorner0 = project3D(0f, 0f, 0f)
                    val bedCorner1 = project3D(200f, 0f, 0f)
                    val bedCorner2 = project3D(200f, 200f, 0f)
                    val bedCorner3 = project3D(0f, 200f, 0f)
                    
                    val bedThickness = 12.dp.toPx()
                    val bedBot0 = Offset(bedCorner0.x, bedCorner0.y + bedThickness)
                    val bedBot1 = Offset(bedCorner1.x, bedCorner1.y + bedThickness)
                    val bedBot2 = Offset(bedCorner2.x, bedCorner2.y + bedThickness)
                    val bedBot3 = Offset(bedCorner3.x, bedCorner3.y + bedThickness)
                    
                    // Draw bottom thickness side faces
                    val pathLeft = Path().apply {
                        moveTo(bedCorner0.x, bedCorner0.y)
                        lineTo(bedCorner3.x, bedCorner3.y)
                        lineTo(bedBot3.x, bedBot3.y)
                        lineTo(bedBot0.x, bedBot0.y)
                        close()
                    }
                    drawPath(
                        path = pathLeft,
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.White.copy(alpha = 0.15f), Color.White.copy(alpha = 0.05f))
                        )
                    )
                    
                    val pathRight = Path().apply {
                        moveTo(bedCorner3.x, bedCorner3.y)
                        lineTo(bedCorner2.x, bedCorner2.y)
                        lineTo(bedBot2.x, bedBot2.y)
                        lineTo(bedBot3.x, bedBot3.y)
                        close()
                    }
                    drawPath(
                        path = pathRight,
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.White.copy(alpha = 0.2f), Color.White.copy(alpha = 0.05f))
                        )
                    )
                    
                    // Top Face of Bed Plate
                    val bedTopPath = Path().apply {
                        moveTo(bedCorner0.x, bedCorner0.y)
                        lineTo(bedCorner1.x, bedCorner1.y)
                        lineTo(bedCorner2.x, bedCorner2.y)
                        lineTo(bedCorner3.x, bedCorner3.y)
                        close()
                    }
                    drawPath(
                        path = bedTopPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.White.copy(alpha = 0.35f), Color.White.copy(alpha = 0.15f))
                        )
                    )
                    drawPath(
                        path = bedTopPath,
                        color = VioletPrimary.copy(alpha = 0.25f),
                        style = Stroke(width = 2.dp.toPx())
                    )
                    
                    // 2. Draw 3D Grid Lines on the bed plate
                    val gridSteps = 5
                    for (i in 1 until gridSteps) {
                        val ratio = (i.toFloat() / gridSteps) * 200f
                        // lines along X
                        val lineXStart = project3D(0f, ratio, 0f)
                        val lineXEnd = project3D(200f, ratio, 0f)
                        drawLine(
                            color = Color.White.copy(alpha = 0.25f),
                            start = lineXStart,
                            end = lineXEnd,
                            strokeWidth = 1.dp.toPx()
                        )
                        
                        // lines along Y
                        val lineYStart = project3D(ratio, 0f, 0f)
                        val lineYEnd = project3D(ratio, 200f, 0f)
                        drawLine(
                            color = Color.White.copy(alpha = 0.25f),
                            start = lineYStart,
                            end = lineYEnd,
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                    
                    // 3. Draw parsed Toolpath vector paths
                    if (toolpathLines.isNotEmpty()) {
                        toolpathLines.forEachIndexed { idx, line ->
                            val isDone = isDrawing && idx <= currentLineIndex
                            val showAsCompleted = !isDrawing || isDone
                            
                            val startProj = project3D(line.startX, line.startY, 0f)
                            val endProj = project3D(line.endX, line.endY, 0f)
                            
                            if (line.color == "Travel") {
                                // Travel moves (G0) drawn as thin dotted paths
                                val showTravel = !isDrawing || !isDone
                                if (showTravel) {
                                    drawLine(
                                        color = Color.White.copy(alpha = if (!isDrawing) 0.08f else 0.15f),
                                        start = startProj,
                                        end = endProj,
                                        strokeWidth = 1.dp.toPx(),
                                        pathEffect = PathEffect.dashPathEffect(
                                            intervals = floatArrayOf(6f, 6f),
                                            phase = 0f
                                        )
                                    )
                                }
                            } else {
                                // Drawing contours (G1)
                                val strokeCol = when (line.color) {
                                    "Red" -> Color(0xFFFF3B30)
                                    "Yellow" -> Color(0xFFFFCC00)
                                    "Blue" -> Color(0xFF007AFF)
                                    else -> Color.White
                                }
                                
                                if (showAsCompleted) {
                                    // Solid path
                                    drawLine(
                                        color = strokeCol,
                                        start = startProj,
                                        end = endProj,
                                        strokeWidth = 2.dp.toPx()
                                    )
                                    if (isDrawing && isDone) {
                                        // Glow highlight only during active drawing
                                        drawLine(
                                            color = strokeCol.copy(alpha = 0.35f),
                                            start = startProj,
                                            end = endProj,
                                            strokeWidth = 5.dp.toPx()
                                        )
                                    }
                                } else {
                                    // Pending contours - thin semi-transparent lines
                                    drawLine(
                                        color = strokeCol.copy(alpha = 0.2f),
                                        start = startProj,
                                        end = endProj,
                                        strokeWidth = 1.2.dp.toPx()
                                    )
                                }
                            }
                        }
                    }
                    
                    // 4. Draw Gantry Bridge (Y-rails sliding bridge at Y = currentY)
                    val gantryLeft = project3D(0f, currentY, 20f)
                    val gantryRight = project3D(200f, currentY, 20f)
                    
                    // Side rails guide paths
                    drawLine(
                        color = Color.White.copy(alpha = 0.2f),
                        start = project3D(0f, 0f, 10f),
                        end = project3D(0f, 200f, 10f),
                        strokeWidth = 2.dp.toPx()
                    )
                    drawLine(
                        color = Color.White.copy(alpha = 0.2f),
                        start = project3D(200f, 0f, 10f),
                        end = project3D(200f, 200f, 10f),
                        strokeWidth = 2.dp.toPx()
                    )
                    
                    // Main bridge metal beam sliding in Y-axis
                    drawLine(
                        brush = Brush.horizontalGradient(
                            colors = listOf(VioletPrimary.copy(alpha = 0.7f), VioletSecondary.copy(alpha = 0.7f))
                        ),
                        start = gantryLeft,
                        end = gantryRight,
                        strokeWidth = 5.dp.toPx()
                    )
                    
                    // 6. Draw Print Head / Dispenser Carriage sliding in X-axis (currentX, currentY)
                    val nozzlePos = project3D(currentX, currentY, 20f)
                    val tipHeight = if (activeColor != "None") 0f else 8f
                    val tipPos = project3D(currentX, currentY, tipHeight)
                    
                    // Vertical guide needle
                    drawLine(
                        color = Color.LightGray,
                        start = nozzlePos,
                        end = tipPos,
                        strokeWidth = 2.dp.toPx()
                    )
                    
                    // Carriage block (3D circle)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.White, Color.LightGray)
                        ),
                        radius = 7.dp.toPx(),
                        center = nozzlePos
                    )
                    drawCircle(
                        color = VioletPrimary.copy(alpha = 0.8f),
                        radius = 7.dp.toPx(),
                        center = nozzlePos,
                        style = Stroke(width = 1.dp.toPx())
                    )
                    
                    // Dispenser nozzle tip (active tool color glowing)
                    val tipColor = when {
                        activeColor.contains("Red") -> Color(0xFFFF3B30)
                        activeColor.contains("Yellow") -> Color(0xFFFFCC00)
                        activeColor.contains("Blue") -> Color(0xFF007AFF)
                        else -> Color.White.copy(alpha = 0.7f)
                    }
                    
                    drawCircle(
                        color = tipColor,
                        radius = 3.dp.toPx(),
                        center = tipPos
                    )
                    
                    if (activeColor != "None") {
                        // Drawing glow spray particles micro-effect
                        drawCircle(
                            color = tipColor.copy(alpha = 0.35f),
                            radius = 10.dp.toPx(),
                            center = tipPos
                        )
                        drawCircle(
                            color = tipColor.copy(alpha = 0.15f),
                            radius = 16.dp.toPx(),
                            center = tipPos
                        )
                    }
                }
                
                // Overlay Badge status indicator
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isDrawing) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(Color(0xFF34C759), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            text = if (isDrawing) "LIVE DRAWING" else "IDLE",
                            color = if (isDrawing) Color(0xFF34C759) else Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
