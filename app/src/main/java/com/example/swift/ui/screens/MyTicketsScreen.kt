package com.example.swift.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swift.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTicketsScreen(
    onTicketClick: () -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(1) } // Default to "Paid"
    val tabs = listOf("Unpaid", "Paid", "History")

    val historyTickets = listOf(
        HistoryTicket("GA11443449", "Halim", "Tegalluar Summarecon", "G1059", "20:25 10/04/2026", "09:13 10/04/2026", 2, "Kiki Supendi MT"),
        HistoryTicket("GA11441658", "Tegalluar Summarecon", "Halim", "G1046", "16:35 09/04/2026", "15:45 09/04/2026", 1, "Kiki Supendi MT"),
        HistoryTicket("GA71474531", "Halim", "Tegalluar Summarecon", "G1059", "20:25 07/04/2026", "09:07 07/04/2026", 2, "Kiki Supendi MT"),
        HistoryTicket("GA01457111", "Tegalluar Summarecon", "Halim", "G1062", "16:35 04/04/2026", "15:40 04/04/2026", 1, "Kiki Supendi MT")
    )

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("My Tickets", modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontSize = 18.sp) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = SwiftWhite,
                        titleContentColor = SwiftBlack
                    )
                )
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = SwiftWhite,
                    contentColor = SwiftRed,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = SwiftRed
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
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
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
                .background(SwiftPinkBg)
                .padding(padding)
        ) {
            if (selectedTabIndex == 2) {
                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(historyTickets.size) { index ->
                        HistoryTicketCard(historyTickets[index], onClick = onTicketClick)
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Search,
                            contentDescription = "No Data",
                            modifier = Modifier.size(80.dp),
                            tint = SwiftGrayLight
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Data",
                            fontSize = 18.sp,
                            color = SwiftGrayMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryTicketCard(ticket: HistoryTicket, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = SwiftWhite),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Surface(
                    color = Color(0xFF91A3B0),
                    shape = RoundedCornerShape(topStart = 4.dp, bottomEnd = 12.dp),
                    modifier = Modifier.offset(x = (-16).dp, y = (-16).dp)
                ) {
                    Text("Single", color = SwiftWhite, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
                }
                Text(ticket.orderId, color = SwiftGray, fontSize = 12.sp)
            }
            
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(ticket.origin, color = SwiftBlack, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(ticket.trainNo, color = SwiftGray, fontSize = 10.sp)
                    androidx.compose.material3.HorizontalDivider(modifier = Modifier.width(40.dp).padding(vertical = 2.dp), color = SwiftGrayLight)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(ticket.destination, color = SwiftBlack, fontSize = 14.sp)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text("Departure: ${ticket.departureTime}", color = SwiftGray, fontSize = 12.sp)
            Text("Order Time: ${ticket.orderTime}", color = SwiftGray, fontSize = 12.sp)
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text("${ticket.ticketCount} tickets  ${ticket.passengerName}", color = SwiftGray, fontSize = 12.sp)
        }
    }
}

private data class HistoryTicket(
    val orderId: String,
    val origin: String,
    val destination: String,
    val trainNo: String,
    val departureTime: String,
    val orderTime: String,
    val ticketCount: Int,
    val passengerName: String
)
