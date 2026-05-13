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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.kolam.ui.theme.Orange
import com.simats.kolam.ui.theme.Pink

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GCodePreviewScreen(onBackClick: () -> Unit, onContinueClick: () -> Unit) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "GCode Preview",
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
            GCodeStepIndicator()

            Spacer(modifier = Modifier.height(24.dp))

            Preview3DCard()

            Spacer(modifier = Modifier.height(16.dp))

            StatisticsRow()

            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                GCodeLinesCard(modifier = Modifier.weight(1f))
                ColorMappingCard(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(32.dp))

            GradientButtonWithIcon(
                text = "Continue to Set Colors (Z-Axis)",
                onClick = onContinueClick
            )
        }
    }
}

@Composable
fun GCodeStepIndicator() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        StepItem("Image", Icons.Default.CheckCircle, Color.Green, true)
        HorizontalDivider(modifier = Modifier.weight(1f).padding(horizontal = 8.dp), color = Color.LightGray)
        StepItem("Processing", Icons.Default.CheckCircle, Color.Green, true)
        HorizontalDivider(modifier = Modifier.weight(1f).padding(horizontal = 8.dp), color = Color.LightGray)
        StepItem("GCode", Icons.Default.Description, Pink, true, isCurrent = true)
    }
}

@Composable
fun Preview3DCard() {
    Card(
        modifier = Modifier.fillMaxWidth().height(300.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Preview", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Box(
                    modifier = Modifier
                        .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ViewInAr, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("3D View", fontSize = 12.sp)
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }

            // Isometric Grid with Mandala Placeholder
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2, size.height / 2)
                    
                    // Draw simplified isometric grid
                    val gridColor = Color.LightGray.copy(alpha = 0.3f)
                    for (i in -5..5) {
                        drawLine(gridColor, Offset(center.x + i * 40 - 200, center.y + i * 20 - 100), Offset(center.x + i * 40 + 200, center.y + i * 20 + 100), strokeWidth = 1f)
                        drawLine(gridColor, Offset(center.x - i * 40 - 200, center.y + i * 20 + 100), Offset(center.x - i * 40 + 200, center.y + i * 20 - 100), strokeWidth = 1f)
                    }

                    // Draw Axes
                    drawLine(Color.Blue, center, Offset(center.x, center.y - 100), strokeWidth = 2f) // Z
                    drawLine(Color.Green, center, Offset(center.x - 150, center.y + 75), strokeWidth = 2f) // Y
                    drawLine(Color.Red, center, Offset(center.x + 150, center.y + 75), strokeWidth = 2f) // X
                    
                    // Simplified Mandala Shape
                    drawCircle(Pink.copy(alpha = 0.3f), radius = 80f, center = center, style = Stroke(width = 2f))
                    drawCircle(Orange.copy(alpha = 0.3f), radius = 60f, center = center, style = Stroke(width = 2f))
                }
                
                // Axis Labels
                Text("Z", color = Color.Blue, modifier = Modifier.align(Alignment.Center).offset(y = (-110).dp), fontWeight = FontWeight.Bold)
                Text("Y", color = Color.Green, modifier = Modifier.align(Alignment.Center).offset(x = (-160).dp, y = 85.dp), fontWeight = FontWeight.Bold)
                Text("X", color = Color.Red, modifier = Modifier.align(Alignment.Center).offset(x = 160.dp, y = 85.dp), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun StatisticsRow() {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        StatItem(Icons.Outlined.Architecture, "Total Lines", "12,842")
        StatItem(Icons.Outlined.CropFree, "Dimensions", "400 x 400 mm")
        StatItem(Icons.Outlined.AccessTime, "Estimated Time", "2h 35m")
        StatItem(Icons.Outlined.Palette, "Color Changes (Z)", "3")
    }
}

@Composable
fun StatItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Column(horizontalAlignment = Alignment.Start) {
        Icon(icon, contentDescription = null, tint = Pink, modifier = Modifier.size(18.dp))
        Text(text = label, fontSize = 10.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
    }
}

@Composable
fun GCodeLinesCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = "GCode (First 20 Lines)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(vertical = 8.dp)
                    .background(Color(0xFFF9F9F9), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Text(
                    text = "; Rangoli GCode\n; Generated on 20 May 2024\nG21 ; Set units to mm\nG90 ; Absolute positioning\nG28 ; Home all axes\n; --- Start Drawing ---\nG1 Z0.5 F300 ; Color 1 (Pink)\nG1 X10.00 Y10.00 F1200\nG1 X20.45 Y10.00\nG1 X20.45 Y20.45\nG1 X10.00 Y20.45\nG1 Z1.5 ; Color 2 (Green)\n...",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 14.sp
                )
            }
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Pink)
                ) {
                    Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Full GCode", fontSize = 10.sp)
                }
                OutlinedButton(
                    onClick = { },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Pink)
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Export", fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
fun ColorMappingCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = "Z-Axis Color Mapping", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(text = "Z height is used to apply colors", fontSize = 10.sp, color = Color.Gray)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Box(modifier = Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
                // Simplified diagram
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val lineX = 80f
                    
                    // Nozzle representation
                    drawRect(Color.Gray, Offset(lineX - 10f, 0f), size = androidx.compose.ui.geometry.Size(20f, 40f))
                    
                    // Vertical path
                    drawLine(Color.LightGray, Offset(lineX, 40f), Offset(lineX, 160f))
                    
                    // Points and labels
                    val points = listOf(
                        Triple(60f, Color(0xFFFF1493), "Z = 0.5mm"),
                        Triple(100f, Color(0xFF32CD32), "Z = 1.5mm"),
                        Triple(140f, Color(0xFFFFD700), "Z = 2.5mm"),
                        Triple(170f, Color.Gray, "Z = 0mm")
                    )
                    
                    points.forEach { (y, color, label) ->
                        drawCircle(color, radius = 6f, center = Offset(lineX, y))
                    }
                }
                
                Column(modifier = Modifier.fillMaxSize().padding(start = 60.dp), verticalArrangement = Arrangement.SpaceEvenly) {
                    MappingItem(Color(0xFFFF1493), "Z = 0.5mm", "Color 1 (Pink)")
                    MappingItem(Color(0xFF32CD32), "Z = 1.5mm", "Color 2 (Green)")
                    MappingItem(Color(0xFFFFD700), "Z = 2.5mm", "Color 3 (Yellow)")
                    MappingItem(Color.Gray, "Z = 0mm", "Base (No Color)")
                }
            }
        }
    }
}

@Composable
fun MappingItem(color: Color, zValue: String, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(12.dp).background(color, CircleShape))
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = zValue, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text(text = label, fontSize = 8.sp, color = Color.Gray)
        }
    }
}
