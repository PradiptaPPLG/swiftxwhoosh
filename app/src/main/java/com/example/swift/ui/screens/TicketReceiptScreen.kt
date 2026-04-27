package com.example.swift.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swift.ui.theme.*
import com.example.swift.viewmodel.BookingViewModel

@Composable
fun TicketReceiptScreen(
    bookingViewModel: BookingViewModel,
    onBackToHome: () -> Unit
) {
    val booking = bookingViewModel.currentBooking ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SwiftPinkBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Success Header
        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SwiftGreen, modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Text("Pembayaran Berhasil!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = SwiftBlack)
        Text("E-Tiket telah dikirim ke Email Anda", style = MaterialTheme.typography.bodyMedium, color = SwiftGray)

        Spacer(modifier = Modifier.height(20.dp))

        // E-Ticket Card
        Card(
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(8.dp),
            colors = CardDefaults.cardColors(containerColor = SwiftWhite),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                // Red Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SwiftRed, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                        .padding(20.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Train, contentDescription = null, tint = SwiftWhite, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Swift", color = SwiftWhite, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                            Spacer(modifier = Modifier.weight(1f))
                            Text("E-TIKET", color = SwiftWhite.copy(alpha = 0.8f), fontSize = 12.sp, fontWeight = FontWeight.Medium, letterSpacing = 2.sp)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Kode Booking", color = SwiftWhite.copy(alpha = 0.7f), fontSize = 12.sp)
                        Text(booking.bookingCode, color = SwiftWhite, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
                    }
                }

                // Dashed line separator
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .drawBehind {
                            drawLine(
                                color = Color.Gray,
                                start = Offset(0f, 0f),
                                end = Offset(size.width, 0f),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f)),
                                strokeWidth = 2f
                            )
                        }
                )

                // Ticket Details
                Column(modifier = Modifier.padding(20.dp)) {
                    // Route
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(booking.departureTime, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = SwiftBlack)
                            Text(booking.origin.displayName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = SwiftDarkTeal)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${booking.travelDuration} mnt", style = MaterialTheme.typography.labelSmall, color = SwiftGray)
                            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = SwiftRed, modifier = Modifier.size(20.dp))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(booking.arrivalTime, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = SwiftBlack)
                            Text(booking.destination.displayName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = SwiftDarkTeal)
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = SwiftGrayLight)

                    // Info Grid
                    Row(modifier = Modifier.fillMaxWidth()) {
                        InfoColumn("Tanggal", booking.departureDate, Modifier.weight(1f))
                        InfoColumn("Gerbong", "${booking.coachClass.displayName} - ${booking.selectedCoachId}", Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        val passNames = booking.passengers.joinToString(", ") { it.name.ifBlank{"Unnamed"} }
                        InfoColumn("Penumpang", passNames, Modifier.weight(1f))
                        InfoColumn("Kursi", booking.selectedSeats.joinToString(", "), Modifier.weight(1f))
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = SwiftGrayLight)

                    // Price
                    Row(modifier = Modifier.fillMaxWidth()) {
                        InfoColumn("Harga/tiket", bookingViewModel.formatCurrency(booking.pricePerTicket), Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SwiftRed.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("TOTAL BAYAR", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = SwiftBlack)
                        Text(bookingViewModel.formatCurrency(booking.totalPrice), fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = SwiftRed)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // QR Code Placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .background(SwiftGrayLight, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.QrCode2, contentDescription = null, tint = SwiftGray, modifier = Modifier.size(36.dp))
                            Text("Scan untuk boarding", style = MaterialTheme.typography.labelSmall, color = SwiftGray)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Back to Home
        OutlinedButton(
            onClick = {
                bookingViewModel.resetBooking()
                onBackToHome()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = SwiftRed)
        ) {
            Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Kembali ke Beranda", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun InfoColumn(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = SwiftGray)
        Spacer(modifier = Modifier.height(2.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = SwiftBlack)
    }
}
