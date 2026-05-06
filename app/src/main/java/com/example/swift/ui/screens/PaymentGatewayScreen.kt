package com.example.swift.ui.screens

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swift.ui.theme.*
import com.example.swift.viewmodel.BookingViewModel
import com.example.swift.utils.EmailSender
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentGatewayScreen(
    bookingViewModel: BookingViewModel,
    onPaymentSuccess: () -> Unit,
    onBack: () -> Unit
) {
    var isVerifying by remember { mutableStateOf(false) }
    var showSuccessAnimation by remember { mutableStateOf(false) }
    var selectedBank by remember { mutableStateOf("Mandiri") }

    val banks = listOf(
        Pair("Mandiri", com.example.swift.R.drawable.mandiri),
        Pair("BNI", com.example.swift.R.drawable.bni),
        Pair("BRI", com.example.swift.R.drawable.bri),
        Pair("BTN", com.example.swift.R.drawable.btn)
    )

    LaunchedEffect(showSuccessAnimation) {
        if (showSuccessAnimation) {
            delay(2500)
            onPaymentSuccess()
        }
    }

    if (showSuccessAnimation) {
        Box(
            modifier = Modifier.fillMaxSize().background(SwiftWhite),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    modifier = Modifier.size(120.dp),
                    shape = CircleShape,
                    color = Color(0xFF16A34A),
                    shadowElevation = 8.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(80.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text("Payment Successful!", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                Text("Your ticket has been issued.", color = SwiftGray)
            }
        }
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Payment Details", fontWeight = FontWeight.SemiBold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                    },
                    actions = { 
                        IconButton(onClick = onBack) { Icon(Icons.Default.Close, contentDescription = "Close") }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = SwiftWhite)
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SwiftPinkBg.copy(alpha = 0.4f))
                    .padding(padding)
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Timer Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7E6)),
                    border = BorderStroke(1.dp, Color(0xFFFAAD14)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFD48806), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Complete payment within", fontSize = 12.sp, color = Color(0xFF8C8C8C))
                            Text(text = bookingViewModel.paymentTimerText, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFD48806))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Bank Selection
                Text("Select Bank", fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    banks.forEach { (name, icon) ->
                        Card(
                            modifier = Modifier
                                .size(70.dp)
                                .clickable { selectedBank = name },
                            border = if (selectedBank == name) BorderStroke(2.dp, SwiftRed) else null,
                            colors = CardDefaults.cardColors(containerColor = SwiftWhite)
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Image(painter = painterResource(id = icon), contentDescription = name, modifier = Modifier.size(40.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Virtual Account Details
                val context = androidx.compose.ui.platform.LocalContext.current
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SwiftWhite),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Virtual Account Number", fontSize = 12.sp, color = SwiftGray)
                        Row(
                            modifier = Modifier.fillMaxWidth(), 
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("8806 0812 3456 7890", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                            Surface(
                                onClick = {
                                    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                    val clip = android.content.ClipData.newPlainText("VA Number", "8806081234567890")
                                    clipboard.setPrimaryClip(clip)
                                    // Simulating a toast or simple feedback
                                },
                                color = SwiftRed.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text("COPY", color = SwiftRed, fontWeight = FontWeight.Bold, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = SwiftGrayLight.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text("Total Amount", fontSize = 12.sp, color = SwiftGray)
                        Text(
                            text = bookingViewModel.formatCurrency(bookingViewModel.currentBooking?.totalPrice ?: 0),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SwiftRed
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Payment Instructions
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Payment Instructions", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    listOf(
                        "Open your $selectedBank Mobile Banking app",
                        "Select Transfer > Virtual Account",
                        "Paste the VA number and confirm the amount",
                        "Your ticket will be issued automatically after payment"
                    ).forEachIndexed { index, instruction ->
                        Row(modifier = Modifier.padding(vertical = 4.dp)) {
                            Text("${index + 1}. ", color = SwiftGray, fontSize = 13.sp)
                            Text(instruction, color = SwiftGray, fontSize = 13.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        isVerifying = true
                        bookingViewModel.confirmBookingPayment { success ->
                            isVerifying = false
                            if (success) {
                                showSuccessAnimation = true
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SwiftRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("I Have Paid", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TextButton(onClick = onBack) {
                    Text("Pay Later", color = SwiftGrayMedium)
                }
            }
        }
    }

    // Global Loading Overlay
    if (isVerifying) {
        androidx.compose.ui.window.Dialog(onDismissRequest = {}) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SwiftWhite),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = SwiftRed)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Verifying Payment...", fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
