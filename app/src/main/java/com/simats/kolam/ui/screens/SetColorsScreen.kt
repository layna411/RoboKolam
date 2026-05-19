package com.simats.kolam.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.kolam.ui.components.GlassCard
import com.simats.kolam.ui.components.GradientButton
import com.simats.kolam.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetColorsScreen(onBackClick: () -> Unit, onContinueClick: () -> Unit) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Color Mapping",
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
                .offset(x = 100.dp, y = 200.dp)
                .size(250.dp)
                .background(TealAccent.copy(alpha = 0.15f), CircleShape)
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
                Text(
                    text = "Assign colors to specific Z-Axis tools.",
                    fontSize = 14.sp,
                    color = GrayText,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                HowItWorksCard()

                Spacer(modifier = Modifier.height(24.dp))

                ThreeColorSystemSection()

                Spacer(modifier = Modifier.height(24.dp))

                ZAxisPreviewSection()

                Spacer(modifier = Modifier.height(32.dp))

                GradientButton(
                    text = "Confirm & Connect",
                    onClick = onContinueClick
                )
                
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@Composable
fun HowItWorksCard() {
    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Z-Axis Dispenser Mapping", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkText)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "The machine uses specific Z-Axis commands (Z1, Z2, Z3) to select different color powder dispensers.",
                    fontSize = 13.sp,
                    color = GrayText,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun ThreeColorSystemSection() {
    Column {
        Text(text = "Machine Colors", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkText, modifier = Modifier.padding(bottom = 16.dp, start = 4.dp))
        
        ColorControlItem(1, "Tool Z1", "Red Dispenser", Color(0xFFFF3B30), "#FF3B30")
        Spacer(modifier = Modifier.height(12.dp))
        ColorControlItem(2, "Tool Z2", "Yellow Dispenser", Color(0xFFFFCC00), "#FFCC00")
        Spacer(modifier = Modifier.height(12.dp))
        ColorControlItem(3, "Tool Z3", "Blue Dispenser", Color(0xFF007AFF), "#007AFF")
    }
}

@Composable
fun ColorControlItem(index: Int, zLabel: String, colorName: String, color: Color, hex: String) {
    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(32.dp).background(color, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = index.toString(), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = zLabel, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkText)
                Text(text = colorName, fontSize = 12.sp, color = GrayText)
            }
            Box(
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(text = hex, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
            }
        }
    }
}

@Composable
fun ZAxisPreviewSection() {
    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(text = "Layer Separation Preview", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkText)
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                // Colored Mandala Placeholder
                Box(modifier = Modifier.size(120.dp), contentAlignment = Alignment.Center) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val center = Offset(size.width / 2, size.height / 2)
                        drawCircle(Color(0xFFFF3B30), radius = 55f, center = center, style = Stroke(width = 3f))
                        drawCircle(Color(0xFFFFCC00), radius = 40f, center = center, style = Stroke(width = 3f))
                        drawCircle(Color(0xFF007AFF), radius = 25f, center = center, style = Stroke(width = 3f))
                    }
                }
                
                Spacer(modifier = Modifier.width(24.dp))
                
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    PreviewLegendItem(Color(0xFFFF3B30), "Outer Layer", "Tool Z1 (Red)")
                    PreviewLegendItem(Color(0xFFFFCC00), "Mid Layer", "Tool Z2 (Yellow)")
                    PreviewLegendItem(Color(0xFF007AFF), "Inner Layer", "Tool Z3 (Blue)")
                }
            }
        }
    }
}

@Composable
fun PreviewLegendItem(color: Color, zLabel: String, name: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.size(12.dp).background(color, CircleShape))
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = zLabel, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DarkText)
            Text(text = name, fontSize = 10.sp, color = GrayText)
        }
    }
}
