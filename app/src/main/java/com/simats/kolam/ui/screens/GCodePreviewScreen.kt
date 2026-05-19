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
import androidx.compose.ui.draw.clip
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
import com.simats.kolam.ui.components.GlassCard
import com.simats.kolam.ui.components.GradientButton
import com.simats.kolam.ui.theme.*
import com.simats.kolam.viewmodel.KolamViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GCodePreviewScreen(
    viewModel: KolamViewModel,
    onBackClick: () -> Unit,
    onContinueClick: () -> Unit
) {
    val generatedGCode by viewModel.generatedGCode.collectAsState()
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "G-Code Preview",
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
                GCodeStepIndicator()

                Spacer(modifier = Modifier.height(24.dp))

                Preview3DCard()

                Spacer(modifier = Modifier.height(20.dp))

                StatisticsRow()

                Spacer(modifier = Modifier.height(24.dp))

                GCodeLinesCard(generatedGCode)

                Spacer(modifier = Modifier.height(24.dp))

                ColorMappingCard()

                Spacer(modifier = Modifier.height(32.dp))

                GradientButton(
                    text = "Send to Machine",
                    onClick = onContinueClick
                )
                
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@Composable
fun GCodeStepIndicator() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        StepItem("Process", Icons.Default.CheckCircle, Color(0xFF34C759), true)
        HorizontalDivider(modifier = Modifier.weight(1f).padding(horizontal = 8.dp), color = VioletPrimary.copy(alpha = 0.3f))
        StepItem("Vectorize", Icons.Default.CheckCircle, Color(0xFF34C759), true)
        HorizontalDivider(modifier = Modifier.weight(1f).padding(horizontal = 8.dp), color = VioletPrimary.copy(alpha = 0.3f))
        StepItem("G-Code", Icons.Default.Description, VioletPrimary, true, isCurrent = true)
    }
}

@Composable
fun StepItem(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, isCompleted: Boolean, isCurrent: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(if (isCurrent) color.copy(alpha = 0.2f) else color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = if (isCurrent) color else Color.White, modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, fontSize = 10.sp, fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium, color = if (isCurrent) DarkText else GrayText)
    }
}

@Composable
fun Preview3DCard() {
    GlassCard(
        modifier = Modifier.fillMaxWidth().height(320.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Live Toolpath Simulation", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkText)
                Box(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ViewInAr, contentDescription = null, modifier = Modifier.size(16.dp), tint = VioletPrimary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("3D View", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = VioletPrimary)
                    }
                }
            }

            // Interactive Toolpath Canvas Placeholder
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2, size.height / 2)
                    
                    // CNC Workspace Bed
                    drawRect(
                        color = Color.White.copy(alpha = 0.5f),
                        topLeft = Offset(center.x - 120, center.y - 80),
                        size = androidx.compose.ui.geometry.Size(240f, 160f)
                    )
                    
                    drawRect(
                        color = VioletPrimary.copy(alpha = 0.2f),
                        topLeft = Offset(center.x - 120, center.y - 80),
                        size = androidx.compose.ui.geometry.Size(240f, 160f),
                        style = Stroke(2f)
                    )

                    // Kolam Path
                    drawCircle(Color(0xFFFF3B30), radius = 60f, center = center, style = Stroke(width = 3f))
                    drawCircle(Color(0xFFFFCC00), radius = 45f, center = center, style = Stroke(width = 3f))
                    drawCircle(Color(0xFF007AFF), radius = 30f, center = center, style = Stroke(width = 3f))
                    
                    // Toolhead Simulation
                    drawCircle(Color.Black, radius = 8f, center = Offset(center.x + 45f, center.y))
                    drawLine(Color.DarkGray, Offset(center.x + 45f, center.y), Offset(center.x + 45f, center.y - 60f), strokeWidth = 4f)
                }
            }
        }
    }
}

@Composable
fun StatisticsRow() {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        StatItem(Icons.Outlined.Timeline, "Toolpath", "15,204 mm")
        StatItem(Icons.Outlined.Speed, "Est. Time", "45 min")
        StatItem(Icons.Outlined.Palette, "Colors", "3")
        StatItem(Icons.Outlined.Layers, "Layers", "5")
    }
}

@Composable
fun StatItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    GlassCard(modifier = Modifier.width(80.dp).height(80.dp)) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize().padding(8.dp)
        ) {
            Icon(icon, contentDescription = null, tint = VioletPrimary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = label, fontSize = 9.sp, color = GrayText)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DarkText)
        }
    }
}

@Composable
fun GCodeLinesCard(gCodeText: String) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Generated G-Code", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkText)
                Icon(Icons.Default.ContentCopy, contentDescription = null, tint = VioletPrimary, modifier = Modifier.size(18.dp))
            }
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .padding(vertical = 12.dp)
                    .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = gCodeText.ifEmpty { "; No GCode generated yet" },
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color.DarkGray,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun ColorMappingCard() {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(text = "Machine Tool Mapping", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkText)
            Text(text = "Z commands are used to select colors", fontSize = 12.sp, color = GrayText)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                MappingItem(Color(0xFFFF3B30), "Z1", "Red Dispenser")
                MappingItem(Color(0xFFFFCC00), "Z2", "Yellow Dispenser")
                MappingItem(Color(0xFF007AFF), "Z3", "Blue Dispenser")
            }
        }
    }
}

@Composable
fun MappingItem(color: Color, zValue: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(20.dp).background(color, CircleShape))
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = zValue, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = DarkText)
        Text(text = label, fontSize = 10.sp, color = GrayText)
    }
}
