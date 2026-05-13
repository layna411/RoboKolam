package com.simats.kolam.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.kolam.ui.theme.Orange
import com.simats.kolam.ui.theme.Pink

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetColorsScreen(onBackClick: () -> Unit, onContinueClick: () -> Unit) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Set Colors (Z-Axis)",
                        style = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            brush = Brush.horizontalGradient(listOf(Orange, Pink))
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Outlined.HelpOutline, contentDescription = "Help")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFFBFBFB))
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Assign colors to Z-axis levels.",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            HowItWorksCard()

            Spacer(modifier = Modifier.height(24.dp))

            ThreeColorSystemSection()

            Spacer(modifier = Modifier.height(24.dp))

            ZAxisPreviewSection()

            Spacer(modifier = Modifier.height(32.dp))

            GradientButtonWithIcon(
                text = "Continue to Connect Device",
                onClick = onContinueClick
            )
        }
    }
}

@Composable
fun HowItWorksCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "How it works", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "The machine will change color automatically based on the Z-axis height during drawing.",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    lineHeight = 18.sp
                )
            }
            
            // Small Z-Axis Diagram
            Box(modifier = Modifier.size(80.dp), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val centerX = size.width / 2
                    drawLine(Color.Black, Offset(centerX, size.height - 10f), Offset(centerX, 10f), strokeWidth = 2f)
                    drawLine(Color.Black, Offset(centerX - 15f, size.height - 10f), Offset(centerX + 15f, size.height - 10f), strokeWidth = 4f)
                    
                    val points = listOf(
                        Triple(20f, Color(0xFFFF1493), "0.5mm"),
                        Triple(45f, Color(0xFF32CD32), "1.5mm"),
                        Triple(70f, Color(0xFFFFD700), "2.5mm")
                    )
                    
                    points.forEach { (y, color, _) ->
                        drawCircle(color, radius = 4f, center = Offset(centerX, y))
                        drawLine(color, Offset(centerX, y), Offset(centerX + 20f, y), strokeWidth = 1f)
                    }
                }
            }
        }
    }
}

@Composable
fun ThreeColorSystemSection() {
    Column {
        Text(text = "3 Color System", fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
        
        ColorControlItem(1, "Z = 0.5 mm", "Color 1 (Pink)", Color(0xFFFF1493), "#FF2D86", 0.5f, "0.0 mm", "1.0 mm")
        Spacer(modifier = Modifier.height(12.dp))
        ColorControlItem(2, "Z = 1.5 mm", "Color 2 (Green)", Color(0xFF32CD32), "#22C55E", 0.5f, "1.0 mm", "2.0 mm")
        Spacer(modifier = Modifier.height(12.dp))
        ColorControlItem(3, "Z = 2.5 mm", "Color 3 (Yellow)", Color(0xFFFFD700), "#FACC15", 0.5f, "2.0 mm", "3.0 mm")
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F7FF)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF7B4DFF).copy(alpha = 0.1f))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(32.dp).background(Color(0xFFEDE7FF), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.SwapVert, contentDescription = null, tint = Color(0xFF7B4DFF), modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = "Z-Axis Range", fontSize = 12.sp, color = Color.Gray)
                        Text(text = "0.0 mm to 3.0 mm", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Total Color Changes", fontSize = 10.sp, color = Color.Gray)
                    Text(text = "3", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7B4DFF))
                }
            }
        }
    }
}

@Composable
fun ColorControlItem(index: Int, zLabel: String, colorName: String, color: Color, hex: String, initialValue: Float, min: String, max: String) {
    var sliderValue by remember { mutableStateOf(initialValue) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(24.dp).background(color, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = index.toString(), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = zLabel, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
                    Text(text = colorName, fontSize = 11.sp, color = Color.Gray)
                }
                Row(
                    modifier = Modifier
                        .background(Color(0xFFF9F9F9), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(16.dp).background(color, RoundedCornerShape(4.dp)))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = hex, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = min, fontSize = 10.sp, color = Color.Gray)
                Slider(
                    value = sliderValue,
                    onValueChange = { sliderValue = it },
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                    colors = SliderDefaults.colors(thumbColor = color, activeTrackColor = color)
                )
                Text(text = max, fontSize = 10.sp, color = Color.Gray)
            }
            Text(
                text = String.format("%.1f", sliderValue * (max.split(" ")[0].toFloat() - min.split(" ")[0].toFloat()) + min.split(" ")[0].toFloat()),
                fontSize = 10.sp,
                color = Color.Gray,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ZAxisPreviewSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Preview (Z-Axis)", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                // Colored Mandala Placeholder
                Box(modifier = Modifier.size(120.dp), contentAlignment = Alignment.Center) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val center = Offset(size.width / 2, size.height / 2)
                        drawCircle(Color(0xFFFF1493).copy(alpha = 0.6f), radius = 55f, center = center, style = Stroke(width = 2f))
                        drawCircle(Color(0xFF32CD32).copy(alpha = 0.6f), radius = 40f, center = center, style = Stroke(width = 2f))
                        drawCircle(Color(0xFFFFD700).copy(alpha = 0.6f), radius = 25f, center = center, style = Stroke(width = 2f))
                    }
                }
                
                Spacer(modifier = Modifier.width(24.dp))
                
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    PreviewLegendItem(Color(0xFFFF1493), "Z = 0.5 mm", "Color 1 (Pink)", "12,842 lines")
                    PreviewLegendItem(Color(0xFF32CD32), "Z = 1.5 mm", "Color 2 (Green)", "18,765 lines")
                    PreviewLegendItem(Color(0xFFFFD700), "Z = 2.5 mm", "Color 3 (Yellow)", "9,532 lines")
                }
            }
        }
    }
}

@Composable
fun PreviewLegendItem(color: Color, zLabel: String, name: String, lines: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.size(10.dp).background(color, CircleShape))
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = zLabel, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text(text = name, fontSize = 8.sp, color = Color.Gray)
        }
        Text(text = lines, fontSize = 10.sp, color = color, fontWeight = FontWeight.Bold)
    }
}
