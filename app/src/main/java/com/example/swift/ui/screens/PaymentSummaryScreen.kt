package com.example.swift.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swift.ui.theme.*
import com.example.swift.viewmodel.AuthViewModel
import com.example.swift.viewmodel.BookingViewModel

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

    LaunchedEffect(bookingComplete) {
        if (bookingComplete) {
            onPaymentComplete()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ringkasan Pembayaran", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SwiftPinkBg)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            if (errorMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SwiftRedLight),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    Text(
                        text = errorMessage,
                        color = SwiftRed,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            // Route Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SwiftWhite),
                elevation = CardDefaults.cardElevation(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Detail Perjalanan", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = SwiftBlack)
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(bookingViewModel.selectedTime ?: "", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = SwiftBlack)
                            Text(bookingViewModel.origin.displayName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = SwiftDarkTeal)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${bookingViewModel.travelDuration} mnt", style = MaterialTheme.typography.labelSmall, color = SwiftGray)
                            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = SwiftRed)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(bookingViewModel.arrivalTime, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = SwiftBlack)
                            Text(bookingViewModel.destination.displayName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = SwiftDarkTeal)
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = SwiftGrayLight)

                    DetailRow(Icons.Default.CalendarToday, "Tanggal", bookingViewModel.departureDate)
                    Spacer(modifier = Modifier.height(8.dp))
                    DetailRow(Icons.Default.ConfirmationNumber, "Jumlah Tiket", "${bookingViewModel.ticketCount} tiket")
                    Spacer(modifier = Modifier.height(8.dp))
                    DetailRow(Icons.Default.AirlineSeatReclineExtra, "Gerbong", "${bookingViewModel.selectedCoach?.displayName} - Kereta ${bookingViewModel.selectedCoachId}")
                    Spacer(modifier = Modifier.height(8.dp))
                    DetailRow(Icons.Default.EventSeat, "Kursi", bookingViewModel.selectedSeats.joinToString(", "))
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val passNames = bookingViewModel.passengers.joinToString(", ") { it.name.ifBlank{"Unnamed"} }
                    DetailRow(Icons.Default.Person, "Penumpang", passNames)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Price Breakdown
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SwiftWhite),
                elevation = CardDefaults.cardElevation(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Rincian Harga", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = SwiftBlack)
                    Spacer(modifier = Modifier.height(12.dp))

                    PriceRow("Harga per tiket", bookingViewModel.formatCurrency(bookingViewModel.pricePerTicket))
                    PriceRow("Jumlah tiket", "× ${bookingViewModel.ticketCount}")

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = SwiftGrayLight)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Bayar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SwiftBlack)
                        Text(
                            bookingViewModel.formatCurrency(bookingViewModel.totalPrice),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SwiftRed
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Pay Button
            Button(
                onClick = {
                    bookingViewModel.processFinalBooking()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SwiftRed),
                enabled = !isProcessing
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(color = SwiftWhite, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Bayar Sekarang", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Dengan menekan tombol bayar, Anda menyetujui syarat dan ketentuan yang berlaku",
                style = MaterialTheme.typography.bodySmall,
                color = SwiftGray,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = SwiftRed, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, color = SwiftGray, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = SwiftBlack)
    }
}

@Composable
private fun PriceRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = SwiftGray)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = SwiftBlack)
    }
}
