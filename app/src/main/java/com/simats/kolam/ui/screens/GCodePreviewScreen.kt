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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.clickable
import android.content.Context
import android.content.ContentValues
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import java.io.OutputStream
import java.io.File
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
    
    // Parse GCode details dynamically
    val parsedSegments = remember(generatedGCode) { GCodeParser.parse(generatedGCode) }
    val totalLengthMm = remember(parsedSegments) { GCodeParser.calculateTotalLength(parsedSegments) }
    val colorsCount = remember(parsedSegments) { parsedSegments.map { it.color }.distinct().size }
    val layersCount = remember(parsedSegments) { parsedSegments.map { it.zValue }.distinct().size }
    val estTimeMin = remember(totalLengthMm, colorsCount) { 
        if (totalLengthMm == 0f) 0 else {
            // Speed F800 means 800mm/min. Add 1 minute overhead per color switch
            (totalLengthMm / 800f + colorsCount * 1.0f).toInt().coerceAtLeast(1)
        }
    }
    
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

                Preview3DCard(parsedSegments)

                Spacer(modifier = Modifier.height(20.dp))

                StatisticsRow(
                    toolpathMm = totalLengthMm,
                    estTimeMin = estTimeMin,
                    colorsCount = colorsCount,
                    layersCount = layersCount
                )

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
fun Preview3DCard(segments: List<GCodeParser.PathSegment>) {
    var animationProgress by remember { mutableStateOf(0f) }
    
    // Animate the path drawing dynamically
    LaunchedEffect(key1 = segments) {
        if (segments.isNotEmpty()) {
            val duration = 8000L
            val startTime = System.currentTimeMillis()
            while (true) {
                val elapsed = System.currentTimeMillis() - startTime
                animationProgress = (elapsed % duration).toFloat() / duration
                kotlinx.coroutines.delay(16)
            }
        }
    }

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

            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Compute raw path bounds in G-Code space
                    var minX = Float.MAX_VALUE
                    var maxX = -Float.MAX_VALUE
                    var minY = Float.MAX_VALUE
                    var maxY = -Float.MAX_VALUE
                    var hasPoints = false
                    
                    segments.forEach { segment ->
                        segment.points.forEach { pt ->
                            if (pt.x < minX) minX = pt.x
                            if (pt.x > maxX) maxX = pt.x
                            if (pt.y < minY) minY = pt.y
                            if (pt.y > maxY) maxY = pt.y
                            hasPoints = true
                        }
                    }
                    
                    val bedSize = 350f
                    
                    val pathWidth = if (hasPoints) maxX - minX else 1f
                    val pathHeight = if (hasPoints) maxY - minY else 1f
                    val pathCenterX = if (hasPoints) (minX + maxX) / 2f else bedSize / 2f
                    val pathCenterY = if (hasPoints) (minY + maxY) / 2f else bedSize / 2f
                    
                    // Scale the G-Code to cover 80% of the 350x350 bed
                    val scaleFactor = if (hasPoints) {
                        (bedSize * 0.8f) / maxOf(pathWidth, pathHeight).coerceAtLeast(1f)
                    } else {
                        1f
                    }
                    
                    // Map arbitrary G-code coordinates to physical bed coordinates [0, bedSize]
                    fun mapToBed(x: Float, y: Float): Offset {
                        val bx = bedSize / 2f + (x - pathCenterX) * scaleFactor
                        val by = bedSize / 2f + (y - pathCenterY) * scaleFactor
                        return Offset(bx, by)
                    }
                    
                    // Scale the 350x350 isometric bed to fit the Canvas area perfectly
                    val bedScaleX = (size.width * 0.75f) / (1.73205f * bedSize)
                    val bedScaleY = (size.height * 0.55f) / bedSize
                    val scale = minOf(bedScaleX, bedScaleY).coerceAtLeast(0.4f)
                    
                    // Center of the projection (shifted vertically to center the 3D projection)
                    val center = Offset(size.width / 2f, (size.height - bedSize * scale) / 2f + 25f)

                    // Helper: 3D Isometric projection mapping (X, Y, Z -> Screen Offset)
                    // Input: physical bed coordinates (x, y) in [0, bedSize]
                    fun projectIsometric(x: Float, y: Float, z: Float): Offset {
                        val px = (x - y) * 0.8660254f * scale
                        val py = (x + y) * 0.5f * scale - z * 2.0f
                        return Offset(center.x + px, center.y + py)
                    }

                    // 1. Draw 3D CNC Bed Grid in Isometric View
                    val corners = listOf(
                        projectIsometric(0f, 0f, 0f),
                        projectIsometric(bedSize, 0f, 0f),
                        projectIsometric(bedSize, bedSize, 0f),
                        projectIsometric(0f, bedSize, 0f)
                    )
                    val bedPath = Path().apply {
                        moveTo(corners[0].x, corners[0].y)
                        lineTo(corners[1].x, corners[1].y)
                        lineTo(corners[2].x, corners[2].y)
                        lineTo(corners[3].x, corners[3].y)
                        close()
                    }
                    drawPath(bedPath, Color.White.copy(alpha = 0.4f))
                    drawPath(bedPath, VioletPrimary.copy(alpha = 0.3f), style = Stroke(width = 2f))
                    
                    // Draw vertical guide poles
                    corners.forEach { pt ->
                        drawLine(Color.White.copy(alpha = 0.3f), pt, Offset(pt.x, pt.y - 80f), strokeWidth = 1f)
                    }

                    // 2. Draw parsed GCode toolpath segments dynamically
                    if (segments.isNotEmpty()) {
                        val totalPointsCount = segments.sumOf { it.points.size }
                        val activeTargetIndex = (totalPointsCount * animationProgress).toInt()
                        
                        var drawnPointsCount = 0
                        var toolheadPosition: Offset? = null
                        var toolheadHeight = 0f
                        
                        segments.forEach { segment ->
                            if (segment.points.size < 2) return@forEach
                            
                            val path = Path()
                            val firstPt = segment.points.first()
                            val bedFirst = mapToBed(firstPt.x, firstPt.y)
                            val screenFirst = projectIsometric(bedFirst.x, bedFirst.y, segment.zValue)
                            path.moveTo(screenFirst.x, screenFirst.y)
                            
                            for (i in 1 until segment.points.size) {
                                val pt = segment.points[i]
                                val bedPt = mapToBed(pt.x, pt.y)
                                val screenPt = projectIsometric(bedPt.x, bedPt.y, segment.zValue)
                                
                                drawnPointsCount++
                                if (drawnPointsCount <= activeTargetIndex) {
                                    path.lineTo(screenPt.x, screenPt.y)
                                    toolheadPosition = screenPt
                                    toolheadHeight = segment.zValue
                                } else {
                                    break
                                }
                            }
                            
                            // Draw dynamic colorful path trail (Z axis is separated beautifully!)
                            drawPath(path, segment.color, style = Stroke(width = 3.5f))
                        }

                        // 3. Draw CNC Toolhead Pointer in Isometric View
                        toolheadPosition?.let { pos ->
                            // Toolhead shadow on bed
                            drawCircle(Color.Black.copy(alpha = 0.3f), radius = 6f, center = Offset(pos.x, pos.y + toolheadHeight * 2f))
                            
                            // Color selection cartridge (Z axis visualizer)
                            val cartridgeHeight = 40f
                            val cartridgeColor = segments.find { it.zValue == toolheadHeight }?.color ?: Color.White
                            
                            drawLine(
                                color = cartridgeColor,
                                start = pos,
                                end = Offset(pos.x, pos.y - cartridgeHeight),
                                strokeWidth = 4f
                            )
                            drawCircle(cartridgeColor, radius = 8f, center = Offset(pos.x, pos.y - cartridgeHeight))
                            drawCircle(Color.White, radius = 3f, center = pos)
                        }
                    } else {
                        // Empty states message
                        drawCircle(Color.LightGray, radius = 50f, center = center, style = Stroke(2f))
                    }
                }
            }
        }
    }
}

fun downloadGCode(context: Context, gcodeText: String) {
    if (gcodeText.isEmpty() || gcodeText.startsWith("; No image") || gcodeText.startsWith("; API Error")) {
        Toast.makeText(context, "No valid G-Code to download", Toast.LENGTH_SHORT).show()
        return
    }
    
    try {
        val filename = "kolam_${System.currentTimeMillis()}.gcode"
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                put(MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_DOWNLOADS)
            }
            
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                val outputStream: OutputStream? = resolver.openOutputStream(uri)
                outputStream?.use { it.write(gcodeText.toByteArray()) }
                Toast.makeText(context, "Saved to Downloads: $filename", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Failed to save file", Toast.LENGTH_SHORT).show()
            }
        } else {
            val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, filename)
            file.writeText(gcodeText)
            Toast.makeText(context, "Saved to Downloads: $filename", Toast.LENGTH_LONG).show()
        }
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Error saving G-Code: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
    }
}

@Composable
fun StatisticsRow(
    toolpathMm: Float,
    estTimeMin: Int,
    colorsCount: Int,
    layersCount: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatItem(Icons.Outlined.Timeline, "Toolpath", "${String.format("%.1f", toolpathMm / 10f)} cm", modifier = Modifier.weight(1f))
        StatItem(Icons.Outlined.Speed, "Est. Time", "$estTimeMin min", modifier = Modifier.weight(1f))
        StatItem(Icons.Outlined.Palette, "Colors", "$colorsCount", modifier = Modifier.weight(1f))
        StatItem(Icons.Outlined.Layers, "Layers", "$layersCount", modifier = Modifier.weight(1f))
    }
}

@Composable
fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.height(82.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize().padding(4.dp)
        ) {
            Icon(icon, contentDescription = null, tint = VioletPrimary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 9.sp,
                color = GrayText,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun GCodeLinesCard(gCodeText: String) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Generated G-Code", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkText)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            if (gCodeText.isNotEmpty()) {
                                clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(gCodeText))
                                Toast.makeText(context, "G-Code copied to clipboard", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy G-Code", tint = VioletPrimary, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            downloadGCode(context, gCodeText)
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = "Download G-Code", tint = VioletPrimary, modifier = Modifier.size(20.dp))
                    }
                }
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

object GCodeParser {
    data class PathSegment(
        val points: List<Offset>,
        val color: Color,
        val zValue: Float
    )

    fun parse(gcode: String): List<PathSegment> {
        val segments = mutableListOf<PathSegment>()
        var currentPoints = mutableListOf<Offset>()
        var currentColor = Color(0xFFFF3B30) // Z1 = Red
        var currentZ = 10f // height tier 1
        
        var lastX = 0f
        var lastY = 0f
        
        gcode.lines().forEach { line ->
            val trimmed = line.trim()
            if (trimmed.isEmpty()) return@forEach
            
            if (trimmed.startsWith("Z") || trimmed.contains("Select Color")) {
                // Color change! Z1, Z2, Z3
                if (currentPoints.isNotEmpty()) {
                    segments.add(PathSegment(currentPoints.toList(), currentColor, currentZ))
                    currentPoints = mutableListOf()
                }
                
                if (trimmed.contains("Z1") || trimmed.contains("Color 1")) {
                    currentColor = Color(0xFFFF3B30)
                    currentZ = 10f
                } else if (trimmed.contains("Z2") || trimmed.contains("Color 2")) {
                    currentColor = Color(0xFFFFCC00)
                    currentZ = 25f
                } else if (trimmed.contains("Z3") || trimmed.contains("Color 3")) {
                    currentColor = Color(0xFF007AFF)
                    currentZ = 40f
                }
            } else if (trimmed.startsWith("G0") || trimmed.startsWith("G1")) {
                val isDrawing = trimmed.startsWith("G1")
                
                var x = lastX
                var y = lastY
                
                // Parse X and Y coordinates (e.g. G1 X120.4 Y230.1 F800)
                val parts = trimmed.split(" ")
                parts.forEach { part ->
                    if (part.startsWith("X")) {
                        x = part.substring(1).toFloatOrNull() ?: lastX
                    } else if (part.startsWith("Y")) {
                        y = part.substring(1).toFloatOrNull() ?: lastY
                    }
                }
                
                if (isDrawing) {
                    if (currentPoints.isEmpty()) {
                        currentPoints.add(Offset(lastX, lastY))
                    }
                    currentPoints.add(Offset(x, y))
                } else {
                    if (currentPoints.isNotEmpty()) {
                        segments.add(PathSegment(currentPoints.toList(), currentColor, currentZ))
                        currentPoints = mutableListOf()
                    }
                }
                
                lastX = x
                lastY = y
            }
        }
        
        if (currentPoints.isNotEmpty()) {
            segments.add(PathSegment(currentPoints.toList(), currentColor, currentZ))
        }
        
        return segments
    }

    fun calculateTotalLength(segments: List<PathSegment>): Float {
        var total = 0f
        segments.forEach { segment ->
            if (segment.points.size > 1) {
                for (i in 0 until segment.points.size - 1) {
                    val p1 = segment.points[i]
                    val p2 = segment.points[i+1]
                    val dx = p2.x - p1.x
                    val dy = p2.y - p1.y
                    total += kotlin.math.sqrt(dx * dx + dy * dy)
                }
            }
        }
        return total
    }
}
