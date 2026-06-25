package com.simats.kolam.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.simats.kolam.ui.components.GlassCard
import com.simats.kolam.ui.theme.*
import com.simats.kolam.viewmodel.KolamViewModel
import com.simats.kolam.api.RetrofitClient
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import android.content.ContentValues
import android.os.Build
import android.provider.MediaStore
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DesignsScreen(
    viewModel: KolamViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToProcessing: () -> Unit,
    onNavigateToDevices: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    val userImages by viewModel.userImages.collectAsState()
    val completedDrawings by viewModel.completedDrawings.collectAsState()
    
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var activeInspectorGCode by remember { mutableStateOf<String?>(null) }
    var activeInspectorTitle by remember { mutableStateOf<String?>(null) }
    
    if (activeInspectorGCode != null) {
        AlertDialog(
            onDismissRequest = { activeInspectorGCode = null },
            title = { Text(text = "${activeInspectorTitle?.substringBeforeLast(".") ?: "Drawing"} - G-Code", fontWeight = FontWeight.ExtraBold, color = DarkText, fontSize = 18.sp) },
            text = {
                Box(modifier = Modifier.fillMaxWidth().height(300.dp).background(Color.Black.copy(alpha = 0.05f), RoundedCornerShape(12.dp)).border(1.dp, Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(12.dp)).padding(12.dp)) {
                    val lines = activeInspectorGCode!!.lines()
                    val displayText = lines.take(150).joinToString("\n") + if (lines.size > 150) "\n... (truncated)" else ""
                    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                        Text(text = displayText, fontSize = 11.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, color = DarkText.copy(alpha = 0.8f))
                    }
                }
            },
            confirmButton = { TextButton(onClick = { activeInspectorGCode = null }) { Text("Close", color = VioletPrimary, fontWeight = FontWeight.Bold) } },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "My Designs", style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DarkText)) },
                actions = { IconButton(onClick = { }, modifier = Modifier.padding(end = 8.dp).background(Color.White.copy(alpha = 0.6f), CircleShape).size(40.dp)) { Icon(Icons.Default.Add, contentDescription = "Add", tint = VioletPrimary) } },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = { DesignsBottomNavigation(onNavigateToHome, onNavigateToDevices, onNavigateToSettings) },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().background(brush = Brush.verticalGradient(colors = listOf(BackgroundStart, BackgroundEnd)))) {
            Box(modifier = Modifier.offset(x = (-50).dp, y = 150.dp).size(250.dp).background(TealAccent.copy(alpha = 0.15f), CircleShape).blur(70.dp))

            Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 20.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color.White.copy(alpha = 0.4f),
                    contentColor = VioletPrimary,
                    indicator = { tabPositions -> TabRowDefaults.SecondaryIndicator(modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]), color = VioletPrimary) },
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color.White.copy(alpha = 0.2f)).padding(2.dp)
                ) {
                    Tab(selected = selectedTabIndex == 0, onClick = { selectedTabIndex = 0 }, text = { Text("Uploads", fontWeight = FontWeight.Bold, fontSize = 13.sp) })
                    Tab(selected = selectedTabIndex == 1, onClick = { selectedTabIndex = 1 }, text = { Text("History", fontWeight = FontWeight.Bold, fontSize = 13.sp) })
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                    if (selectedTabIndex == 0) {
                        if (userImages.isEmpty()) {
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { Text("No uploads found", color = GrayText, fontSize = 14.sp) }
                        } else {
                            userImages.forEach { img ->
                                val fullUrl = if (img.url.startsWith("http")) img.url else "${RetrofitClient.BASE_URL.removeSuffix("/")}/${img.url.removePrefix("/")}"
                                GlassCard(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp).clickable { viewModel.selectRecentImage(img); onNavigateToProcessing() }) {
                                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(50.dp).background(Color.White, RoundedCornerShape(12.dp)).clip(RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                                            AsyncImage(model = fullUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize(), error = androidx.compose.ui.graphics.vector.rememberVectorPainter(Icons.Default.BrokenImage), placeholder = androidx.compose.ui.graphics.vector.rememberVectorPainter(Icons.Default.Image))
                                        }
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(text = img.filename, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkText, maxLines = 1)
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(text = "Status: ${img.status.replaceFirstChar { it.uppercase() }}", fontSize = 12.sp, color = GrayText)
                                        }
                                        IconButton(onClick = {}) { Icon(Icons.Default.MoreVert, contentDescription = null, tint = DarkText) }
                                    }
                                }
                            }
                        }
                    } else {
                        if (completedDrawings.isEmpty()) {
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { Text("No completed drawings history yet", color = GrayText, fontSize = 14.sp, textAlign = TextAlign.Center) }
                        } else {
                            completedDrawings.forEach { drawing ->
                                val fullUrl = if (drawing.image_url.startsWith("http")) drawing.image_url else "${RetrofitClient.BASE_URL.removeSuffix("/")}/${drawing.image_url.removePrefix("/")}"
                                GlassCard(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(modifier = Modifier.size(60.dp).background(Color.White, RoundedCornerShape(12.dp)).clip(RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                                                AsyncImage(model = fullUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize(), error = androidx.compose.ui.graphics.vector.rememberVectorPainter(Icons.Default.BrokenImage), placeholder = androidx.compose.ui.graphics.vector.rememberVectorPainter(Icons.Default.Image))
                                            }
                                            Spacer(modifier = Modifier.width(16.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(text = drawing.filename, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkText, maxLines = 1)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                val durationStr = if (drawing.time_taken / 60 > 0) "${drawing.time_taken / 60}m ${drawing.time_taken % 60}s" else "${drawing.time_taken}s"
                                                Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Timer, null, tint = VioletPrimary, modifier = Modifier.size(13.dp)); Spacer(modifier = Modifier.width(4.dp)); Text(text = "Time taken: $durationStr", fontSize = 12.sp, color = GrayText, fontWeight = FontWeight.Medium) }
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.CalendarToday, null, tint = VioletSecondary, modifier = Modifier.size(11.dp)); Spacer(modifier = Modifier.width(4.dp)); Text(text = "Date: ${drawing.created_at.take(16)}", fontSize = 11.sp, color = GrayText) }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Button(onClick = { activeInspectorGCode = drawing.gcode; activeInspectorTitle = drawing.filename }, colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary.copy(alpha = 0.1f), contentColor = VioletPrimary), shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f)) { Icon(Icons.Default.Visibility, null, modifier = Modifier.size(15.dp)); Spacer(modifier = Modifier.width(6.dp)); Text("View G-Code", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                                            Button(onClick = { downloadGCodeFile(context, drawing.filename, drawing.gcode) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF34C759).copy(alpha = 0.15f), contentColor = Color(0xFF34C759)), shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f)) { Icon(Icons.Default.Download, null, modifier = Modifier.size(15.dp)); Spacer(modifier = Modifier.width(6.dp)); Text("Download", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                                        }
                                    }
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
    NavigationBar(containerColor = Color.White.copy(alpha = 0.8f), tonalElevation = 0.dp, modifier = Modifier.background(Color.White.copy(alpha = 0.9f))) {
        NavigationBarItem(selected = false, onClick = onHomeClick, icon = { Icon(Icons.Outlined.Home, null) }, label = { Text("Home", fontWeight = FontWeight.Medium) }, colors = NavigationBarItemDefaults.colors(unselectedIconColor = GrayText, unselectedTextColor = GrayText))
        NavigationBarItem(selected = true, onClick = { }, icon = { Icon(Icons.Default.Folder, null) }, label = { Text("Designs", fontWeight = FontWeight.Medium) }, colors = NavigationBarItemDefaults.colors(selectedIconColor = VioletPrimary, selectedTextColor = VioletPrimary, unselectedIconColor = GrayText, unselectedTextColor = GrayText, indicatorColor = VioletPrimary.copy(alpha = 0.1f)))
        NavigationBarItem(selected = false, onClick = onDevicesClick, icon = { Icon(Icons.Outlined.Bluetooth, null) }, label = { Text("Devices", fontWeight = FontWeight.Medium) }, colors = NavigationBarItemDefaults.colors(unselectedIconColor = GrayText, unselectedTextColor = GrayText))
        NavigationBarItem(selected = false, onClick = onSettingsClick, icon = { Icon(Icons.Outlined.Settings, null) }, label = { Text("Settings", fontWeight = FontWeight.Medium) }, colors = NavigationBarItemDefaults.colors(unselectedIconColor = GrayText, unselectedTextColor = GrayText))
    }
}

fun downloadGCodeFile(context: Context, originalFilename: String, gcodeText: String) {
    if (gcodeText.isEmpty() || gcodeText.startsWith("; No image") || gcodeText.startsWith("; API Error")) { Toast.makeText(context, "No valid G-Code", Toast.LENGTH_SHORT).show(); return }
    try {
        val filename = "${originalFilename.substringBeforeLast(".")}_${System.currentTimeMillis()}.gcode"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply { put(MediaStore.MediaColumns.DISPLAY_NAME, filename); put(MediaStore.MediaColumns.MIME_TYPE, "text/plain"); put(MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_DOWNLOADS) }
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) { resolver.openOutputStream(uri)?.use { it.write(gcodeText.toByteArray()) }; Toast.makeText(context, "Saved to Downloads", Toast.LENGTH_LONG).show() }
        } else {
            val file = File(android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS), filename)
            file.writeText(gcodeText); Toast.makeText(context, "Saved to Downloads", Toast.LENGTH_LONG).show()
        }
    } catch (e: Exception) { Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show() }
}
