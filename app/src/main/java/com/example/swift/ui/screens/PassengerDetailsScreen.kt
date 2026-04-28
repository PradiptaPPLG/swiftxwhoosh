package com.example.swift.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swift.models.IdentityType
import com.example.swift.models.PassengerDetail
import com.example.swift.models.PassengerType
import com.example.swift.models.CoachClass
import com.example.swift.ui.theme.*
import com.example.swift.viewmodel.AuthViewModel
import com.example.swift.viewmodel.BookingViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassengerDetailsScreen(
    bookingViewModel: BookingViewModel,
    authViewModel: AuthViewModel,
    onNextClicked: () -> Unit,
    onAddPassengerClicked: () -> Unit,
    onBack: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val savedPassengers = bookingViewModel.savedPassengers
    
    // Auto-fetch saved passengers from PostgreSQL
    LaunchedEffect(Unit) {
        bookingViewModel.fetchSavedPassengers(1) // TODO: Use real logged in userId
    }
    val currentSelected = bookingViewModel.passengers
    val userId = authViewModel.userId.collectAsState().value ?: 0

    LaunchedEffect(Unit) {
        bookingViewModel.fetchSavedPassengers(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Book", fontWeight = FontWeight.SemiBold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
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
            Column(modifier = Modifier.background(SwiftWhite).padding(16.dp)) {
                Button(
                    onClick = { /* Navigate to seat selection */ },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333))
                ) {
                    Text("Select seat", color = SwiftWhite)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        if (currentSelected.isNotEmpty()) {
                            onNextClicked()
                        } else {
                            coroutineScope.launch { snackbarHostState.showSnackbar("Please select at least 1 passenger") }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SwiftRed)
                ) {
                    Text("Next step", color = SwiftWhite)
                }
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F6F8))
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Schedule Summary Card (Photo 4 Top)
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SwiftWhite)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(bookingViewModel.departureDate, color = SwiftGray, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(bookingViewModel.selectedTime ?: "", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = SwiftBlack)
                            Text(bookingViewModel.origin.displayName, fontSize = 14.sp, color = SwiftGray)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("G1057", color = SwiftGray, fontSize = 12.sp)
                            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = SwiftGrayLight, modifier = Modifier.size(24.dp))
                            Text("${bookingViewModel.travelDuration} m", color = SwiftGray, fontSize = 12.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(bookingViewModel.selectedArrivalTime ?: "", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = SwiftBlack)
                            Text(bookingViewModel.destination.displayName, fontSize = 14.sp, color = SwiftGray)
                        }
                    }
                }
            }

            // Class Cards (Photo 4 Middle)
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ClassMiniCard("First Class", 600000, bookingViewModel.selectedCoach == CoachClass.FIRST, Modifier.weight(1f)) {
                    bookingViewModel.selectedCoach = CoachClass.FIRST
                }
                ClassMiniCard("Business Class", 450000, bookingViewModel.selectedCoach == CoachClass.BUSINESS, Modifier.weight(1f)) {
                    bookingViewModel.selectedCoach = CoachClass.BUSINESS
                }
                ClassMiniCard("Premium Economy Class", 250000, bookingViewModel.selectedCoach == CoachClass.PREMIUM_ECONOMY, Modifier.weight(1f)) {
                    bookingViewModel.selectedCoach = CoachClass.PREMIUM_ECONOMY
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Passenger Selection Section (Photo 4 Bottom)
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(8.dp),
                color = SwiftWhite,
                border = androidx.compose.foundation.BorderStroke(1.dp, SwiftGrayLight.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Passenger", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = SwiftBlack)
                        if (currentSelected.isNotEmpty()) {
                             Text(currentSelected.firstOrNull()?.name ?: "", color = SwiftGray, fontSize = 14.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onAddPassengerClicked,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = SwiftWhite),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SwiftRed),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("Add New Passenger", color = SwiftRed)
                    }

                    if (savedPassengers.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Saved Passengers", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = SwiftBlack)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = SwiftGrayLight.copy(alpha = 0.5f))
                        savedPassengers.forEach { passenger ->
                            PassengerListItem(
                                passenger = passenger,
                                isSelected = currentSelected.any { it.identityNumber == passenger.identityNumber },
                                onToggle = { bookingViewModel.togglePassengerSelection(passenger, userId) },
                                onEdit = { /* Not implemented yet */ }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Conditions Text
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text("Ticket Detection & Cancel Condition", fontWeight = FontWeight.Bold, color = SwiftBlack)
                Spacer(modifier = Modifier.height(8.dp))
                Text("1. BA baby under three", fontSize = 12.sp, color = SwiftGray)
                Text("2. Adults 17 years of age or older", fontSize = 12.sp, color = SwiftGray)
                Text("3. The name and identity number must be in accordance with that contained in the identity certificate (KTP/ Passport), when the passenger age below 17 years can be filled in with the date of birth of", fontSize = 12.sp, color = SwiftGray)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ClassMiniCard(name: String, price: Int, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) SwiftWhite else Color(0xFFF0F0F0),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, SwiftRed) else null,
        shadowElevation = if (isSelected) 4.dp else 0.dp
    ) {
        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.Start) {
            Text("Rp.${String.format("%,d", price).replace(",", ".")}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (isSelected) SwiftRed else SwiftGray)
            Text(name, fontSize = 10.sp, color = SwiftGray)
            Text("Available", fontSize = 10.sp, color = SwiftGray)
            if (isSelected) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.BottomEnd) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SwiftRed, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun PassengerListItem(
    passenger: PassengerDetail,
    isSelected: Boolean,
    onToggle: () -> Unit,
    onEdit: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Selection Checkbox
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(
                    if (isSelected) SwiftRed else Color.Transparent, 
                    RoundedCornerShape(4.dp)
                )
                .border(1.dp, if (isSelected) SwiftRed else SwiftGrayLight, RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = SwiftWhite,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Passenger Info
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = passenger.name.ifBlank { "New Passenger" },
                    fontSize = 16.sp,
                    color = SwiftBlack,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    color = SwiftGrayLight,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = passenger.passengerType.displayName,
                        fontSize = 10.sp,
                        color = SwiftGrayMedium,
                        maxLines = 1,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${passenger.identityType.displayName}  ${passenger.identityNumber}",
                fontSize = 14.sp,
                color = SwiftGrayMedium
            )
        }

        // Edit Icon
        IconButton(onClick = onEdit) {
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.Edit,
                contentDescription = "Edit",
                tint = SwiftGrayMedium,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
