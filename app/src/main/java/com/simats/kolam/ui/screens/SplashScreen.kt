package com.simats.kolam.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.kolam.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    LaunchedEffect(key1 = true) {
        delay(2000)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(BackgroundStart, BackgroundEnd)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Decorative background elements for Glassmorphism effect
        Box(modifier = Modifier
            .offset(x = (-50).dp, y = (-50).dp)
            .size(250.dp)
            .background(VioletPrimary.copy(alpha = 0.25f), CircleShape)
            .blur(60.dp)
        )
        Box(modifier = Modifier
            .offset(x = 100.dp, y = 200.dp)
            .size(200.dp)
            .background(TealAccent.copy(alpha = 0.2f), CircleShape)
            .blur(80.dp)
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(
                        brush = Brush.linearGradient(listOf(VioletPrimary, VioletSecondary)),
                        shape = RoundedCornerShape(24.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(50.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "RangoliBot",
                style = TextStyle(
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Smart Automation Series",
                fontSize = 14.sp,
                color = GrayText
            )
        }
    }
}
