package com.example.swift.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swift.models.Coach
import com.example.swift.ui.theme.*
import com.example.swift.viewmodel.BookingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeatSelectionScreen(
    bookingViewModel: BookingViewModel,
    onSaveClicked: () -> Unit,
    onBack: () -> Unit
) {
    val coaches = bookingViewModel.coaches
    val selectedCoachId = bookingViewModel.selectedCoachId
    val currentCoach = coaches.find { it.id == selectedCoachId }
    val isLoading = bookingViewModel.isLoadingSeats

    LaunchedEffect(Unit) {
        val scheduleId = bookingViewModel.schedules.find { it.departureTime.startsWith(bookingViewModel.selectedTime ?: "") }?.scheduleId?.toInt() ?: 0
        bookingViewModel.prepareSeatingAndFetch(scheduleId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select carriage seat", fontWeight = FontWeight.SemiBold, fontSize = 18.sp) },
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
        },
        bottomBar = {
            Surface(
                color = SwiftWhite,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    Button(
                        onClick = onSaveClicked,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SwiftRed),
                        enabled = bookingViewModel.selectedSeats.size == bookingViewModel.ticketCount
                    ) {
                        Text("Submit", fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
        ) {
            if (isLoading || coaches.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SwiftRed)
                }
                return@Scaffold
            }

            // Passenger Info Header 
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SwiftWhite),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Passenger", style = MaterialTheme.typography.bodyMedium, color = SwiftGray)
                        
                        val firstPassenger = bookingViewModel.passengers.firstOrNull()?.name?.ifBlank { "Passenger" } ?: "Passenger"
                        val extra = if (bookingViewModel.ticketCount > 1) " + ${bookingViewModel.ticketCount - 1}" else ""
                        Text(
                            "$firstPassenger$extra >",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = SwiftBlack
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Select carriage", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = SwiftBlack)
                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(coaches) { coach ->
                            CoachTab(
                                coachId = coach.id,
                                isSelected = coach.id == selectedCoachId,
                                onClick = {
                                    if (coach.id != selectedCoachId) {
                                        bookingViewModel.selectedCoachId = coach.id
                                        // Optional: Clear selection when changing coach. Wait, Swift might allow cross coach? Let's clear for simplicity
                                        if (bookingViewModel.selectedSeats.isNotEmpty()) {
                                            bookingViewModel.selectedSeats = emptyList()
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // Seat Map Layout
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SwiftWhite),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Header Status
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Select seat", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = SwiftBlack)
                        Text(
                            "Selected: ${bookingViewModel.selectedSeats.size}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = SwiftBlack
                        )
                    }

                    // Seat Grid Layout (Scrollable vertically)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        currentCoach?.let { coach ->
                            val maxRow = coach.seats.map { it.id.filter { c -> c.isDigit() }.toInt() }.maxOrNull() ?: 1
                            
                            for (row in 1..maxRow) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Left Side (A, B, C)
                                    listOf("A", "B", "C").forEach { letter ->
                                        val seatId = "$row$letter"
                                        val seat = coach.seats.find { it.id == seatId }
                                        if (seat != null) {
                                            SeatIcon(
                                                seatId = seatId,
                                                isAvailable = seat.isAvailable,
                                                isSelected = bookingViewModel.selectedSeats.contains(seatId),
                                                onClick = { 
                                                    val sId = bookingViewModel.schedules.find { it.departureTime.startsWith(bookingViewModel.selectedTime ?: "") }?.scheduleId?.toInt() ?: 0
                                                    bookingViewModel.toggleSeatSelection(seatId, sId) 
                                                }
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                        } else if (bookingViewModel.selectedCoachClass == com.example.swift.models.CoachClass.FIRST && letter == "B") {
                                            // Optional: Hidden placeholder to maintain spacing if needed, but 2-1 is usually offset
                                            Spacer(modifier = Modifier.width(42.dp).padding(horizontal = 3.dp))
                                        }
                                    }

                                    // Aisle
                                    Box(modifier = Modifier.width(40.dp), contentAlignment = Alignment.Center) {
                                        Text("AISLE", color = SwiftGrayLight, fontSize = 8.sp)
                                    }

                                    // Right Side (D, F)
                                    listOf("D", "F").forEach { letter ->
                                        val seatId = "$row$letter"
                                        val seat = coach.seats.find { it.id == seatId }
                                        if (seat != null) {
                                            SeatIcon(
                                                seatId = seatId,
                                                isAvailable = seat.isAvailable,
                                                isSelected = bookingViewModel.selectedSeats.contains(seatId),
                                                onClick = { 
                                                    val sId = bookingViewModel.schedules.find { it.departureTime.startsWith(bookingViewModel.selectedTime ?: "") }?.scheduleId?.toInt() ?: 0
                                                    bookingViewModel.toggleSeatSelection(seatId, sId) 
                                                }
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CoachTab(coachId: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .background(if (isSelected) SwiftRed else SwiftWhite)
            .border(
                width = 1.dp,
                color = if (isSelected) SwiftRed else SwiftGrayMedium,
                shape = RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = coachId,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isSelected) SwiftWhite else SwiftRed
        )
    }
}

@Composable
private fun SeatIcon(seatId: String, isAvailable: Boolean, isSelected: Boolean, onClick: () -> Unit) {
    val imageRes = if (isAvailable) com.example.swift.R.drawable.kursiswift_available else com.example.swift.R.drawable.kursiswift_unavailable
    val textColor = if (isSelected) SwiftWhite else SwiftRed

    Box(
        modifier = Modifier
            .width(42.dp)
            .height(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(enabled = isAvailable) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        // Background Image
        androidx.compose.foundation.Image(
            painter = androidx.compose.ui.res.painterResource(id = imageRes),
            contentDescription = "Seat $seatId",
            modifier = Modifier.fillMaxSize(),
            contentScale = androidx.compose.ui.layout.ContentScale.Fit
        )
        
        // Selected overlay
        if (isSelected) {
            Box(modifier = Modifier.fillMaxSize().background(SwiftRed.copy(alpha = 0.6f)))
        }

        // Text only if available
        if (isAvailable) {
            Text(
                text = seatId,
                color = textColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}
