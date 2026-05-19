package com.simats.kolam.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.kolam.ui.components.CustomTextField
import com.simats.kolam.ui.components.GradientButton
import com.simats.kolam.ui.components.GlassCard
import com.simats.kolam.ui.theme.*
import com.simats.kolam.viewmodel.KolamViewModel

@Composable
fun SignupScreen(
    viewModel: KolamViewModel,
    onNavigateToLogin: () -> Unit,
    onSignupSuccess: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val authState by viewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        if (authState == "Signup Success") {
            onSignupSuccess()
            viewModel.resetAuth()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(BackgroundStart, BackgroundEnd)
                )
            )
    ) {
        // Decorative background elements for Glassmorphism effect
        Box(modifier = Modifier
            .offset(x = (-50).dp, y = (-50).dp)
            .size(200.dp)
            .background(VioletPrimary.copy(alpha = 0.3f), CircleShape)
            .blur(60.dp)
        )
        Box(modifier = Modifier
            .offset(x = 200.dp, y = 300.dp)
            .size(250.dp)
            .background(TealAccent.copy(alpha = 0.2f), CircleShape)
            .blur(80.dp)
        )
        Box(modifier = Modifier
            .offset(x = (-20).dp, y = 600.dp)
            .size(150.dp)
            .background(VioletSecondary.copy(alpha = 0.25f), CircleShape)
            .blur(50.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            
            // Logo
            Box(
                modifier = Modifier
                    .size(80.dp)
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
                    modifier = Modifier.size(40.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "RangoliBot",
                style = TextStyle(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            )
            
            Text(
                text = "Join the digital kolam revolution",
                fontSize = 14.sp,
                color = Color.DarkGray
            )
            
            Spacer(modifier = Modifier.height(30.dp))
            
            // Glass Card
            GlassCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "Create Account",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    CustomTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = "Full Name",
                        placeholder = "John Doe",
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = VioletPrimary) }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    CustomTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Email",
                        placeholder = "you@example.com",
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = VioletPrimary) }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    CustomTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Password",
                        placeholder = ".........",
                        isPassword = true,
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = VioletPrimary) }
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    GradientButton(
                        text = "Sign Up",
                        onClick = { viewModel.signup(fullName, email, password) }
                    )
                    
                    if (authState != null && authState != "Signup Success") {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = authState ?: "", color = Color.Red, fontSize = 12.sp)
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(text = "Already have an account? ", color = Color.DarkGray, fontSize = 14.sp)
                        Text(
                            text = "Sign in",
                            color = VioletPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { onNavigateToLogin() }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Text(
                text = "Start creating beautiful automated rangoli designs today",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }
}
