package com.example.swift.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Modifier
import com.example.swift.models.BookingData
import com.example.swift.ui.theme.*
import com.example.swift.viewmodel.AuthViewModel
import com.example.swift.viewmodel.BookingViewModel
import com.example.swift.utils.EmailSender
import kotlinx.coroutines.launch
import androidx.compose.runtime.collectAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailsScreen(
    bookingViewModel: BookingViewModel,
    authViewModel: AuthViewModel,
    title: String = "Order Details",
    onBack: () -> Unit
) {
    val currentBooking = bookingViewModel.currentBooking
    if (currentBooking == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = SwiftRed)
        }
        return
    }

    var showQrDialog by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }
    var showRescheduleDialog by remember { mutableStateOf(false) }
    var showRefundDialog by remember { mutableStateOf(false) }
    var showInfantDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Trigger email only if this is the Success screen
    LaunchedEffect(Unit) {
        if (title == "Payment Succeeded") {
            Log.d("OrderDetailsScreen", "Success screen detected. Flag ticketEmailSent = ${bookingViewModel.ticketEmailSent}")
            if (!bookingViewModel.ticketEmailSent) {
                scope.launch { snackbarHostState.showSnackbar("Sending ticket email to ${currentBooking.passengerEmail}...") }
                bookingViewModel.sendFinalTicketEmail()
            }
        }
    }
    
    // Reschedule flow states
    var showRescheduleDatePicker by remember { mutableStateOf(false) }
    var newSelectedDate by remember { mutableStateOf("") }
    
    // Initialize editableEmail from passenger data, fallback to account email if empty
    val accountEmail by authViewModel.userEmail.collectAsState()
    var editableEmail by remember(currentBooking) { 
        mutableStateOf(currentBooking.passengerEmail.ifBlank { accountEmail }) 
    }

    // Reschedule Date Picker
    if (showRescheduleDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = System.currentTimeMillis() + (24 * 60 * 60 * 1000) // Tomorrow
        )
        DatePickerDialog(
            onDismissRequest = { showRescheduleDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val date = datePickerState.selectedDateMillis
                    if (date != null) {
                        val sdf = java.text.SimpleDateFormat("EEEE, dd MMM yyyy", java.util.Locale.getDefault())
                        newSelectedDate = sdf.format(java.util.Date(date))
                    }
                    showRescheduleDatePicker = false
                }) { Text("Confirm") }
            },
            dismissButton = {
                TextButton(onClick = { showRescheduleDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.SemiBold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = { }) {
                        Text("Rules", color = SwiftWhite)
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SwiftPinkBg)
                .padding(padding)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(SwiftRed)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text("Order Number: ${currentBooking?.bookingCode ?: "GA81662081"}", color = SwiftWhite, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Order Time: ${currentBooking?.departureDate ?: "18/04/2026"}", color = SwiftWhite, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Train Card
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SwiftWhite),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(currentBooking?.departureDate ?: "Sat, 18 Apr 2026", color = SwiftGray, fontSize = 14.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.Schedule, contentDescription = null, tint = SwiftGray, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("${currentBooking?.travelDuration ?: 54} m", color = SwiftGray, fontSize = 14.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(currentBooking?.departureTime ?: "21:25", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = SwiftBlack)
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("G1063", color = SwiftGray, fontSize = 14.sp)
                                HorizontalDivider(modifier = Modifier.width(60.dp).padding(vertical = 4.dp), color = SwiftGrayLight)
                            }
                            Text(currentBooking?.arrivalTime ?: "22:19", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = SwiftBlack)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(currentBooking?.origin?.displayName ?: "Halim", color = SwiftGray, fontSize = 14.sp)
                            Text(currentBooking?.destination?.displayName ?: "Tegalluar", color = SwiftGray, fontSize = 14.sp)
                        }
                Spacer(modifier = Modifier.height(24.dp))

                        // Status badge
                        val status = bookingViewModel.bookingStatus
                        val (statusLabel, statusColor) = when (status) {
                            BookingViewModel.BookingStatus.ACTIVE -> "ACTIVE" to Color(0xFF16A34A)
                            BookingViewModel.BookingStatus.CANCELLED -> "CANCELLED" to SwiftRed
                            BookingViewModel.BookingStatus.REFUNDED -> "REFUNDED PENDING" to Color(0xFF16A34A)
                            BookingViewModel.BookingStatus.RESCHEDULED -> "RESCHEDULED" to Color(0xFF1A6EDB)
                            BookingViewModel.BookingStatus.PENDING -> "WAITING FOR PAYMENT" to Color(0xFFE2913A)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            Surface(
                                color = statusColor.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.padding(bottom = 12.dp)
                            ) {
                                Text(
                                    "● $statusLabel",
                                    color = statusColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                        }

                        val isActive = status == BookingViewModel.BookingStatus.ACTIVE
                        val isPending = status == BookingViewModel.BookingStatus.PENDING

                        if (isPending) {
                             Button(
                                 onClick = {
                                     bookingViewModel.pendingBookingId = currentBooking.bookingId
                                     onBack() 
                                 },
                                 modifier = Modifier.fillMaxWidth().height(48.dp),
                                 shape = RoundedCornerShape(8.dp),
                                 colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE2913A))
                             ) {
                                 Text("Pay Now", fontWeight = FontWeight.Bold)
                             }
                             Spacer(modifier = Modifier.height(12.dp))
                        }

                        // Reschedule + Refund buttons
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            OutlinedButton(
                                onClick = { if (isActive) showRescheduleDialog = true },
                                enabled = isActive,
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = if (isActive) SwiftBlack else SwiftGrayMedium),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isActive) SwiftGrayLight else SwiftGrayLight.copy(alpha = 0.3f))
                            ) {
                                Text("Reschedule")
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            OutlinedButton(
                                onClick = { if (isActive) showRefundDialog = true },
                                enabled = isActive,
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = if (isActive) SwiftBlack else SwiftGrayMedium),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isActive) SwiftGrayLight else SwiftGrayLight.copy(alpha = 0.3f))
                            ) {
                                Text("Refund")
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Cancel button
                        OutlinedButton(
                            onClick = { if (isActive) showCancelDialog = true },
                            enabled = isActive,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = if (isActive) SwiftRed else SwiftGrayMedium),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isActive) SwiftRed.copy(alpha = 0.5f) else SwiftGrayLight.copy(alpha = 0.3f))
                        ) {
                            Text(
                                if (status == BookingViewModel.BookingStatus.CANCELLED) "Booking Cancelled" else "Cancel Booking",
                                color = if (isActive) SwiftRed else SwiftGrayMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Passenger Cards
                currentBooking?.passengers?.forEachIndexed { index, passenger ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SwiftWhite),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column {
                            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(passenger.name, fontSize = 16.sp, color = SwiftBlack, fontWeight = FontWeight.SemiBold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Surface(
                                        color = SwiftGrayLight.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text("${passenger.passengerType.displayName} ticket", fontSize = 10.sp, color = SwiftGrayMedium, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    val hiddenId = if (passenger.identityNumber.length > 4) {
                                        passenger.identityNumber.take(4) + "****" + passenger.identityNumber.takeLast(2)
                                    } else passenger.identityNumber
                                    Text("Identity No. $hiddenId", color = SwiftGray, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    val seatStr = currentBooking.selectedSeats.getOrNull(index) ?: "N/A"
                                    Text("Coach ${currentBooking.selectedCoachId} | ${currentBooking.coachClass.displayName} $seatStr", color = SwiftGray, fontSize = 14.sp)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(bookingViewModel.formatCurrency(currentBooking.pricePerTicket), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SwiftBlack)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Surface(
                                        color = Color(0xFFFDF0E1),
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier.clickable { showQrDialog = true }
                                    ) {
                                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Outlined.QrCode, contentDescription = null, tint = Color(0xFFE2913A), modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("QR Code >", fontSize = 12.sp, color = Color(0xFFE2913A), fontWeight = FontWeight.Medium)
                                        }
                                    }
                                }
                            }

                            // Add Infant button
                            OutlinedButton(
                                onClick = { showInfantDialog = true },
                                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 16.dp).height(48.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = SwiftBlack),
                                border = androidx.compose.foundation.BorderStroke(1.dp, SwiftGrayLight)
                            ) {
                                Text("Add Infant")
                            }

                            HorizontalDivider(color = SwiftGrayLight)

                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Total payment amount:", color = SwiftGray, fontSize = 14.sp)
                                Text(bookingViewModel.formatCurrency(currentBooking.totalPrice), color = SwiftBlack, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Reminder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, SwiftGrayLight, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text("Reminder", fontSize = 16.sp, color = SwiftBlack)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("1. You can enter the station with the QR code of the ticket, or exchange for a paper ticket at the station window.", color = SwiftBlack, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("2. After exchanging for a paper ticket, the ticket cannot be refunded or rescheduled on the APP.", color = SwiftBlack, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("3. Cancellation is allowed up to 24 hours before departure. Refunds are processed within 3–7 business days.", color = SwiftBlack, fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // ── Dialogs ───────────────────────────────────────────────────────────────

    if (showQrDialog) {
        QRCodeDialog(bookingViewModel = bookingViewModel, onDismiss = { showQrDialog = false })
    }

    // Cancel Dialog
    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            icon = { Text("⚠️", fontSize = 32.sp) },
            title = { Text("Cancel Booking?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to cancel booking ${currentBooking?.bookingCode}? A refund will be processed if eligible.") },
            confirmButton = {
                Button(
                    onClick = {
                        showCancelDialog = false
                        bookingViewModel.bookingStatus = BookingViewModel.BookingStatus.CANCELLED
                        scope.launch {
                            currentBooking?.let { b ->
                                EmailSender.sendCancellationEmail(b, bookingViewModel.formatCurrency(b.totalPrice), "Cancelled by user via app")
                            }
                            snackbarHostState.showSnackbar("❌ Booking cancelled. Confirmation email sent.")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SwiftRed)
                ) { Text("Yes, Cancel") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showCancelDialog = false }) { Text("Go Back") }
            }
        )
    }

    // Reschedule Dialog
    if (showRescheduleDialog) {
        AlertDialog(
            onDismissRequest = { showRescheduleDialog = false },
            icon = { Text("🔄", fontSize = 32.sp) },
            title = { Text("Reschedule Ticket", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Select a new date for your journey. Please note that rescheduling may be subject to availability.")
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedButton(
                        onClick = { showRescheduleDatePicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SwiftBlack),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SwiftGrayLight)
                    ) {
                        Icon(Icons.Outlined.Schedule, contentDescription = null, modifier = Modifier.size(18.dp), tint = SwiftGray)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (newSelectedDate.isEmpty()) "Select New Date" else "New Date: $newSelectedDate")
                    }
                    
                    if (newSelectedDate.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            color = Color(0xFF1A6EDB).copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Reschedule from ${currentBooking!!.departureDate} to $newSelectedDate",
                                modifier = Modifier.padding(12.dp),
                                fontSize = 12.sp,
                                color = Color(0xFF1A6EDB),
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Confirmation Email:", fontSize = 12.sp, color = SwiftBlack, fontWeight = FontWeight.SemiBold)
                        OutlinedTextField(
                            value = editableEmail,
                            onValueChange = { editableEmail = it },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            placeholder = { Text("Enter valid email", fontSize = 14.sp) },
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = SwiftGrayLight,
                                focusedBorderColor = Color(0xFF1A6EDB)
                            )
                        )
                        if (editableEmail.contains(".") && !editableEmail.contains("@")) {
                             Text("Please enter a valid email address", color = SwiftRed, fontSize = 10.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newSelectedDate.isEmpty()) {
                            showRescheduleDatePicker = true
                            return@Button
                        }
                        showRescheduleDialog = false
                        
                        // START REAL RESCHEDULE FLOW
                        bookingViewModel.isRescheduling = true
                        bookingViewModel.bookingToReschedule = currentBooking
                        bookingViewModel.selectedSeats = emptyList() // Clear for new selection
                        
                        // Navigate back to Dashboard to pick new date/time
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A6EDB))
                ) { Text("Confirm Reschedule") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showRescheduleDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Refund Dialog
    if (showRefundDialog) {
        AlertDialog(
            onDismissRequest = { showRefundDialog = false },
            icon = { Text("💸", fontSize = 32.sp) },
            title = { Text("Refund Ticket?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to refund booking ${currentBooking?.bookingCode}? This will release your seats and cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        showRefundDialog = false
                        currentBooking?.let { b ->
                            bookingViewModel.refundBooking(b.bookingId) { success ->
                                if (success) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("✅ Refund successful. Seats released.")
                                    }
                                    onBack()
                                } else {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("❌ Refund failed.")
                                    }
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SwiftRed)
                ) { Text("Confirm Refund") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showRefundDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Add Infant Dialog
    if (showInfantDialog) {
        AlertDialog(
            onDismissRequest = { showInfantDialog = false },
            icon = { Text("👶", fontSize = 32.sp) },
            title = { Text("Add Infant (0–2 years)", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Infants under 2 years old travel free of charge and share a seat with an accompanying adult.", color = SwiftGray, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Requirements:", fontWeight = FontWeight.SemiBold, color = SwiftBlack)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("• Must be accompanied by an adult passenger on this booking.", fontSize = 13.sp, color = SwiftGray)
                    Text("• Infant's birth certificate or ID may be required at the station.", fontSize = 13.sp, color = SwiftGray)
                    Text("• Maximum 1 infant per adult passenger.", fontSize = 13.sp, color = SwiftGray)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showInfantDialog = false
                        scope.launch {
                            snackbarHostState.showSnackbar("👶 Infant added to your booking. No extra charge.")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SwiftRed)
                ) { Text("Add Infant") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showInfantDialog = false }) { Text("Cancel") }
            }
        )
    }
}
