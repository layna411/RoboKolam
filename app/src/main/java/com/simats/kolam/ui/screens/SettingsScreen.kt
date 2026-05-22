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
import androidx.compose.runtime.*
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
import com.simats.kolam.viewmodel.KolamViewModel
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.foundation.clickable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: KolamViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToDesigns: () -> Unit,
    onNavigateToDevices: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val context = LocalContext.current
    
    // Core Session Data
    val currentUser by viewModel.currentUser.collectAsState()
    
    // Calibration States
    val motorStepsX by viewModel.motorStepsX.collectAsState()
    val motorStepsY by viewModel.motorStepsY.collectAsState()
    val motorStepsZ by viewModel.motorStepsZ.collectAsState()
    val powderFlowRate by viewModel.powderFlowRate.collectAsState()
    val bedWidth by viewModel.bedWidth.collectAsState()
    val bedHeight by viewModel.bedHeight.collectAsState()
    val machineFeedrate by viewModel.machineFeedrate.collectAsState()
    
    // Preferences States
    val appThemeMode by viewModel.appThemeMode.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val unitsPreference by viewModel.unitsPreference.collectAsState()

    // Dialog Toggle States
    var showProfileDialog by remember { mutableStateOf(false) }
    var showCalibrationDialog by remember { mutableStateOf(false) }
    var showFlowRateDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showUnitsDialog by remember { mutableStateOf(false) }

    // 1. PROFILE UPDATE DIALOG
    if (showProfileDialog) {
        var usernameInput by remember { mutableStateOf(currentUser?.username ?: "") }
        var emailInput by remember { mutableStateOf(currentUser?.email ?: "") }
        var passwordInput by remember { mutableStateOf("") }
        var isUpdating by remember { mutableStateOf(false) }
        
        AlertDialog(
            onDismissRequest = { if (!isUpdating) showProfileDialog = false },
            title = { Text("Update Profile", fontWeight = FontWeight.Bold, color = DarkText) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = usernameInput,
                        onValueChange = { usernameInput = it },
                        label = { Text("Username") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VioletPrimary, focusedLabelColor = VioletPrimary),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Email Address") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VioletPrimary, focusedLabelColor = VioletPrimary),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("New Password (Optional)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VioletPrimary, focusedLabelColor = VioletPrimary),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (usernameInput.isBlank() || emailInput.isBlank()) {
                            Toast.makeText(context, "Username and email cannot be blank", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isUpdating = true
                        viewModel.updateProfile(usernameInput, emailInput, passwordInput.ifBlank { null }) { success, msg ->
                            isUpdating = false
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            if (success) showProfileDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary),
                    enabled = !isUpdating
                ) {
                    if (isUpdating) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Save")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showProfileDialog = false }, enabled = !isUpdating) {
                    Text("Cancel", color = GrayText)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp)
        )
    }

    // 2. MOTOR CALIBRATION DIALOG
    if (showCalibrationDialog) {
        var stepsX by remember { mutableStateOf(motorStepsX.toString()) }
        var stepsY by remember { mutableStateOf(motorStepsY.toString()) }
        var stepsZ by remember { mutableStateOf(motorStepsZ.toString()) }
        var widthInput by remember { mutableStateOf(bedWidth.toString()) }
        var heightInput by remember { mutableStateOf(bedHeight.toString()) }
        var feedrateInput by remember { mutableStateOf(machineFeedrate.toString()) }
        
        AlertDialog(
            onDismissRequest = { showCalibrationDialog = false },
            title = { Text("Motor & Bed Calibration", fontWeight = FontWeight.Bold, color = DarkText) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Resolution (steps/mm)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = VioletPrimary)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = stepsX,
                            onValueChange = { stepsX = it },
                            label = { Text("X") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = stepsY,
                            onValueChange = { stepsY = it },
                            label = { Text("Y") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = stepsZ,
                            onValueChange = { stepsZ = it },
                            label = { Text("Z") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Bed Size Limits (mm)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = VioletPrimary)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = widthInput,
                            onValueChange = { widthInput = it },
                            label = { Text("Width") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = heightInput,
                            onValueChange = { heightInput = it },
                            label = { Text("Height") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = feedrateInput,
                        onValueChange = { feedrateInput = it },
                        label = { Text("Max Jog Speed (mm/min)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val sX = stepsX.toFloatOrNull() ?: 80f
                        val sY = stepsY.toFloatOrNull() ?: 80f
                        val sZ = stepsZ.toFloatOrNull() ?: 80f
                        val w = widthInput.toFloatOrNull() ?: 200f
                        val h = heightInput.toFloatOrNull() ?: 200f
                        val feed = feedrateInput.toFloatOrNull() ?: 1500f
                        
                        viewModel.updateMachineSettings(sX, sY, sZ, powderFlowRate, w, h, feed)
                        Toast.makeText(context, "Calibration saved successfully", Toast.LENGTH_SHORT).show()
                        showCalibrationDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary)
                ) {
                    Text("Apply")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCalibrationDialog = false }) {
                    Text("Cancel", color = GrayText)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp)
        )
    }

    // 3. POWDER FLOW RATE DIALOG
    if (showFlowRateDialog) {
        var rateVal by remember { mutableStateOf(powderFlowRate) }
        
        AlertDialog(
            onDismissRequest = { showFlowRateDialog = false },
            title = { Text("Powder Flow Adjustments", fontWeight = FontWeight.Bold, color = DarkText) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Powder Extruder Flow: ${rateVal.toInt()}%", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = DarkText)
                    Slider(
                        value = rateVal,
                        onValueChange = { rateVal = it },
                        valueRange = 20f..300f,
                        colors = SliderDefaults.colors(
                            thumbColor = VioletPrimary,
                            activeTrackColor = VioletPrimary,
                            inactiveTrackColor = VioletPrimary.copy(alpha = 0.2f)
                        )
                    )
                    Text("Controls the powder delivery volumetric scale. Standard default speed is 100%.", fontSize = 11.sp, color = GrayText)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateMachineSettings(
                            motorStepsX, motorStepsY, motorStepsZ,
                            rateVal,
                            bedWidth, bedHeight,
                            machineFeedrate
                        )
                        Toast.makeText(context, "Powder flow updated", Toast.LENGTH_SHORT).show()
                        showFlowRateDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFlowRateDialog = false }) {
                    Text("Cancel", color = GrayText)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp)
        )
    }

    // 4. THEME SELECT DIALOG
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Choose Theme", fontWeight = FontWeight.Bold, color = DarkText) },
            text = {
                Column {
                    listOf("System", "Light", "Dark").forEach { option ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setAppThemeMode(option)
                                    showThemeDialog = false
                                }
                                .padding(vertical = 8.dp)
                        ) {
                            RadioButton(
                                selected = (appThemeMode == option),
                                onClick = {
                                    viewModel.setAppThemeMode(option)
                                    showThemeDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = VioletPrimary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = option, color = DarkText, fontSize = 15.sp)
                        }
                    }
                }
            },
            confirmButton = {},
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp)
        )
    }

    // 5. UNITS SELECT DIALOG
    if (showUnitsDialog) {
        AlertDialog(
            onDismissRequest = { showUnitsDialog = false },
            title = { Text("Measurement Units", fontWeight = FontWeight.Bold, color = DarkText) },
            text = {
                Column {
                    listOf("mm", "inches").forEach { option ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setUnitsPreference(option)
                                    showUnitsDialog = false
                                }
                                .padding(vertical = 8.dp)
                        ) {
                            RadioButton(
                                selected = (unitsPreference == option),
                                onClick = {
                                    viewModel.setUnitsPreference(option)
                                    showUnitsDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = VioletPrimary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = if (option == "mm") "Metric (mm)" else "Imperial (inches)", color = DarkText, fontSize = 15.sp)
                        }
                    }
                }
            },
            confirmButton = {},
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp)
        )
    }

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
                // Profile Section (Clickable to Edit)
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showProfileDialog = true }
                ) {
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
                            Text(
                                text = currentUser?.username?.firstOrNull()?.uppercase() ?: "A",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = currentUser?.username ?: "Creator", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkText)
                            Text(text = currentUser?.email ?: "Not logged in", fontSize = 13.sp, color = GrayText)
                        }
                        
                        Icon(Icons.Default.ChevronRight, contentDescription = "Edit Profile", tint = GrayText)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Machine Settings Section
                Text(text = "Machine Settings", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkText, modifier = Modifier.fillMaxWidth().padding(start = 4.dp, bottom = 12.dp))
                
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        SettingItem(
                            icon = Icons.Outlined.Bluetooth,
                            title = "Bluetooth Preferences",
                            subtitle = "Manage paired CNC hardware status",
                            onClick = onNavigateToDevices
                        )
                        HorizontalDivider(color = Color.White.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 16.dp))
                        SettingItem(
                            icon = Icons.Outlined.Speed,
                            title = "Motor Calibration",
                            subtitle = "Steps/mm: X=${motorStepsX.toInt()}, Y=${motorStepsY.toInt()}, Z=${motorStepsZ.toInt()}",
                            onClick = { showCalibrationDialog = true }
                        )
                        HorizontalDivider(color = Color.White.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 16.dp))
                        SettingItem(
                            icon = Icons.Outlined.Polyline,
                            title = "Powder Flow Speed",
                            subtitle = "Volumetric Extrusion: ${powderFlowRate.toInt()}%",
                            onClick = { showFlowRateDialog = true }
                        )
                        HorizontalDivider(color = Color.White.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 16.dp))
                        SettingItem(
                            icon = Icons.Outlined.SystemUpdateAlt,
                            title = "Firmware Update",
                            subtitle = "Version 2.1.4 (Up to date)",
                            onClick = { Toast.makeText(context, "Firmware is already up to date", Toast.LENGTH_SHORT).show() }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                // App Settings Section
                Text(text = "App Settings", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkText, modifier = Modifier.fillMaxWidth().padding(start = 4.dp, bottom = 12.dp))
                
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        SettingItem(
                            icon = Icons.Outlined.DarkMode,
                            title = "Theme Preference",
                            subtitle = "Current: $appThemeMode",
                            onClick = { showThemeDialog = true }
                        )
                        HorizontalDivider(color = Color.White.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 16.dp))
                        SettingItem(
                            icon = Icons.Outlined.Notifications,
                            title = "Notifications",
                            subtitle = if (notificationsEnabled) "Status: Enabled" else "Status: Muted",
                            trailingContent = {
                                Switch(
                                    checked = notificationsEnabled,
                                    onCheckedChange = { viewModel.setNotificationsEnabled(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = VioletPrimary,
                                        uncheckedThumbColor = Color.LightGray,
                                        uncheckedTrackColor = Color.White.copy(alpha = 0.4f)
                                    )
                                )
                            }
                        )
                        HorizontalDivider(color = Color.White.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 16.dp))
                        SettingItem(
                            icon = Icons.Outlined.SquareFoot,
                            title = "Measurement Units",
                            subtitle = if (unitsPreference == "mm") "Metric (mm)" else "Imperial (inches)",
                            onClick = { showUnitsDialog = true }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))
                
                TextButton(onClick = { 
                    viewModel.logout()
                    onNavigateToLogin()
                }) {
                    Text(text = "Log Out", color = Color(0xFFFF3B30), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun SettingItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    trailingContent: @Composable (() -> Unit)? = null,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = trailingContent == null) { onClick() }
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
        
        if (trailingContent != null) {
            trailingContent()
        } else {
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = GrayText)
        }
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
