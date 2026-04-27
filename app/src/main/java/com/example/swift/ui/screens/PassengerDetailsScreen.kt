package com.example.swift.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swift.models.IdentityType
import com.example.swift.models.PassengerDetail
import com.example.swift.models.PassengerType
import com.example.swift.ui.theme.*
import com.example.swift.viewmodel.BookingViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassengerDetailsScreen(
    bookingViewModel: BookingViewModel,
    onNextClicked: () -> Unit,
    onAddPassengerClicked: () -> Unit,
    onBack: () -> Unit
) {
    val passengers = bookingViewModel.passengers
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Passenger", fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        val allValid = passengers.all { it.name.isNotBlank() && it.identityNumber.isNotBlank() }
                        if (allValid && passengers.isNotEmpty()) {
                            onNextClicked()
                        } else {
                            coroutineScope.launch { snackbarHostState.showSnackbar("Please add at least 1 valid passenger") }
                        }
                    }) {
                        Text("Done", color = SwiftRed, fontWeight = FontWeight.Medium, fontSize = 16.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SwiftWhite,
                    titleContentColor = SwiftBlack,
                    navigationIconContentColor = SwiftBlack
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SwiftWhite)
                .padding(padding)
        ) {
            // Header Info
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SwiftRed.copy(alpha = 0.05f))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "${passengers.size}/15 ( Up to 15 passengers can be added)",
                    color = SwiftGrayMedium,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Add Passenger Button
            OutlinedButton(
                onClick = {
                    if (passengers.size < 15) {
                        onAddPassengerClicked()
                    } else {
                        coroutineScope.launch { snackbarHostState.showSnackbar("Maximum 15 passengers reached") }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = SwiftRed),
                border = androidx.compose.foundation.BorderStroke(1.dp, SwiftRed.copy(alpha = 0.5f))
            ) {
                Icon(androidx.compose.material.icons.Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Passenger", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Passenger List
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(passengers) { index, passenger ->
                    PassengerListItem(
                        passenger = passenger,
                        onEdit = {
                            // Currently, clicking edit might just navigate or could set an active passenger index.
                            // For simplicity, we can let users edit by removing and re-adding or we'll need to pass index.
                            // We can just show a snackbar for now or navigate to AddPassengerScreen with context.
                            coroutineScope.launch { snackbarHostState.showSnackbar("Edit feature coming soon") }
                        },
                        onRemove = {
                            bookingViewModel.removePassenger(index)
                        }
                    )
                    HorizontalDivider(color = SwiftGrayLight, modifier = Modifier.padding(start = 56.dp))
                }
            }
        }
    }
}

@Composable
fun PassengerListItem(
    passenger: PassengerDetail,
    onEdit: () -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Red Checkbox
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(SwiftRed, RoundedCornerShape(4.dp))
                .clickable { onRemove() }, // Clicking the checkbox removes them for now
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.Check,
                contentDescription = "Selected",
                tint = SwiftWhite,
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Passenger Info
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = passenger.name.ifBlank { "New Passenger" },
                    fontSize = 16.sp,
                    color = SwiftBlack
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
