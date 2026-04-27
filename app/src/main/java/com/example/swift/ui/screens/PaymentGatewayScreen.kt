package com.example.swift.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swift.ui.theme.*
import com.example.swift.viewmodel.BookingViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentGatewayScreen(
    bookingViewModel: BookingViewModel,
    onPaymentSuccess: () -> Unit,
    onBack: () -> Unit
) {
    // Mock countdown
    var remainingTime by remember { mutableIntStateOf(16 * 60 + 44) }
    LaunchedEffect(Unit) {
        while (remainingTime > 0) {
            delay(1000L)
            remainingTime--
        }
    }
    val min = remainingTime / 60
    val sec = remainingTime % 60
    val timeString = String.format("%02d:%02d", min, sec)

    val currentBooking = bookingViewModel.currentBooking

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pay", fontWeight = FontWeight.SemiBold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                navigationIcon = {
                    Row {
                        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                        IconButton(onClick = onBack) { Icon(Icons.Default.Close, contentDescription = "Close") }
                    }
                },
                actions = { Spacer(modifier = Modifier.width(96.dp)) }, // Balance the two icons on the left
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SwiftWhite,
                    titleContentColor = SwiftBlack,
                    navigationIconContentColor = SwiftBlack
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SwiftWhite)
                .padding(padding)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().background(SwiftRed).padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = SwiftWhite)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Complete Payment", color = SwiftWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            // Booking Details
            Column(modifier = Modifier.fillMaxWidth().background(SwiftGrayLight.copy(alpha = 0.3f)).padding(16.dp)) {
                Row {
                    Text("Passenger :", color = SwiftGray, modifier = Modifier.width(100.dp))
                    Text(currentBooking?.passengerName ?: "Passenger", color = SwiftBlack, fontWeight = FontWeight.Medium)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    Text("Booking No :", color = SwiftGray, modifier = Modifier.width(100.dp))
                    Text(currentBooking?.bookingCode ?: "GA81662081", color = SwiftBlack, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Price and Countdown
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = bookingViewModel.formatCurrency(currentBooking?.totalPrice ?: 0).replace("Rp ", "Rp "),
                    fontSize = 32.sp,
                    color = SwiftRed,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Payment Countdown : $timeString", color = SwiftGray, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "In accordance to KCIC policy, ticket booking\npayment cannot start if remaining booking\ntime is less than 5 minutes",
                    color = SwiftRed.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Virtual Account List
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).border(1.dp, SwiftGrayLight, RoundedCornerShape(8.dp)).clip(RoundedCornerShape(8.dp))) {
                Column {
                    Box(modifier = Modifier.fillMaxWidth().background(SwiftPinkBg).padding(16.dp)) {
                        Text("Virtual Account", color = SwiftDarkTeal, fontWeight = FontWeight.Bold)
                    }
                    
                    val banks = listOf("Bank Mandiri", "Bank BNI", "Bank BRI", "Bank BTN")
                    banks.forEachIndexed { index, bank ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPaymentSuccess() }
                                .padding(horizontal = 16.dp, vertical = 20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Placeholder for bank logo
                            Box(modifier = Modifier.size(32.dp).background(SwiftGrayLight, RoundedCornerShape(4.dp)))
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(bank, fontSize = 16.sp, color = SwiftBlack, fontWeight = FontWeight.Medium)
                        }
                        if (index < banks.lastIndex) {
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = SwiftGrayLight)
                        }
                    }
                }
            }
        }
    }
}
