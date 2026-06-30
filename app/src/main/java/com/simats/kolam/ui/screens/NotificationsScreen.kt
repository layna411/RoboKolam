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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import com.simats.kolam.ui.components.GlassCard
import com.simats.kolam.ui.theme.*
import com.simats.kolam.viewmodel.KolamViewModel
import com.simats.kolam.api.RetrofitClient
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    viewModel: KolamViewModel,
    onBackClick: () -> Unit
) {
    val completedDrawings by viewModel.completedDrawings.collectAsState()
    val hasUnread by viewModel.hasUnreadNotifications.collectAsState()
    
    LaunchedEffect(Unit) {
        delay(500) // Small delay so the user can see the "new" status before it clears
        viewModel.markNotificationsAsRead()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Notifications",
                        style = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkText
                        )
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .background(Color.White.copy(alpha = 0.6f), CircleShape)
                            .size(40.dp)
                    ) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Back", tint = DarkText)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        },
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
                .offset(x = (-50).dp, y = 100.dp)
                .size(200.dp)
                .background(VioletPrimary.copy(alpha = 0.1f), CircleShape)
                .blur(60.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                if (completedDrawings.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Outlined.NotificationsNone, null, modifier = Modifier.size(64.dp), tint = GrayText.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("No notifications yet", color = GrayText)
                        }
                    }
                } else {
                    completedDrawings.forEach { drawing ->
                        NotificationItem(drawing, isNew = hasUnread)
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationItem(drawing: com.simats.kolam.models.CompletedDrawingRecord, isNew: Boolean) {
    val fullUrl = if (drawing.image_url.startsWith("http")) {
        drawing.image_url
    } else {
        val baseUrl = RetrofitClient.BASE_URL.removeSuffix("/")
        val path = if (drawing.image_url.startsWith("/")) drawing.image_url else "/${drawing.image_url}"
        baseUrl + path
    }

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
                    modifier = Modifier.fillMaxSize(),
                    error = androidx.compose.ui.graphics.vector.rememberVectorPainter(Icons.Default.Done),
                    placeholder = androidx.compose.ui.graphics.vector.rememberVectorPainter(Icons.Default.Image)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Project Completed!",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )
                Text(
                    text = "${drawing.filename} has been drawn successfully.",
                    fontSize = 13.sp,
                    color = GrayText,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = drawing.created_at.take(16),
                    fontSize = 11.sp,
                    color = VioletPrimary,
                    fontWeight = FontWeight.Medium
                )
            }
            
            if (isNew) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(VioletPrimary, CircleShape)
                )
            }
        }
    }
}
