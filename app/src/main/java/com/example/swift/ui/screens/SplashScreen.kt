package com.example.swift.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swift.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SplashScreen(onNavigateToLogin: () -> Unit, onNavigateToDashboard: () -> Unit, isLoggedIn: Boolean) {
    // Phase control
    var phase by remember { mutableIntStateOf(0) } // 0=initial, 1=logo, 2=tagline, 3=loading

    // Logo animations
    val logoAlpha by animateFloatAsState(
        targetValue = if (phase >= 1) 1f else 0f,
        animationSpec = tween(durationMillis = 800, easing = EaseOutCubic), label = "logoAlpha"
    )
    val logoScale by animateFloatAsState(
        targetValue = if (phase >= 1) 1f else 0.3f,
        animationSpec = tween(durationMillis = 900, easing = EaseOutBack), label = "logoScale"
    )
    val logoOffsetY by animateFloatAsState(
        targetValue = if (phase >= 2) 0f else 20f,
        animationSpec = tween(durationMillis = 600, easing = EaseOutCubic), label = "logoOffsetY"
    )

    // Tagline animation
    val taglineAlpha by animateFloatAsState(
        targetValue = if (phase >= 2) 1f else 0f,
        animationSpec = tween(durationMillis = 700, delayMillis = 100, easing = EaseOutCubic), label = "tagAlpha"
    )

    // Loading bar animation
    val loadingAlpha by animateFloatAsState(
        targetValue = if (phase >= 3) 1f else 0f,
        animationSpec = tween(durationMillis = 500), label = "loadAlpha"
    )
    var loadingProgress by remember { mutableFloatStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = loadingProgress,
        animationSpec = tween(durationMillis = 300, easing = LinearEasing), label = "progress"
    )

    // Spinning ring animation
    val infiniteTransition = rememberInfiniteTransition(label = "spin")
    val spinAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "spinAngle"
    )

    // Particle pulse
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ), label = "pulse"
    )

    // Version text
    val versionAlpha by animateFloatAsState(
        targetValue = if (phase >= 2) 1f else 0f,
        animationSpec = tween(durationMillis = 800, delayMillis = 300), label = "verAlpha"
    )

    LaunchedEffect(Unit) {
        delay(200)
        phase = 1 // Show logo
        delay(700)
        phase = 2 // Show tagline
        delay(500)
        phase = 3 // Show loading bar

        // Simulate loading progress over ~2.5 seconds
        val steps = 25
        val stepDelay = 100L
        for (i in 1..steps) {
            loadingProgress = i.toFloat() / steps.toFloat()
            delay(stepDelay)
        }

        delay(200) // Brief pause at 100%
        if (isLoggedIn) onNavigateToDashboard() else onNavigateToLogin()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE01E1E),
                        SwiftRed,
                        Color(0xFFA01212)
                    )
                )
            )
    ) {
        // Decorative background particles/circles
        Canvas(
            modifier = Modifier.fillMaxSize().alpha(0.08f)
        ) {
            val w = size.width
            val h = size.height
            // Floating circles
            val circles = listOf(
                Triple(w * 0.15f, h * 0.2f, 120f),
                Triple(w * 0.85f, h * 0.15f, 80f),
                Triple(w * 0.7f, h * 0.75f, 150f),
                Triple(w * 0.25f, h * 0.8f, 100f),
                Triple(w * 0.5f, h * 0.45f, 200f),
                Triple(w * 0.9f, h * 0.5f, 60f),
            )
            circles.forEach { (cx, cy, radius) ->
                drawCircle(
                    color = Color.White,
                    radius = radius * pulseScale,
                    center = Offset(cx, cy)
                )
            }
        }

        // Spinning arc decoration behind logo
        if (phase >= 1) {
            Canvas(
                modifier = Modifier
                    .size(180.dp)
                    .align(Alignment.Center)
                    .offset(y = (-40).dp)
                    .alpha(logoAlpha * 0.15f)
            ) {
                drawArc(
                    color = Color.White,
                    startAngle = spinAngle,
                    sweepAngle = 90f,
                    useCenter = false,
                    style = Stroke(width = 4f, cap = StrokeCap.Round)
                )
                drawArc(
                    color = Color.White,
                    startAngle = spinAngle + 180f,
                    sweepAngle = 90f,
                    useCenter = false,
                    style = Stroke(width = 4f, cap = StrokeCap.Round)
                )
            }
        }

        // Main content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-20).dp)
        ) {
            // Lightning bolt icon (text-based, no logo image)
            Box(
                modifier = Modifier
                    .alpha(logoAlpha)
                    .scale(logoScale),
                contentAlignment = Alignment.Center
            ) {
                // Glow behind the icon
                Canvas(
                    modifier = Modifier.size(90.dp)
                ) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.25f),
                                Color.Transparent
                            )
                        ),
                        radius = size.minDimension / 2
                    )
                }
                Text(
                    text = "⚡",
                    fontSize = 56.sp,
                    modifier = Modifier.offset(y = (-2).dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Brand name "SWIFT"
            Text(
                text = "SWIFT",
                color = SwiftWhite,
                fontSize = 48.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 12.sp,
                modifier = Modifier
                    .alpha(logoAlpha)
                    .scale(logoScale)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Tagline
            Text(
                text = "High-Speed Railway",
                color = SwiftWhite.copy(alpha = 0.75f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 4.sp,
                modifier = Modifier.alpha(taglineAlpha)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Loading bar
            Box(
                modifier = Modifier
                    .alpha(loadingAlpha)
                    .width(220.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(SwiftWhite.copy(alpha = 0.15f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animatedProgress)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    SwiftWhite.copy(alpha = 0.9f),
                                    SwiftWhite
                                )
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Loading percentage text
            Text(
                text = "${(animatedProgress * 100).toInt()}%",
                color = SwiftWhite.copy(alpha = 0.6f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.sp,
                modifier = Modifier.alpha(loadingAlpha)
            )
        }

        // Animated dots at bottom
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
                .alpha(loadingAlpha),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(3) { index ->
                val dotAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.3f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(600, delayMillis = index * 200, easing = EaseInOutCubic),
                        repeatMode = RepeatMode.Reverse
                    ), label = "dot$index"
                )
                Canvas(modifier = Modifier.size(6.dp).alpha(dotAlpha)) {
                    drawCircle(color = Color.White)
                }
            }
        }

        // Version text
        Text(
            text = "v1.0.0",
            color = SwiftWhite.copy(alpha = 0.4f),
            fontSize = 11.sp,
            letterSpacing = 1.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp)
                .alpha(versionAlpha)
        )
    }
}
