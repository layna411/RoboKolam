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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.kolam.ui.components.GlassCard
import com.simats.kolam.ui.components.GradientButton
import com.simats.kolam.ui.theme.*
import com.simats.kolam.viewmodel.KolamViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageToGCodeScreen(
    viewModel: KolamViewModel,
    onBackClick: () -> Unit,
    onContinueClick: () -> Unit
) {
    val progress by viewModel.processingProgress.collectAsState()
    val isComplete by viewModel.isProcessingComplete.collectAsState()
    
    LaunchedEffect(key1 = true) {
        viewModel.startProcessing()
    }
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Image Processing",
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
                .offset(x = 150.dp, y = 50.dp)
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
                StepIndicator()

                Spacer(modifier = Modifier.height(24.dp))

                ProcessingStatusCard(progress = progress)

                Spacer(modifier = Modifier.height(24.dp))

                ConversionSettingsCard()

                Spacer(modifier = Modifier.height(32.dp))

                if (isComplete) {
                    GradientButton(
                        text = "Generate G-Code",
                        onClick = onContinueClick
                    )
                }
                
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@Composable
fun StepIndicator() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        StepItem("Process", Icons.Default.CheckCircle, Color(0xFF34C759), true)
        HorizontalDivider(modifier = Modifier.weight(1f).padding(horizontal = 8.dp), color = VioletPrimary.copy(alpha = 0.3f))
        StepItem("Vectorize", Icons.Default.Image, VioletPrimary, true, isCurrent = true)
        HorizontalDivider(modifier = Modifier.weight(1f).padding(horizontal = 8.dp), color = VioletPrimary.copy(alpha = 0.3f))
        StepItem("G-Code", Icons.Default.GridOn, GrayText, false)
    }
}

@Composable
fun ProcessingStatusCard(progress: Float) {
    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(160.dp)) {
                Canvas(modifier = Modifier.size(140.dp)) {
                    drawArc(
                        color = Color.White.copy(alpha = 0.5f),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawArc(
                        brush = Brush.sweepGradient(listOf(VioletPrimary, TealAccent, VioletPrimary)),
                        startAngle = -90f,
                        sweepAngle = 360 * progress,
                        useCenter = false,
                        style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                Text(
                    text = "${(progress * 100).toInt()}%",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = DarkText
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Text(text = "Edge Detection & Contours", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkText)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Vectorizing kolam paths for CNC precision",
                fontSize = 14.sp,
                color = GrayText,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun ConversionSettingsCard() {
    var detailLevel by remember { mutableStateOf(0.8f) }
    var lineSimplification by remember { mutableStateOf(0.4f) }

    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(text = "Computer Vision Settings", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkText)
            
            Spacer(modifier = Modifier.height(20.dp))
            
            SettingRow(
                icon = Icons.Outlined.AutoFixHigh,
                label = "Algorithm",
                content = {
                    Box(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Canny Edge", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = DarkText)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = VioletPrimary)
                        }
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            SettingRow(
                icon = Icons.Outlined.Architecture,
                label = "Edge Sensitivity",
                content = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Slider(
                            value = detailLevel,
                            onValueChange = { detailLevel = it },
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(thumbColor = VioletPrimary, activeTrackColor = VioletPrimary)
                        )
                        Text(text = "${(detailLevel * 100).toInt()}%", fontSize = 12.sp, color = VioletPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.width(40.dp), textAlign = TextAlign.End)
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            SettingRow(
                icon = Icons.Outlined.Timeline,
                label = "Noise Reduction",
                content = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Slider(
                            value = lineSimplification,
                            onValueChange = { lineSimplification = it },
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(thumbColor = VioletPrimary, activeTrackColor = VioletPrimary)
                        )
                        Text(text = "${(lineSimplification * 100).toInt()}%", fontSize = 12.sp, color = VioletPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.width(40.dp), textAlign = TextAlign.End)
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            SettingRow(
                icon = Icons.Outlined.Polyline,
                label = "Path Extraction",
                content = {
                    Text(text = "Active", fontSize = 14.sp, color = Color(0xFF34C759), fontWeight = FontWeight.Bold)
                }
            )
        }
    }
}

@Composable
fun SettingRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, content: @Composable () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier.size(36.dp).background(Color.White, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = VioletPrimary, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = DarkText, modifier = Modifier.weight(1f))
        content()
    }
}
