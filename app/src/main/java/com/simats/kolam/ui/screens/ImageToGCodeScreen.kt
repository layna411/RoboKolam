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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
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
fun ImageToGCodeScreen(onBackClick: () -> Unit, onContinueClick: () -> Unit) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Image to GCode",
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
            StepIndicator()

            Spacer(modifier = Modifier.height(24.dp))

            ProcessingStatusCard(progress = 0.85f)

            Spacer(modifier = Modifier.height(24.dp))

            ConversionSettingsCard()

            Spacer(modifier = Modifier.height(24.dp))

//            ZAxisColorSection()

            Spacer(modifier = Modifier.height(32.dp))

            GradientButtonWithIcon(
                text = "Continue to Preview",
                onClick = onContinueClick
            )
        }
    }
}

@Composable
fun StepIndicator() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        StepItem("Image", Icons.Default.CheckCircle, Color.Green, true)
        Divider(modifier = Modifier.weight(1f).padding(horizontal = 8.dp), color = Color.LightGray)
        StepItem("Processing", Icons.Default.Image, Pink, true, isCurrent = true)
        Divider(modifier = Modifier.weight(1f).padding(horizontal = 8.dp), color = Color.LightGray)
        StepItem("GCode", Icons.Default.GridOn, Color.LightGray, false)
    }
}

@Composable
fun StepItem(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, isCompleted: Boolean, isCurrent: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(if (isCurrent) Color.Transparent else color.copy(alpha = 0.1f), CircleShape)
                .then(if (isCurrent) Modifier.background(brush = Brush.linearGradient(listOf(Orange, Pink)), shape = CircleShape) else Modifier),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = if (isCurrent) Color.White else color, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, fontSize = 10.sp, color = if (isCurrent) Pink else Color.Gray, fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
fun ProcessingStatusCard(progress: Float) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(140.dp)) {
                Canvas(modifier = Modifier.size(120.dp)) {
                    drawArc(
                        color = Color(0xFFF5F5F5),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawArc(
                        brush = Brush.sweepGradient(listOf(Orange, Pink, Orange)),
                        startAngle = -90f,
                        sweepAngle = 360 * progress,
                        useCenter = false,
                        style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                Text(
                    text = "${(progress * 100).toInt()}%",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(text = "Processing...", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(
                text = "Converting image to GCode\nPlease wait a moment",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun ConversionSettingsCard() {
    var detailLevel by remember { mutableStateOf(0.7f) }
    var lineSimplification by remember { mutableStateOf(0.5f) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Conversion Settings", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            SettingRow(
                icon = Icons.Outlined.AutoFixHigh,
                label = "Method",
                content = {
                    Box(
                        modifier = Modifier
                            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Line Art", fontSize = 14.sp)
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                        }
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            SettingRow(
                icon = Icons.Outlined.Architecture,
                label = "Detail Level",
                content = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Slider(
                            value = detailLevel,
                            onValueChange = { detailLevel = it },
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(thumbColor = Pink, activeTrackColor = Pink)
                        )
                        Text(text = "${(detailLevel * 100).toInt()}%", fontSize = 12.sp, color = Pink, fontWeight = FontWeight.Bold, modifier = Modifier.width(40.dp), textAlign = TextAlign.End)
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            SettingRow(
                icon = Icons.Outlined.Timeline,
                label = "Line Simplification",
                content = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Slider(
                            value = lineSimplification,
                            onValueChange = { lineSimplification = it },
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(thumbColor = Pink, activeTrackColor = Pink)
                        )
                        Text(text = "${(lineSimplification * 100).toInt()}%", fontSize = 12.sp, color = Pink, fontWeight = FontWeight.Bold, modifier = Modifier.width(40.dp), textAlign = TextAlign.End)
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            SettingRow(
                icon = Icons.Outlined.AccessTime,
                label = "Estimated Time",
                content = {
                    Text(text = "~ 1 min", fontSize = 14.sp, color = Color.Gray)
                }
            )
        }
    }
}

@Composable
fun SettingRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, content: @Composable () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier.size(32.dp).background(Color(0xFFF5F0FF), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFF7B4DFF), modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = label, fontSize = 14.sp, modifier = Modifier.weight(1f))
        content()
    }
}
