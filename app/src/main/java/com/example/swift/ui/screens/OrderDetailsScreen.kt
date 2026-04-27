package com.example.swift.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.QrCode
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
import com.example.swift.viewmodel.BookingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailsScreen(
    bookingViewModel: BookingViewModel,
    title: String = "Order Details",
    onBack: () -> Unit
) {
    val currentBooking = bookingViewModel.currentBooking
    var showQrDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.SemiBold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = { }) {
                        Text("Rules", color = SwiftWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SwiftRed,
                    titleContentColor = SwiftWhite,
                    navigationIconContentColor = SwiftWhite
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SwiftPinkBg)
                .padding(padding)
        ) {
            // Top Red Background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(SwiftRed)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Order Info
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text("Order Number: ${currentBooking?.bookingCode ?: "GA81662081"}", color = SwiftWhite, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Order Time: 07:36 18/04/2026", color = SwiftWhite, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Train Card
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SwiftWhite),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(currentBooking?.departureDate ?: "Sat, 18 Apr 2026", color = SwiftGray, fontSize = 14.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.Schedule, contentDescription = null, tint = SwiftGray, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("${currentBooking?.travelDuration ?: 54} m", color = SwiftGray, fontSize = 14.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(currentBooking?.departureTime ?: "21:25", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = SwiftBlack)
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("G1063", color = SwiftGray, fontSize = 14.sp)
                                HorizontalDivider(modifier = Modifier.width(60.dp).padding(vertical = 4.dp), color = SwiftGrayLight)
                            }
                            Text(currentBooking?.arrivalTime ?: "22:19", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = SwiftBlack)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(currentBooking?.origin?.displayName ?: "Halim", color = SwiftGray, fontSize = 14.sp)
                            Text(currentBooking?.destination?.displayName ?: "Tegalluar", color = SwiftGray, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            OutlinedButton(
                                onClick = { },
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = SwiftBlack),
                                border = androidx.compose.foundation.BorderStroke(1.dp, SwiftGrayLight)
                            ) {
                                Text("Reschedule")
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            OutlinedButton(
                                onClick = { },
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = SwiftBlack),
                                border = androidx.compose.foundation.BorderStroke(1.dp, SwiftGrayLight)
                            ) {
                                Text("Refund")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Passenger Card
                currentBooking?.passengers?.forEachIndexed { index, passenger ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SwiftWhite),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column {
                            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(passenger.name, fontSize = 16.sp, color = SwiftBlack)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(color = SwiftGrayLight, shape = RoundedCornerShape(4.dp)) {
                                            Text("${passenger.passengerType.displayName} ticket", fontSize = 10.sp, color = SwiftGrayMedium, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    val hiddenId = if (passenger.identityNumber.length > 4) {
                                        passenger.identityNumber.take(4) + "****" + passenger.identityNumber.takeLast(2)
                                    } else passenger.identityNumber
                                    Text("Identity No. $hiddenId", color = SwiftGray, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    val seatStr = currentBooking.selectedSeats.getOrNull(index) ?: "N/A"
                                    Text("Coach ${currentBooking.selectedCoachId} | ${currentBooking.coachClass.displayName} $seatStr", color = SwiftGray, fontSize = 14.sp)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(bookingViewModel.formatCurrency(currentBooking.pricePerTicket), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SwiftBlack)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    // QR Code Button
                                    Surface(
                                        color = Color(0xFFFDF0E1),
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier.clickable { showQrDialog = true }
                                    ) {
                                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Outlined.QrCode, contentDescription = null, tint = Color(0xFFE2913A), modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("QR Code >", fontSize = 12.sp, color = Color(0xFFE2913A), fontWeight = FontWeight.Medium)
                                        }
                                    }
                                }
                            }
                            
                            OutlinedButton(
                                onClick = { },
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, bottom = 16.dp).height(48.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = SwiftBlack),
                                border = androidx.compose.foundation.BorderStroke(1.dp, SwiftGrayLight)
                            ) {
                                Text("Add Infant")
                            }

                            HorizontalDivider(color = SwiftGrayLight)

                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Total payment amount:", color = SwiftGray, fontSize = 14.sp)
                                Text(bookingViewModel.formatCurrency(currentBooking.pricePerTicket), color = SwiftBlack, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
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
                        .border(1.dp, SwiftGrayLight, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text("Reminder", fontSize = 16.sp, color = SwiftBlack)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("1. The ticket you purchased this time has been issued, You can enter the station with the QR code of the ticket, or enter the station after exchanging the paper ticket at the station window.", color = SwiftBlack, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("2. After exchanging for a paper ticket, the ticket cannot be refunded or Rescheduled on the APP.", color = SwiftBlack, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("3. You can save a screenshot of the current order details interface so that you can view the seat position when taking the bus.", color = SwiftBlack, fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    if (showQrDialog) {
        QRCodeDialog(bookingViewModel = bookingViewModel, onDismiss = { showQrDialog = false })
    }
}
