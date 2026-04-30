package com.example.swift.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swift.ui.theme.*
import com.example.swift.viewmodel.AuthViewModel
import com.example.swift.viewmodel.BookingViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentSummaryScreen(
    bookingViewModel: BookingViewModel,
    authViewModel: AuthViewModel,
    onPaymentComplete: () -> Unit,
    onBack: () -> Unit
) {
    val isProcessing = bookingViewModel.isProcessingBooking
    val errorMessage = bookingViewModel.bookingErrorMessage
    val bookingComplete = bookingViewModel.bookingComplete

    // Mock countdown timer
    var remainingTime by remember { mutableIntStateOf(19 * 60 + 55) } // 19m 55s
    LaunchedEffect(Unit) {
        while (remainingTime > 0) {
            delay(1000L)
            remainingTime--
        }
    }
    val min = remainingTime / 60
    val sec = remainingTime % 60

    LaunchedEffect(bookingComplete) {
        if (bookingComplete) {
            onPaymentComplete()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Unpaid", fontWeight = FontWeight.SemiBold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = { Spacer(modifier = Modifier.width(48.dp)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SwiftRed,
                    titleContentColor = SwiftWhite,
                    navigationIconContentColor = SwiftWhite
                )
            )
        },
        bottomBar = {
            Surface(color = SwiftWhite, shadowElevation = 16.dp) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Fare", color = SwiftGray, fontSize = 14.sp)
                        Text(bookingViewModel.formatCurrency(bookingViewModel.totalPrice), color = SwiftBlack, fontWeight = FontWeight.Bold)
                    }
                    HorizontalDivider(color = SwiftGrayLight)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Total Price", color = SwiftBlack, fontSize = 16.sp)
                        Text(bookingViewModel.formatCurrency(bookingViewModel.totalPrice), color = SwiftRed, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedButton(
                            onClick = onBack,
                            modifier = Modifier.weight(0.8f).height(48.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = SwiftGray),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SwiftGrayLight)
                        ) {
                            Text("Cancel", fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(
                            onClick = { },
                            modifier = Modifier.weight(0.8f).height(48.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = SwiftGray),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SwiftGrayLight)
                        ) {
                            Text("Return", fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { 
                                val uid = authViewModel.userId.value ?: 0
                                val accountEmail = authViewModel.userEmail.value ?: ""
                                if (uid > 0) {
                                    bookingViewModel.processFinalBooking(uid, accountEmail)
                                } else {
                                    // Manually trigger error if session is lost
                                    bookingViewModel.bookingErrorMessage = "Session error: Please log out and log in again."
                                }
                            },
                            modifier = Modifier.weight(1.4f).height(48.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SwiftRed),
                            enabled = !isProcessing
                        ) {
                            if (isProcessing) {
                                CircularProgressIndicator(color = SwiftWhite, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Text("Pay", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SwiftPinkBg)
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Countdown Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SwiftRed.copy(alpha = 0.05f))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.Schedule, contentDescription = null, tint = SwiftGray, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("To be paid, remaining time: ", color = SwiftGray, fontSize = 14.sp)
                Text("${min} m ${sec} s", color = SwiftRed, fontSize = 14.sp)
            }

            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = SwiftWhite,
                    modifier = Modifier.fillMaxWidth().background(SwiftRed).padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Train Card
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SwiftWhite)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(bookingViewModel.departureDate, color = SwiftGray, fontSize = 14.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Schedule, contentDescription = null, tint = SwiftGray, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${bookingViewModel.travelDuration} m", color = SwiftGray, fontSize = 14.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(bookingViewModel.selectedTime ?: "", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = SwiftBlack)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("G1063", color = SwiftGray, fontSize = 14.sp)
                            HorizontalDivider(modifier = Modifier.width(60.dp).padding(vertical = 4.dp), color = SwiftGrayLight)
                        }
                        Text(bookingViewModel.arrivalTime, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = SwiftBlack)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(bookingViewModel.origin.displayName, color = SwiftGray, fontSize = 14.sp)
                        Text(bookingViewModel.destination.displayName, color = SwiftGray, fontSize = 14.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Passenger Cards
            bookingViewModel.passengers.forEachIndexed { index, passenger ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SwiftWhite)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Passenger", fontSize = 16.sp, color = SwiftBlack)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = passenger.name.ifBlank { "New Passenger" },
                                    fontSize = 16.sp,
                                    color = SwiftBlack,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    color = SwiftGrayLight.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "${passenger.passengerType.displayName} ticket",
                                        fontSize = 10.sp,
                                        color = SwiftGrayMedium,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = bookingViewModel.formatCurrency(bookingViewModel.pricePerTicket),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = SwiftBlack
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        val hiddenId = if (passenger.identityNumber.length > 4) {
                            passenger.identityNumber.take(4) + "****" + passenger.identityNumber.takeLast(2)
                        } else passenger.identityNumber
                        Text("Identity No. $hiddenId", color = SwiftGray, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        val seatStr = bookingViewModel.selectedSeats.getOrNull(index) ?: "N/A"
                        Text("Coach ${bookingViewModel.selectedCoachId} | ${bookingViewModel.selectedCoach?.displayName} $seatStr", color = SwiftGray, fontSize = 14.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Reminder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SwiftWhite)
                    .border(1.dp, SwiftGrayLight, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text("Reminder", fontSize = 16.sp, color = SwiftBlack)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("1. Please complete the online payment within the specified time.", color = SwiftBlack, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("2. In case of late payment, the system will cancel the transaction.", color = SwiftBlack, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("3. You will not be able to purchase additional tickets until you complete payment or cancel this order.", color = SwiftBlack, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
