package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onNavigateToHome: () -> Unit) {
    // Animation states
    var startAnimation by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.5f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "LogoScale"
    )
    
    val alpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1200, easing = LinearOutSlowInEasing),
        label = "TaglineAlpha"
    )

    // Pulse animation for glow effect
    val infiniteTransition = rememberInfiniteTransition(label = "GlowPulse")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowPulseVal"
    )

    LaunchedEffect(key1 = true) {
        startAnimation = true
        // Keep splash screen visible for 2.5 seconds
        delay(2500)
        onNavigateToHome()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        DarkNavyBackground,
                        DarkNavyBackground,
                        DarkNavySurface
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Logo Container with Cyan Glow
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .scale(scale)
                    .size(130.dp)
            ) {
                // Glow circles
                Box(
                    modifier = Modifier
                        .size(100.dp * pulseGlow)
                        .background(NeonCyan.copy(alpha = 0.05f * pulseGlow), shape = androidx.compose.foundation.shape.CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .background(NeonCyan.copy(alpha = 0.15f), shape = androidx.compose.foundation.shape.CircleShape)
                )
                
                // Icon
                Icon(
                    imageVector = Icons.Default.Hub,
                    contentDescription = "Barishal Connect Logo",
                    tint = NeonCyan,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Title
            Text(
                text = "Barishal Connect",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp,
                modifier = Modifier.scale(scale)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Bengali Tagline
            Text(
                text = "বরিশাল বিভাগের সকল তথ্য ও সেবা এক অ্যাপে",
                color = TextCyan,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(alpha)
                    .padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(60.dp))

            // Custom futuristic progress indicator
            Box(
                modifier = Modifier
                    .width(180.dp)
                    .alpha(alpha),
                contentAlignment = Alignment.Center
            ) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().height(2.dp),
                    color = NeonCyan,
                    trackColor = GlassBorder
                )
            }
        }

        // Footer copyright / version
        Text(
            text = "Digital Super App • v1.0.0",
            color = TextGray.copy(alpha = 0.5f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Light,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 24.dp)
        )
    }
}
