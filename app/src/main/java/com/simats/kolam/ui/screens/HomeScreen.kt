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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.kolam.ui.theme.Orange
import com.simats.kolam.ui.theme.Pink

@Composable
fun HomeScreen(
    onNavigateToUpload: () -> Unit,
    onNavigateToDesigns: () -> Unit,
    onNavigateToDevices: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    Scaffold(
        topBar = { HomeTopBar() },
        bottomBar = { HomeBottomNavigation(onNavigateToDesigns, onNavigateToDevices, onNavigateToSettings) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFFEF9FB))
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            HomeHeader()

            Spacer(modifier = Modifier.height(24.dp))

            CreateNewDesignCard()

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                SecondaryActionCard(
                    title = "Import Image",
                    subtitle = "Convert an image to GCode",
                    icon = Icons.Outlined.Image,
                    iconBg = Color(0xFFF5F0FF),
                    iconTint = Color(0xFF7B4DFF),
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToUpload
                )
                SecondaryActionCard(
                    title = "My Designs",
                    subtitle = "View and manage your saved designs",
                    icon = Icons.Outlined.Description,
                    iconBg = Color(0xFFFFF2E8),
                    iconTint = Color(0xFFFF9248),
                    modifier = Modifier.weight(1f),
                    onClick = { }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            RecentDesignsSection()

            Spacer(modifier = Modifier.height(24.dp))

            DeviceConnectivityCard()
            
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar() {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "RangoliBot",
                style = TextStyle(
                    fontSize = 24.sp,
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
                Icon(Icons.Default.NotificationsNone, contentDescription = "Notifications")
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.Transparent
        )
    )
}

@Composable
fun HomeHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Hello, Ananya 👋",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Create, customize and bring your\nrangoli designs to life",
                fontSize = 14.sp,
                color = Color.Gray,
                lineHeight = 20.sp
            )
        }
        
        // Header Mandala Placeholder
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(
                    brush = Brush.sweepGradient(listOf(Pink, Orange, Color.Magenta, Pink)),
                    shape = CircleShape,
                    alpha = 0.1f
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.AutoAwesome,
                contentDescription = null,
                modifier = Modifier.size(60.dp),
                tint = Pink.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun CreateNewDesignCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .border(1.dp, Pink.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Pink.copy(alpha = 0.05f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(brush = Brush.linearGradient(listOf(Orange, Pink)), shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Create New Design", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(text = "Start a new rangoli design from scratch", fontSize = 12.sp, color = Color.Gray)
            }
            
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Pink)
        }
    }
}

@Composable
fun SecondaryActionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBg: Color,
    iconTint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconBg, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = subtitle, fontSize = 11.sp, color = Color.Gray, lineHeight = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.align(Alignment.End).size(16.dp)
            )
        }
    }
}

@Composable
fun RecentDesignsSection() {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Recent Designs", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(text = "View all", fontSize = 14.sp, color = Pink)
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        DesignItem("Festival Rangoli", "12,842 lines  •  3 Colors", "Today, 9:30 AM", Color.Magenta)
        DesignItem("Simple Kolam", "8,156 lines  •  2 Colors", "Yesterday, 6:15 PM", Color(0xFF7B4DFF))
        DesignItem("Lotus Design", "15,320 lines  •  4 Colors", "May 12, 2024", Color(0xFFFF9248))
    }
}

@Composable
fun DesignItem(title: String, stats: String, date: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(color.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = color.copy(alpha = 0.5f))
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text(text = stats, fontSize = 11.sp, color = Color.Gray)
        }
        
        Column(horizontalAlignment = Alignment.End) {
            Text(text = date, fontSize = 10.sp, color = Color.Gray)
            IconButton(onClick = { }, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.MoreVert, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun DeviceConnectivityCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF6FFF9)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2DCC70).copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.SmartToy, contentDescription = null, tint = Color(0xFF2DCC70))
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "RangoliBot X1", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(6.dp).background(Color(0xFF2DCC70), CircleShape))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Connected", fontSize = 11.sp, color = Color(0xFF2DCC70))
                }
            }
            
            TextButton(
                onClick = { },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
            ) {
                Text(text = "Connect Device", fontSize = 12.sp, color = Color(0xFF2DCC70), fontWeight = FontWeight.Bold)
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF2DCC70), modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun HomeBottomNavigation(onDesignsClick: () -> Unit, onDevicesClick: () -> Unit, onSettingsClick: () -> Unit) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = true,
            onClick = { },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") },
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
            onClick = onDesignsClick,
            icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "Designs") },
            label = { Text("Designs") }
        )
        NavigationBarItem(
            selected = false,
            onClick = onDevicesClick,
            icon = { Icon(Icons.Default.SmartToy, contentDescription = "Devices") },
            label = { Text("Devices") }
        )
        NavigationBarItem(
            selected = false,
            onClick = onSettingsClick,
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            label = { Text("Settings") }
        )
    }
}
