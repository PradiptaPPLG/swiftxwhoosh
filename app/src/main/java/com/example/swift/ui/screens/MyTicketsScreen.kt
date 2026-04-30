package com.example.swift.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swift.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTicketsScreen(
    authViewModel: com.example.swift.viewmodel.AuthViewModel,
    bookingViewModel: com.example.swift.viewmodel.BookingViewModel,
    onTicketClick: (com.example.swift.models.UserBooking) -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(1) } // Default to "Paid"
    val tabs = listOf("Unpaid", "Paid", "History")
    val userIdState by authViewModel.userId.collectAsState()
    val userId = userIdState ?: 0

    LaunchedEffect(userId) {
        if (userId > 0) {
            bookingViewModel.fetchUserBookings(userId)
        }
    }

    val now = java.util.Date()
    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())

    val filteredTickets = remember(bookingViewModel.userBookings.toList(), selectedTabIndex) {
        val bookings = bookingViewModel.userBookings.toList()
        val nowTime = now.time
        
        when (selectedTabIndex) {
            0 -> bookings.filter { it.status == "unpaid" }
            1 -> bookings.filter { 
                val departureDate = try { sdf.parse(it.departureTime) } catch(e: Exception) { null }
                val depTime = departureDate?.time ?: 0L
                it.status == "paid" && depTime > nowTime
            }
            2 -> bookings.filter { 
                val departureDate = try { sdf.parse(it.departureTime) } catch(e: Exception) { null }
                val depTime = departureDate?.time ?: 0L
                it.status in listOf("cancelled", "refunded", "rescheduled") || 
                (depTime > 0 && depTime <= nowTime)
            }
            else -> emptyList()
        }
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(SwiftWhite)) {
                TopAppBar(
                    title = { Text("My Tickets", modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = SwiftBlack) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = SwiftWhite)
                )
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = SwiftWhite,
                    contentColor = SwiftRed,
                    divider = {},
                    indicator = { tabPositions ->
                        Box(
                            Modifier
                                .tabIndicatorOffset(tabPositions[selectedTabIndex])
                                .height(3.dp)
                                .padding(horizontal = 24.dp)
                                .background(SwiftRed, RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = {
                                Text(
                                    text = title,
                                    color = if (selectedTabIndex == index) SwiftRed else SwiftGray,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 14.sp
                                )
                            }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SwiftPinkBg.copy(alpha = 0.4f))
                .padding(padding)
        ) {
            if (bookingViewModel.isLoadingUserBookings) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = SwiftRed)
            } else if (filteredTickets.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(id = com.example.swift.R.drawable.logo),
                        contentDescription = null,
                        modifier = Modifier.size(100.dp).alpha(0.1f),
                        tint = SwiftBlack
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No tickets in ${tabs[selectedTabIndex]}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SwiftGrayMedium)
                    Text("Your travel history will appear here", fontSize = 14.sp, color = SwiftGray)
                }
            } else {
                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredTickets.size) { index ->
                        PremiumTicketCard(filteredTickets[index], onClick = { onTicketClick(filteredTickets[index]) })
                    }
                }
            }
        }
    }
}

@Composable
private fun PremiumTicketCard(ticket: com.example.swift.models.UserBooking, onClick: () -> Unit) {
    val canModify = ticket.canBeModified() && ticket.status == "paid"
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SwiftWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // Top Section (Status & Train Code)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (ticket.status == "paid") Color(0xFFFDECEC) else Color(0xFFFFF7E6))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(if (ticket.status == "paid") SwiftRed else Color(0xFFFAAD14), RoundedCornerShape(50))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = ticket.status.uppercase(),
                        color = if (ticket.status == "paid") SwiftRed else Color(0xFFD48806),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = ticket.bookingCode,
                    color = SwiftGrayMedium,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Column(modifier = Modifier.padding(16.dp)) {
                // Route Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(ticket.departureTime.split(" ")[1].substring(0, 5), fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = SwiftBlack)
                        Text(ticket.originStation, fontSize = 14.sp, color = SwiftGray, fontWeight = FontWeight.Medium)
                    }
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 8.dp)) {
                        Text(ticket.trainNumber, color = SwiftRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(6.dp).background(SwiftRed, RoundedCornerShape(50)))
                            androidx.compose.material3.HorizontalDivider(
                                modifier = Modifier.width(50.dp),
                                color = SwiftRed.copy(alpha = 0.3f),
                                thickness = 1.dp
                            )
                            Icon(
                                painter = painterResource(id = com.example.swift.R.drawable.logo),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp).padding(2.dp),
                                tint = SwiftRed
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                        Text("Arrival", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = SwiftBlack)
                        Text(ticket.destinationStation, fontSize = 14.sp, color = SwiftGray, fontWeight = FontWeight.Medium, textAlign = androidx.compose.ui.text.style.TextAlign.End)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                
                // Passenger & Info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = com.example.swift.R.drawable.logo),
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = SwiftGrayMedium
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${ticket.ticketCount} Tickets • ${ticket.passengerNames.split(",")[0]}${if (ticket.ticketCount > 1) " & others" else ""}",
                            color = SwiftGrayMedium,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Text(
                        text = "Rp " + String.format("%,d", ticket.totalPrice).replace(",", "."),
                        color = SwiftBlack,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                if (ticket.status == "paid") {
                    Spacer(modifier = Modifier.height(16.dp))
                    androidx.compose.material3.HorizontalDivider(
                        color = SwiftGrayLight.copy(alpha = 0.5f),
                        thickness = 0.5.dp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!canModify) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Default.Search,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = SwiftRed
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Locked (<2h to dep)",
                                    color = SwiftRed,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.width(1.dp))
                        }
                        
                        Button(
                            onClick = { /* Reschedule logic */ },
                            enabled = canModify,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (canModify) SwiftRed else SwiftGrayLight,
                                contentColor = SwiftWhite
                            ),
                            modifier = Modifier.height(34.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Reschedule", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
