package com.example.swift.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.swift.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

// ── 1. MOCK FINGERPRINT DIALOG ───────────────────────────────────────────────

@Composable
fun MockFingerprintDialog(
    accountName: String,
    onSuccess: () -> Unit,
    onCancel: () -> Unit
) {
    var isSuccess by remember { mutableStateOf(false) }
    
    // Scale animation when pressed
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.85f else 1f, label = "scale")

    // Ripple effect animation when idle
    val infiniteTransition = rememberInfiniteTransition(label = "ripple")
    val rippleScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rippleScale"
    )
    val rippleAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rippleAlpha"
    )

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(1000) // Hold for 1 second to authenticate
            isSuccess = true
            delay(500)
            onSuccess()
        }
    }

    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SwiftWhite)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Swift Express",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = SwiftBlack
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Sign in as $accountName",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SwiftGray
                )
                
                Spacer(modifier = Modifier.height(40.dp))

                Box(
                    modifier = Modifier.size(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (!isSuccess && !isPressed) {
                        // Background ripple
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .scale(rippleScale)
                                .clip(CircleShape)
                                .background(SwiftRed.copy(alpha = rippleAlpha))
                        )
                    }

                    // Main Fingerprint Icon
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .scale(scale)
                            .clip(CircleShape)
                            .background(if (isSuccess) Color(0xFF4CAF50).copy(alpha = 0.2f) else SwiftRed.copy(alpha = 0.1f))
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null
                            ) {},
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = "Fingerprint Sensor",
                            tint = if (isSuccess) Color(0xFF4CAF50) else SwiftRed,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    if (isSuccess) "Authenticated!" else "Touch the fingerprint sensor",
                    color = if (isSuccess) Color(0xFF4CAF50) else SwiftGray,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(32.dp))

                TextButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) {
                    Text("Cancel", color = SwiftGray, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// ── 2. MOCK FACE ID SCREEN ───────────────────────────────────────────────────

@Composable
fun MockFaceIdScreen(
    accountName: String,
    onSuccess: () -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    if (hasPermission) {
        Dialog(
            onDismissRequest = onCancel,
            properties = DialogProperties(
                dismissOnBackPress = true, 
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false
            )
        ) {
            FaceIdCameraContent(accountName, onSuccess, onCancel)
        }
    } else {
        // Request permission manually to avoid FragmentActivity 16-bit crash
        LaunchedEffect(Unit) {
            val activity = context as? android.app.Activity
            if (activity != null) {
                androidx.core.app.ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.CAMERA),
                    101 // Safe 16-bit request code
                )
            }
        }
        
        // Poll for permission grant
        LaunchedEffect(Unit) {
            while (!hasPermission) {
                delay(500)
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    hasPermission = true
                }
            }
        }

        Dialog(onDismissRequest = onCancel) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SwiftWhite),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = SwiftRed)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Waiting for Camera Permission...", color = SwiftBlack, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun FaceIdCameraContent(
    accountName: String,
    onSuccess: () -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var isSuccess by remember { mutableStateOf(false) }

    // Scanner animation
    val infiniteTransition = rememberInfiniteTransition(label = "scanner")
    val scannerOffset by infiniteTransition.animateFloat(
        initialValue = -150f,
        targetValue = 150f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scannerOffset"
    )

    LaunchedEffect(Unit) {
        delay(3000) // Scan for 3 seconds
        isSuccess = true
        delay(1000) // Show success state briefly
        onSuccess()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Camera Preview
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }
                
                setupCamera(ctx, previewView, lifecycleOwner)
                previewView
            }
        )

        // Dark Overlay with transparent circle
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
        )

        // UI Elements
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))
            
            Text(
                "Face ID",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Position your face within the frame",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.weight(1f))

            // The Scanner Circle
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .border(
                        width = 4.dp,
                        color = if (isSuccess) Color(0xFF4CAF50) else SwiftRed,
                        shape = CircleShape
                    )
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // Clear the dark overlay inside the circle (simulated by the PreviewView behind it)
                
                // Animated Scanning Line
                if (!isSuccess) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .offset(y = scannerOffset.dp)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        SwiftRed,
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                } else {
                    Icon(
                        Icons.Default.Fingerprint, // Success Icon placeholder
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(64.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = if (isSuccess) "Face Recognized" else "Scanning...",
                color = if (isSuccess) Color(0xFF4CAF50) else Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Close button
            IconButton(
                onClick = onCancel,
                modifier = Modifier
                    .size(56.dp)
                    .background(Color.White.copy(alpha = 0.2f), CircleShape)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

private fun setupCamera(
    context: Context,
    previewView: PreviewView,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner
) {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    cameraProviderFuture.addListener({
        try {
            val cameraProvider = cameraProviderFuture.get()
            
            // Try to use front camera
            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                .build()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }, ContextCompat.getMainExecutor(context))
}
