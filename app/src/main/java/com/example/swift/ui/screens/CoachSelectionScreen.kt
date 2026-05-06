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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swift.models.CoachClass
import com.example.swift.models.TicketPricing
import com.example.swift.ui.theme.*
import com.example.swift.viewmodel.BookingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoachSelectionScreen(
    bookingViewModel: BookingViewModel,
    onCoachSelected: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pilih Gerbong", fontWeight = FontWeight.SemiBold) },
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
            // Trip Summary Card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SwiftWhite),
                elevation = CardDefaults.cardElevation(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(bookingViewModel.origin.displayName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(bookingViewModel.selectedTime ?: "", style = MaterialTheme.typography.bodyMedium, color = SwiftDarkTeal, fontWeight = FontWeight.Medium)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = SwiftRed)
                            Text("${bookingViewModel.travelDuration} mnt", style = MaterialTheme.typography.labelSmall, color = SwiftGray)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(bookingViewModel.destination.displayName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(bookingViewModel.arrivalTime, style = MaterialTheme.typography.bodyMedium, color = SwiftDarkTeal, fontWeight = FontWeight.Medium)
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = SwiftGrayLight)
                    Row {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = SwiftRed, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(bookingViewModel.departureDate, style = MaterialTheme.typography.bodySmall, color = SwiftGray)
                        Spacer(modifier = Modifier.width(16.dp))
                        Icon(Icons.Default.ConfirmationNumber, contentDescription = null, tint = SwiftRed, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${bookingViewModel.ticketCount} tiket", style = MaterialTheme.typography.bodySmall, color = SwiftGray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                "Pilih Jenis Gerbong",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = SwiftBlack
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Coach Options
            CoachClass.entries.forEach { coach ->
                val price = TicketPricing.getPrice(coach, bookingViewModel.ticketCount)
                val total = price * bookingViewModel.ticketCount
                val isSelected = bookingViewModel.selectedCoachClass == coach

                val (gradientColor, iconColor) = when (coach) {
                    CoachClass.PREMIUM_ECONOMY -> Pair(SwiftTeal, SwiftDarkTeal)
                    CoachClass.BUSINESS -> Pair(SwiftDarkTeal, SwiftWhite)
                    CoachClass.FIRST -> Pair(SwiftGold, SwiftBlack)
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            bookingViewModel.selectedCoachClass = coach
                            onCoachSelected()
                        }
                        .then(
                            if (isSelected) Modifier.border(2.dp, SwiftRed, RoundedCornerShape(16.dp))
                            else Modifier
                        ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(containerColor = SwiftWhite)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Coach Icon
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(gradientColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (coach) {
                                    CoachClass.PREMIUM_ECONOMY -> Icons.Default.AirlineSeatReclineNormal
                                    CoachClass.BUSINESS -> Icons.Default.AirlineSeatReclineExtra
                                    CoachClass.FIRST -> Icons.Default.Star
                                },
                                contentDescription = null,
                                tint = gradientColor,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                coach.displayName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = SwiftBlack
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "Harga per tiket",
                                style = MaterialTheme.typography.labelSmall,
                                color = SwiftGray
                            )
                            Text(
                                bookingViewModel.formatCurrency(price),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = SwiftDarkTeal
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Total", style = MaterialTheme.typography.labelSmall, color = SwiftGray)
                            Text(
                                bookingViewModel.formatCurrency(total),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SwiftRed
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Price Info
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SwiftTeal.copy(alpha = 0.1f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Informasi Harga", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = SwiftDarkTeal)
                    Spacer(modifier = Modifier.height(8.dp))
                    PriceInfoRow("1-2 tiket", "Harga normal")
                    PriceInfoRow("3-5 tiket", "Diskon 5%")
                    PriceInfoRow("6-8 tiket", "Diskon 10%")
                    PriceInfoRow("9-10 tiket", "Diskon terbaik!")
                }
            }
        }
    }
}

@Composable
private fun PriceInfoRow(range: String, desc: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(range, style = MaterialTheme.typography.bodySmall, color = SwiftDarkTeal)
        Text(desc, style = MaterialTheme.typography.bodySmall, color = SwiftGray)
    }
}
