package com.example.swift.ui.screens

import androidx.compose.ui.graphics.Color
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
import com.example.swift.models.CoachClass
import com.example.swift.ui.theme.*
import com.example.swift.viewmodel.BookingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    bookingViewModel: BookingViewModel,
    onTimeSelected: () -> Unit,
    onBack: () -> Unit
) {
    var expandedScheduleId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(Unit) {
        bookingViewModel.fetchSchedules()
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(SwiftRed)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp, start = 8.dp, end = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = SwiftWhite)
                    }
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(bookingViewModel.origin.displayName, color = SwiftWhite, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.Train, contentDescription = null, tint = SwiftWhite, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(bookingViewModel.destination.displayName, color = SwiftWhite, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                }
                
                // Date Navigation
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SwiftWhite)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { }) {
                        Icon(Icons.Default.ArrowBackIos, contentDescription = null, tint = SwiftGray, modifier = Modifier.size(14.dp))
                        Text("Previous", color = SwiftGray, fontSize = 14.sp)
                    }
                    
                    Surface(
                        color = SwiftGrayLight.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.clickable { }
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(bookingViewModel.departureDate, color = SwiftBlack, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = SwiftGray)
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { }) {
                        Text("Next", color = SwiftGray, fontSize = 14.sp)
                        Icon(Icons.Default.ArrowForwardIos, contentDescription = null, tint = SwiftGray, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F6F8))
                .padding(padding)
        ) {
            if (bookingViewModel.isLoadingSchedules) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SwiftRed)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(bookingViewModel.schedules) { schedule ->
                        ScheduleItem(
                            schedule = schedule,
                            isExpanded = expandedScheduleId == schedule.scheduleId.toInt(),
                            onExpandToggle = {
                                expandedScheduleId = if (expandedScheduleId == schedule.scheduleId.toInt()) null else schedule.scheduleId.toInt()
                            },
                            onBook = { coachClass ->
                                bookingViewModel.selectedTime = schedule.departureTime.substring(0, 5)
                                bookingViewModel.selectedArrivalTime = schedule.arrivalTime.substring(0, 5)
                                bookingViewModel.selectedCoach = coachClass
                                onTimeSelected()
                            },
                            bookingViewModel = bookingViewModel
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ScheduleItem(
    schedule: com.example.swift.api.TrainSchedule,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
    onBook: (CoachClass) -> Unit,
    bookingViewModel: BookingViewModel
) {
    val depTime = schedule.departureTime.substring(0, 5)
    val arrTime = schedule.arrivalTime.substring(0, 5)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .background(SwiftWhite)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onExpandToggle() }
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(depTime, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = SwiftBlack)
                    Text(" WIB", fontSize = 10.sp, color = SwiftGray, modifier = Modifier.padding(top = 8.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(schedule.trainCode, fontSize = 12.sp, color = SwiftGray)
                        Icon(Icons.Default.ArrowRightAlt, contentDescription = null, tint = SwiftGrayLight)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(arrTime, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = SwiftBlack)
                    Text(" WIB", fontSize = 10.sp, color = SwiftGray, modifier = Modifier.padding(top = 8.dp))
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(16.dp).background(Color(0xFF00B4D8), RoundedCornerShape(2.dp)), contentAlignment = Alignment.Center) {
                        Text("F", color = SwiftWhite, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(bookingViewModel.origin.displayName, color = SwiftGray, fontSize = 14.sp)
                    
                    Spacer(modifier = Modifier.width(24.dp))
                    
                    Box(modifier = Modifier.size(16.dp).background(SwiftRed, RoundedCornerShape(2.dp)), contentAlignment = Alignment.Center) {
                        Text("T", color = SwiftWhite, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(bookingViewModel.destination.displayName, color = SwiftGray, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = SwiftGray, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${bookingViewModel.travelDuration}m", color = SwiftGray, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(24.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Stop Station", color = SwiftRed.copy(alpha = 0.7f), fontSize = 14.sp)
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = SwiftRed.copy(alpha = 0.7f))
                    }
                }
            }
            
            Text(
                "Rp.${String.format("%,d", schedule.price.toDouble().toInt()).replace(",", ".")}",
                color = SwiftRed,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (isExpanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SwiftPinkBg.copy(alpha = 0.3f))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                ClassOptionRow("First Class", 600000, onBook = { onBook(CoachClass.FIRST) })
                ClassOptionRow("Business Class", 450000, onBook = { onBook(CoachClass.BUSINESS) })
                ClassOptionRow("Premium Economy Class", 250000, onBook = { onBook(CoachClass.PREMIUM_ECONOMY) })
            }
        }
        
        HorizontalDivider(color = SwiftGrayLight.copy(alpha = 0.5f))
    }
}

@Composable
fun ClassOptionRow(className: String, price: Int, onBook: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(className, fontSize = 16.sp, color = SwiftBlack, fontWeight = FontWeight.Medium)
            Text("Available", fontSize = 12.sp, color = SwiftGray)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Rp.${String.format("%,d", price).replace(",", ".")}",
                fontSize = 16.sp,
                color = SwiftRed,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(16.dp))
            Button(
                onClick = onBook,
                colors = ButtonDefaults.buttonColors(containerColor = SwiftRed),
                shape = RoundedCornerShape(4.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text("Book", fontSize = 12.sp, color = SwiftWhite)
            }
        }
    }
    HorizontalDivider(color = SwiftGrayLight.copy(alpha = 0.3f))
}
