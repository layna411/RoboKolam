package com.simats.kolam.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.kolam.ui.components.GlassCard
import com.simats.kolam.ui.theme.*
import coil.compose.AsyncImage
import com.simats.kolam.viewmodel.KolamViewModel
import com.simats.kolam.models.ImageRecord
import androidx.compose.ui.layout.ContentScale
import com.simats.kolam.api.RetrofitClient
import android.widget.Toast

@Composable
fun HomeScreen(
    viewModel: KolamViewModel,
    onNavigateToUpload: () -> Unit,
    onNavigateToProcessing: () -> Unit,
    onNavigateToDesigns: () -> Unit,
    onNavigateToDevices: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val isConnected by viewModel.isConnected.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val userImages by viewModel.userImages.collectAsState()
    
    Scaffold(
        topBar = { HomeTopBar() },
        bottomBar = { HomeBottomNavigation(onNavigateToDesigns, onNavigateToDevices, onNavigateToSettings) },
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
                .offset(x = 100.dp, y = (-100).dp)
                .size(300.dp)
                .background(VioletPrimary.copy(alpha = 0.15f), CircleShape)
                .blur(80.dp)
            )
            Box(modifier = Modifier
                .offset(x = (-100).dp, y = 400.dp)
                .size(250.dp)
                .background(BlueAccent.copy(alpha = 0.15f), CircleShape)
                .blur(70.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                HomeHeader(username = currentUser?.username ?: "Creator")
                Spacer(modifier = Modifier.height(32.dp))
                CreateNewDesignCard()
                Spacer(modifier = Modifier.height(20.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    SecondaryActionCard(
                        title = "Import Image",
                        subtitle = "image to GCode",
                        icon = Icons.Outlined.Image,
                        iconBg = VioletPrimary.copy(alpha = 0.1f),
                        iconTint = VioletPrimary,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToUpload
                    )
                    SecondaryActionCard(
                        title = "My Designs",
                        subtitle = "View saved designs",
                        icon = Icons.Outlined.Folder,
                        iconBg = TealAccent.copy(alpha = 0.1f),
                        iconTint = TealAccent,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToDesigns
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
                RecentDesignsSection(
                    images = userImages,
                    onImageClick = { img ->
                        viewModel.selectRecentImage(img)
                        onNavigateToProcessing()
                    }
                )

                Spacer(modifier = Modifier.height(32.dp))
                DeviceConnectivityCard(isConnected = isConnected, onClick = onNavigateToDevices)
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar() {
    CenterAlignedTopAppBar(
        title = { Text(text = "RangoliBot", style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DarkText)) },
        actions = {
            IconButton(
                onClick = { },
                modifier = Modifier
                    .padding(end = 8.dp)
                    .background(Color.White.copy(alpha = 0.6f), CircleShape)
                    .size(40.dp)
            ) {
                Icon(Icons.Outlined.Notifications, contentDescription = "Notifications", tint = DarkText)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
    )
}

@Composable
fun HomeHeader(username: String) {
    Column {
        Text(text = "Hello, $username 👋", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = DarkText)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Create, customize and bring your\nrangoli designs to life", fontSize = 15.sp, color = GrayText, lineHeight = 22.sp)
    }
}

@Composable
fun CreateNewDesignCard() {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .background(brush = Brush.linearGradient(listOf(VioletPrimary, VioletSecondary)), shape = RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
            }
            Spacer(modifier = Modifier.width(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Create New Design", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkText)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Start a new rangoli design from scratch", fontSize = 13.sp, color = GrayText)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = GrayText)
        }
    }
}

@Composable
fun SecondaryActionCard(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, iconBg: Color, iconTint: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    GlassCard(modifier = modifier.clickable { onClick() }) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(modifier = Modifier.size(44.dp).background(iconBg, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkText)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = subtitle, fontSize = 12.sp, color = GrayText, lineHeight = 16.sp)
        }
    }
}

@Composable
fun RecentDesignsSection(images: List<ImageRecord>, onImageClick: (ImageRecord) -> Unit) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Recent Designs", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DarkText)
            Text(text = "See All", fontSize = 15.sp, color = VioletPrimary, fontWeight = FontWeight.Medium)
        }
        Spacer(modifier = Modifier.height(16.dp))
        GlassCard {
            Column(modifier = Modifier.padding(16.dp)) {
                if (images.isEmpty()) {
                    Text("No recent designs found", color = GrayText, modifier = Modifier.padding(8.dp))
                } else {
                    images.take(3).forEachIndexed { index, img ->
                        DesignItem(img, onClick = { onImageClick(img) })
                        if (index < minOf(images.size - 1, 2)) {
                            HorizontalDivider(color = Color.White.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 12.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DesignItem(img: ImageRecord, onClick: () -> Unit) {
    val fullUrl = if (img.url.startsWith("http")) {
        img.url
    } else {
        val baseUrl = RetrofitClient.BASE_URL.removeSuffix("/")
        val path = if (img.url.startsWith("/")) img.url else "/${img.url}"
        baseUrl + path
    }
    
    val context = androidx.compose.ui.platform.LocalContext.current
    Row(modifier = Modifier.fillMaxWidth().clickable { 
        onClick()
        Toast.makeText(context, "Loading: $fullUrl", Toast.LENGTH_SHORT).show()
    }, verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(48.dp).background(Color.White, RoundedCornerShape(12.dp)).clip(RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = fullUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                error = androidx.compose.ui.graphics.vector.rememberVectorPainter(Icons.Default.BrokenImage),
                placeholder = androidx.compose.ui.graphics.vector.rememberVectorPainter(Icons.Default.Image)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = img.filename, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkText, maxLines = 1)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = "Status: ${img.status.replaceFirstChar { it.uppercase() }}", fontSize = 13.sp, color = GrayText)
        }
        
        val displayDate = try {
            if (img.created_at.length >= 10) img.created_at.substring(0, 10) else "Recent"
        } catch (e: Exception) {
            "Recent"
        }
        Text(text = displayDate, fontSize = 12.sp, color = GrayText)
    }
}

@Composable
fun DeviceConnectivityCard(isConnected: Boolean = false, onClick: () -> Unit = {}) {
    GlassCard(modifier = Modifier.fillMaxWidth().clickable { onClick() }) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(50.dp).background(Color.White, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Bluetooth, contentDescription = null, tint = VioletPrimary)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "RangoliBot X1", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkText)
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(if (isConnected) Color(0xFF34C759) else Color.Gray, CircleShape))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = if (isConnected) "Connected" else "Disconnected", fontSize = 13.sp, color = if (isConnected) Color(0xFF34C759) else Color.Gray, fontWeight = FontWeight.Medium)
                }
            }
            Box(modifier = Modifier.background(Color.White, RoundedCornerShape(20.dp)).clickable { onClick() }.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(text = "Manage", fontSize = 13.sp, color = VioletPrimary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun HomeBottomNavigation(onDesignsClick: () -> Unit, onDevicesClick: () -> Unit, onSettingsClick: () -> Unit) {
    NavigationBar(containerColor = Color.White.copy(alpha = 0.8f), tonalElevation = 0.dp, modifier = Modifier.background(Color.White.copy(alpha = 0.9f))) {
        NavigationBarItem(
            selected = true,
            onClick = { },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home", fontWeight = FontWeight.Medium) },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = VioletPrimary, selectedTextColor = VioletPrimary, unselectedIconColor = GrayText, unselectedTextColor = GrayText, indicatorColor = VioletPrimary.copy(alpha = 0.1f))
        )
        NavigationBarItem(
            selected = false,
            onClick = onDesignsClick,
            icon = { Icon(Icons.Outlined.Folder, contentDescription = "Designs") },
            label = { Text("Designs", fontWeight = FontWeight.Medium) },
            colors = NavigationBarItemDefaults.colors(unselectedIconColor = GrayText, unselectedTextColor = GrayText)
        )
        NavigationBarItem(
            selected = false,
            onClick = onDevicesClick,
            icon = { Icon(Icons.Outlined.Bluetooth, contentDescription = "Devices") },
            label = { Text("Devices", fontWeight = FontWeight.Medium) },
            colors = NavigationBarItemDefaults.colors(unselectedIconColor = GrayText, unselectedTextColor = GrayText)
        )
        NavigationBarItem(
            selected = false,
            onClick = onSettingsClick,
            icon = { Icon(Icons.Outlined.Settings, contentDescription = "Settings") },
            label = { Text("Settings", fontWeight = FontWeight.Medium) },
            colors = NavigationBarItemDefaults.colors(unselectedIconColor = GrayText, unselectedTextColor = GrayText)
        )
    }
}
