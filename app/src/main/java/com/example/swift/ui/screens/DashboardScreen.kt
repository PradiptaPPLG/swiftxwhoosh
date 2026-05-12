package com.example.swift.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.basicMarquee
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swift.R
import com.example.swift.models.Station
import com.example.swift.ui.theme.*
import kotlinx.coroutines.delay
import com.example.swift.viewmodel.BookingViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DashboardScreen(
    bookingViewModel: BookingViewModel,
    onSearchClick: () -> Unit,
    onRefundPolicyClick: () -> Unit,
    onScheduleInfoClick: () -> Unit,
    onAnnouncementClick: () -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var originExpanded by remember { mutableStateOf(false) }
    var destinationExpanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SwiftPinkBg)
            .verticalScroll(rememberScrollState())
    ) {
        // Image Carousel Banner
        val images = listOf(
            R.drawable.swift_action,
            R.drawable.swift_cara_pesan,
            R.drawable.swift_discount,
            R.drawable.swift_potongan45,
            R.drawable.swift_tiket_promo
        )
        val startIndex = Int.MAX_VALUE / 2
        val initialPage = startIndex - (startIndex % images.size)
        val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { Int.MAX_VALUE })
        
        LaunchedEffect(pagerState.settledPage) {
            delay(3500)
            val nextPage = pagerState.settledPage + 1
            pagerState.animateScrollToPage(
                page = nextPage,
                animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val actualPage = page % images.size
                Image(
                    painter = painterResource(id = images[actualPage]),
                    contentDescription = "Banner ${actualPage + 1}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Semi-transparent overlay for text readability
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(Brush.verticalGradient(
                        colors = listOf(SwiftBlack.copy(alpha = 0.5f), androidx.compose.ui.graphics.Color.Transparent)
                    ))
            )

            // Pager Indicators
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 54.dp), // Leave space for the elevated card (-40.dp)
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(images.size) { iteration ->
                    val actualCurrentPage = pagerState.currentPage % images.size
                    val color = if (actualCurrentPage == iteration) SwiftWhite else SwiftWhite.copy(alpha = 0.5f)
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                            .clip(CircleShape)
                            .background(color)
                            .size(if (actualCurrentPage == iteration) 8.dp else 6.dp)








                    )
                }
            }
        }

        // Booking Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .offset(y = (-40).dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = SwiftWhite)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Swift and Feeder Train Schedule",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SwiftBlack
                )
                Spacer(modifier = Modifier.height(16.dp))
                // Origin & Destination Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Origin
                    Column(modifier = Modifier.weight(1f)) {
                        Text("From", style = MaterialTheme.typography.bodySmall, color = SwiftGray)
                        Spacer(modifier = Modifier.height(4.dp))
                        StationSelector(
                            selectedStation = bookingViewModel.origin,
                            expanded = originExpanded,
                            onExpandChange = { originExpanded = it },
                            onStationSelected = { station ->
                                bookingViewModel.origin = station
                                originExpanded = false
                                errorMessage = null
                            },
                            disabledStation = bookingViewModel.destination
                        )
                    }

                    // Swap Button
                    IconButton(
                        onClick = {
                            bookingViewModel.swapStations()
                            errorMessage = null
                        },
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .size(40.dp)
                            .background(SwiftRed.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = "Swap", tint = SwiftRed)
                    }

                    // Destination
                    Column(modifier = Modifier.weight(1f)) {
                        Text("To", style = MaterialTheme.typography.bodySmall, color = SwiftGray, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(4.dp))
                        StationSelector(
                            selectedStation = bookingViewModel.destination,
                            expanded = destinationExpanded,
                            onExpandChange = { destinationExpanded = it },
                            onStationSelected = { station ->
                                bookingViewModel.destination = station
                                destinationExpanded = false
                                errorMessage = null
                            },
                            disabledStation = bookingViewModel.origin,
                            alignEnd = true
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = SwiftGrayLight)

                // Departure Date
                Text("Departure", style = MaterialTheme.typography.bodySmall, color = SwiftGray)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showDatePicker = true }
                        .border(1.dp, SwiftGrayMedium, RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = SwiftRed, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = bookingViewModel.departureDate.ifBlank { "Select a departure date" },
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (bookingViewModel.departureDate.isNotBlank()) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (bookingViewModel.departureDate.isNotBlank()) SwiftBlack else SwiftGrayMedium
                    )
                }

                // Error
                errorMessage?.let { msg ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(msg, color = SwiftRed, style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Search Button
                Button(
                    onClick = {
                        when {
                            !bookingViewModel.isValidRoute() -> errorMessage = "Origin and destination stations cannot be the same"
                            bookingViewModel.departureDate.isBlank() -> errorMessage = "Select a departure date"
                            else -> {
                                errorMessage = null
                                onSearchClick()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SwiftRed)
                ) {
                    Text("Search", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Announcement Marquee
        val announcementText = "Jadwal Pola Operasi Swift Mulai 1 Februari 2025: Swift resmi membuka penjualan tiket keberangkatan 1 Februari 2025 dengan 62 jadwal perhari mulai tanggal 25 Januari 2025. Rute Jakarta-Bandung tersedia setiap 30 menit sekali, sementara rute Karawang setiap 1 jam sekali. • Ketentuan Boarding: Boarding dibuka paling cepat 30 menit dan ditutup 5 menit sebelum jadwal. Setiap penumpang wajib menggunakan Tiket Fisik atau QR Code masing-masing dengan memindai ke gate boarding. Penumpang yang datang terlambat tiketnya hangus dan tidak dapat dikembalikan. • Pengembalian Dana / Perubahan Jadwal: Pembatalan dikenakan potongan bea 25%. Perubahan ke tanggal yang berbeda dikenakan potongan bea 25%. Proses pengembalian dana memakan waktu selambatnya 15 hari secara transfer bank."
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .offset(y = (-30).dp)
                .clip(RoundedCornerShape(8.dp))
                .background(SwiftRed.copy(alpha = 0.08f))
                .clickable { onAnnouncementClick() }
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Campaign, 
                contentDescription = "Railway regulations", 
                tint = SwiftRed, 
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = announcementText,
                style = MaterialTheme.typography.bodyMedium,
                color = SwiftBlack,
                modifier = Modifier
                    .weight(1f)
                    .basicMarquee(iterations = Int.MAX_VALUE)
            )
        }

        // Info Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .offset(y = (-20).dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SwiftWhite)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Service Terms", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SwiftBlack)
                    Text("More", style = MaterialTheme.typography.bodySmall, color = SwiftDarkTeal)
                }
                Spacer(modifier = Modifier.height(12.dp))

                ServiceInfoItem(
                    imageRes = R.drawable.jadwalwhooshkafeeder, 
                    title = "Jadwal Swift & KA Feeder",
                    desc = "Jadwal perjalanan Kereta Cepat Swift dan Integrasinya",
                    onClick = onScheduleInfoClick
                )
                Spacer(modifier = Modifier.height(8.dp))
                ServiceInfoItem(
                    imageRes = R.drawable.refund_icon,
                    title = "Pengembalian Dana / Perubahan Jadwal",
                    desc = "Pengembalian Dana / Perubahan Jadwal",
                    onClick = onRefundPolicyClick
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = bookingViewModel.departureDateMillis ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { bookingViewModel.setDate(it) }
                    showDatePicker = false
                }) {
                    Text("OK", color = SwiftRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Batal", color = SwiftGray)
                }
            },
            colors = DatePickerDefaults.colors(containerColor = SwiftWhite)
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = SwiftRed,
                    todayContentColor = SwiftRed,
                    todayDateBorderColor = SwiftRed
                )
            )
        }
    }
}

@Composable
private fun StationSelector(
    selectedStation: Station,
    expanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    onStationSelected: (Station) -> Unit,
    disabledStation: Station,
    alignEnd: Boolean = false
) {
    Box {
        Text(
            text = selectedStation.displayName,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = SwiftBlack,
            textAlign = if (alignEnd) TextAlign.End else TextAlign.Start,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onExpandChange(true) }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandChange(false) },
            modifier = Modifier.background(SwiftWhite)
        ) {
            Station.entries.forEach { station ->
                DropdownMenuItem(
                    text = {
                        Text(
                            station.displayName,
                            color = if (station == disabledStation) SwiftGrayMedium else SwiftBlack,
                            fontWeight = if (station == selectedStation) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    onClick = { if (station != disabledStation) onStationSelected(station) },
                    enabled = station != disabledStation
                )
            }
        }
    }
}

@Composable
private fun ServiceInfoItem(
    imageRes: Int,
    title: String,
    desc: String,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SwiftPinkBg)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(width = 100.dp, height = 70.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = SwiftBlack)
            Spacer(modifier = Modifier.height(4.dp))
            Text(desc, style = MaterialTheme.typography.bodySmall, color = SwiftGray, lineHeight = 16.sp)
        }
    }
}
