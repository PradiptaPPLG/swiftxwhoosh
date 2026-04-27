package com.example.swift.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swift.models.DepartureSchedule
import com.example.swift.ui.theme.*
import com.example.swift.viewmodel.BookingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    bookingViewModel: BookingViewModel,
    onTimeSelected: () -> Unit,
    onBack: () -> Unit
) {
    val duration = bookingViewModel.travelDuration

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pilih Jadwal", fontWeight = FontWeight.SemiBold) },
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
        ) {
            // Route Info Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SwiftWhite),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(bookingViewModel.origin.displayName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SwiftBlack)
                        Text("Keberangkatan", style = MaterialTheme.typography.bodySmall, color = SwiftGray)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Train, contentDescription = null, tint = SwiftRed, modifier = Modifier.size(24.dp))
                        Text("$duration menit", style = MaterialTheme.typography.labelSmall, color = SwiftDarkTeal, fontWeight = FontWeight.Medium)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(bookingViewModel.destination.displayName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SwiftBlack)
                        Text("Tujuan", style = MaterialTheme.typography.bodySmall, color = SwiftGray)
                    }
                }
                HorizontalDivider(color = SwiftGrayLight)
                Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = SwiftRed, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(bookingViewModel.departureDate, style = MaterialTheme.typography.bodySmall, color = SwiftGray)
                    Spacer(modifier = Modifier.width(16.dp))
                    Icon(Icons.Default.Person, contentDescription = null, tint = SwiftRed, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${bookingViewModel.ticketCount} tiket", style = MaterialTheme.typography.bodySmall, color = SwiftGray)
                }
            }

            Text(
                "Pilih Jam Keberangkatan",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = SwiftBlack,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            // Time List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(DepartureSchedule.times) { time ->
                    val arrival = DepartureSchedule.calculateArrival(time, duration)
                    val isSelected = bookingViewModel.selectedTime == time

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                bookingViewModel.selectedTime = time
                                onTimeSelected()
                            }
                            .then(
                                if (isSelected) Modifier.border(2.dp, SwiftRed, RoundedCornerShape(12.dp))
                                else Modifier
                            ),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) SwiftRed.copy(alpha = 0.05f) else SwiftWhite
                        ),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Departure Time
                            Column {
                                Text(time, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = SwiftBlack)
                                Text("Berangkat", style = MaterialTheme.typography.labelSmall, color = SwiftGray)
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            // Duration line
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("$duration mnt", style = MaterialTheme.typography.labelSmall, color = SwiftDarkTeal, fontWeight = FontWeight.Medium)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.width(40.dp).height(2.dp).background(SwiftTeal))
                                    Icon(Icons.Default.ArrowForward, contentDescription = null, tint = SwiftTeal, modifier = Modifier.size(14.dp))
                                }
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            // Arrival Time
                            Column(horizontalAlignment = Alignment.End) {
                                Text(arrival, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = SwiftDarkTeal)
                                Text("Tiba", style = MaterialTheme.typography.labelSmall, color = SwiftGray)
                            }
                        }
                    }
                }
            }
        }
    }
}
