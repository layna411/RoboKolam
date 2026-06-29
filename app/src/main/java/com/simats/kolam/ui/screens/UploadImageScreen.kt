package com.simats.kolam.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.simats.kolam.viewmodel.KolamViewModel
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.kolam.ui.components.GlassCard
import com.simats.kolam.ui.components.GradientButton
import com.simats.kolam.ui.theme.*
import com.simats.kolam.models.ImageRecord
import com.simats.kolam.api.RetrofitClient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadImageScreen(
    viewModel: KolamViewModel,
    onBackClick: () -> Unit,
    onContinueClick: () -> Unit
) {
    val selectedImageUri by viewModel.selectedImageUri.collectAsState()
    val userImages by viewModel.userImages.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        viewModel.setSelectedImage(uri, context)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Upload Image",
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
                .offset(x = 100.dp, y = (-100).dp)
                .size(300.dp)
                .background(VioletPrimary.copy(alpha = 0.15f), CircleShape)
                .blur(80.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                UploadArea(selectedImageUri = selectedImageUri, onPickImage = { launcher.launch("image/*") })

                Spacer(modifier = Modifier.height(32.dp))

                RecentImagesSection(
                    images = userImages,
                    onImageClick = { img ->
                        viewModel.selectRecentImage(img)
                        onContinueClick()
                    }
                )

                Spacer(modifier = Modifier.height(32.dp))

                ImageGuidelinesSection()

                Spacer(modifier = Modifier.height(32.dp))

                if (selectedImageUri != null) {
                    GradientButton(
                        text = "Continue to Processing",
                        onClick = onContinueClick
                    )
                }
                
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@Composable
fun UploadArea(selectedImageUri: android.net.Uri?, onPickImage: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 240.dp)
            .border(
                width = 2.dp,
                color = VioletPrimary.copy(alpha = 0.3f),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(2.dp)
            .clickable { onPickImage() }
    ) {
        GlassCard(
            modifier = Modifier.fillMaxSize()
        ) {
            if (selectedImageUri != null) {
                AsyncImage(
                    model = selectedImageUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(24.dp)),
                    contentScale = ContentScale.Crop,
                    error = androidx.compose.ui.graphics.vector.rememberVectorPainter(Icons.Default.BrokenImage),
                    placeholder = androidx.compose.ui.graphics.vector.rememberVectorPainter(Icons.Default.Image)
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                    modifier = Modifier
                        .size(70.dp)
                        .background(VioletPrimary.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CloudUpload,
                        contentDescription = null,
                        tint = VioletPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Drag & Drop your image here",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )
                
                Text(
                    text = "or",
                    fontSize = 14.sp,
                    color = GrayText,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                OutlinedButton(
                    onClick = onPickImage,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = VioletPrimary),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.horizontalGradient(listOf(VioletPrimary, VioletSecondary)))
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Choose from Gallery", fontWeight = FontWeight.Bold)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Supported formats: JPG, PNG",
                    fontSize = 12.sp,
                    color = GrayText
                )
            }
            }
        }
    }
}

@Composable
fun RecentImagesSection(images: List<ImageRecord>, onImageClick: (ImageRecord) -> Unit) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Recent Images", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkText)
            Text(text = "See All", fontSize = 14.sp, color = VioletPrimary, fontWeight = FontWeight.Medium)
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (images.isEmpty()) {
            Text("No recent images uploaded", color = GrayText, modifier = Modifier.padding(start = 8.dp))
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                items(images.size) { index ->
                    RecentImageItem(images[index], onClick = { onImageClick(images[index]) })
                }
            }
        }
    }
}

@Composable
fun RecentImageItem(img: ImageRecord, onClick: () -> Unit) {
    val fullUrl = if (img.url.startsWith("http")) {
        img.url
    } else {
        val baseUrl = RetrofitClient.BASE_URL.removeSuffix("/")
        val path = if (img.url.startsWith("/")) img.url else "/${img.url}"
        baseUrl + path
    }
    
    GlassCard(
        modifier = Modifier
            .size(110.dp),
        onClick = onClick
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            AsyncImage(
                model = fullUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                error = androidx.compose.ui.graphics.vector.rememberVectorPainter(Icons.Default.BrokenImage),
                placeholder = androidx.compose.ui.graphics.vector.rememberVectorPainter(Icons.Default.Image)
            )
        }
    }
}

@Composable
fun ImageGuidelinesSection() {
    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.White, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Lightbulb, contentDescription = null, tint = VioletPrimary, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = "Image Guidelines", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkText)
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                GuidelineItem(Icons.Default.Contrast, "High contrast\nblack & white")
                GuidelineItem(Icons.Default.CropOriginal, "No shadows\nor reflections")
                GuidelineItem(Icons.Default.AspectRatio, "Square\nproportions")
            }
        }
    }
}

@Composable
fun GuidelineItem(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(90.dp)) {
        Box(
            modifier = Modifier.size(40.dp).background(VioletPrimary.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = VioletPrimary, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = text,
            fontSize = 11.sp,
            color = GrayText,
            textAlign = TextAlign.Center,
            lineHeight = 14.sp
        )
    }
}
