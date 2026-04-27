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
        bookingViewModel.prepareSeatingAndFetch()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pilih kereta & tempat duduk", fontWeight = FontWeight.SemiBold, fontSize = 18.sp) },
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
                        Text("Simpan", fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
                        Text("Penumpang", style = MaterialTheme.typography.bodyMedium, color = SwiftGray)
                        val seatStr = if (bookingViewModel.selectedSeats.isNotEmpty()) {
                            bookingViewModel.selectedSeats.joinToString(", ") { "$selectedCoachId-$it" }
                        } else "Belum pilih"
                        Text(
                            "${bookingViewModel.passengers.size} Penumpang / $seatStr",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = SwiftBlack
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Pilih Kereta", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = SwiftBlack)
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
                                        // Optional: Clear selection when changing coach. Wait, Whoosh might allow cross coach? Let's clear for simplicity
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
                        Text("Pilih tempat duduk", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = SwiftBlack)
                        Text(
                            "Dipilih: ${bookingViewModel.selectedSeats.size}/${bookingViewModel.ticketCount}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (bookingViewModel.selectedSeats.size == bookingViewModel.ticketCount) SwiftTeal else SwiftRed
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
                            for (row in 1..13) {
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
                                        SeatIcon(
                                            seatId = seatId,
                                            isAvailable = seat?.isAvailable == true,
                                            isSelected = bookingViewModel.selectedSeats.contains(seatId),
                                            onClick = { bookingViewModel.toggleSeatSelection(seatId) }
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }

                                    // Aisle
                                    Spacer(modifier = Modifier.width(28.dp))

                                    // Right Side (D, F)
                                    listOf("D", "F").forEach { letter ->
                                        val seatId = "$row$letter"
                                        val seat = coach.seats.find { it.id == seatId }
                                        SeatIcon(
                                            seatId = seatId,
                                            isAvailable = seat?.isAvailable == true,
                                            isSelected = bookingViewModel.selectedSeats.contains(seatId),
                                            onClick = { bookingViewModel.toggleSeatSelection(seatId) }
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
    val bgColor = when {
        isSelected -> SwiftRed
        !isAvailable -> SwiftGrayLight
        else -> SwiftPinkBg
    }
    
    val borderColor = when {
        isSelected -> SwiftRed
        !isAvailable -> SwiftGrayLight
        else -> SwiftRedLight.copy(alpha = 0.5f)
    }
    
    val textColor = when {
        isSelected -> SwiftWhite
        !isAvailable -> SwiftGrayMedium
        else -> SwiftRed
    }

    Box(
        modifier = Modifier
            .width(42.dp)
            .height(48.dp)
            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 2.dp, bottomEnd = 2.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 2.dp, bottomEnd = 2.dp))
            .clickable(enabled = isAvailable) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = seatId,
            color = textColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        // Simulate armrest visually
        Box(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(4.dp).background(borderColor))
    }
}
