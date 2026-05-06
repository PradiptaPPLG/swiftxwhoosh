package com.example.swift.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swift.models.*
import com.example.swift.ui.theme.*
import com.example.swift.viewmodel.AuthViewModel
import com.example.swift.viewmodel.BookingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPassengerScreen(
    bookingViewModel: BookingViewModel,
    authViewModel: AuthViewModel,
    onSave: () -> Unit,
    onBack: () -> Unit
) {
    var isAddingNew by remember { mutableStateOf(false) }
    var passenger by remember { mutableStateOf(PassengerDetail()) }
    
    val savedPassengers = bookingViewModel.savedPassengers
    val userId by authViewModel.userId.collectAsState()

    // Fetch passengers if empty
    LaunchedEffect(userId) {
        if (userId != null) {
            bookingViewModel.fetchSavedPassengers(userId!!)
        }
    }

    // Auto-fill email from account if empty
    val accountEmail by authViewModel.userEmail.collectAsState()
    LaunchedEffect(accountEmail) {
        if (passenger.email.isBlank() && accountEmail.isNotBlank()) {
            passenger = passenger.copy(email = accountEmail)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isAddingNew) "Add Passenger" else "Common Passenger", fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center) },
                navigationIcon = {
                    IconButton(onClick = { if (isAddingNew) isAddingNew = false else onBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Spacer(modifier = Modifier.width(48.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SwiftWhite)
            )
        },
        bottomBar = {
            if (!isAddingNew) {
                Surface(color = SwiftWhite, shadowElevation = 16.dp) {
                    Button(
                        onClick = { isAddingNew = true },
                        modifier = Modifier.fillMaxWidth().padding(16.dp).height(54.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SwiftRed)
                    ) {
                        Text("+ Add Passenger", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Surface(color = SwiftWhite, shadowElevation = 16.dp) {
                    val isFormValid = passenger.name.isNotBlank() && passenger.identityNumber.isNotBlank()
                    Button(
                        onClick = {
                            if (isFormValid) {
                                bookingViewModel.addPassenger(passenger)
                                isAddingNew = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth().padding(16.dp).height(54.dp),
                        enabled = isFormValid,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SwiftRed, disabledContainerColor = SwiftGrayLight)
                    ) {
                        Text("Save Passenger", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { padding ->
        if (!isAddingNew) {
            // List View
            if (savedPassengers.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text("No saved passengers found", color = SwiftGray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().background(SwiftPinkBg).padding(padding)
                ) {
                    items(savedPassengers.size) { index ->
                        val p = savedPassengers[index]
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = SwiftWhite)
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(p.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text("${p.identityType.displayName}: ${p.identityNumber}", color = SwiftGray, fontSize = 14.sp)
                                }
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = SwiftGrayLight)
                            }
                        }
                    }
                }
            }
        } else {
            // Form View
            LazyColumn(
                modifier = Modifier.fillMaxSize().background(SwiftPinkBg).padding(padding)
            ) {
                item {
                    SectionHeader("Personal Information")
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(0.dp), colors = CardDefaults.cardColors(containerColor = SwiftWhite)) {
                        Column(modifier = Modifier.padding(start = 16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("Gender", modifier = Modifier.weight(1f))
                                Row {
                                    RadioButton(selected = passenger.gender == Gender.MALE, onClick = { passenger = passenger.copy(gender = Gender.MALE) })
                                    Text("Male", modifier = Modifier.align(Alignment.CenterVertically))
                                    RadioButton(selected = passenger.gender == Gender.FEMALE, onClick = { passenger = passenger.copy(gender = Gender.FEMALE) })
                                    Text("Female", modifier = Modifier.align(Alignment.CenterVertically))
                                }
                            }
                            HorizontalDivider(color = SwiftGrayLight)
                            ClickableRow("Passenger Type", passenger.passengerType.displayName, false) {
                                passenger = passenger.copy(passengerType = if (passenger.passengerType == PassengerType.ADULT) PassengerType.CHILD else PassengerType.ADULT)
                            }
                        }
                    }
                }
                item {
                    SectionHeader("Certificate Information")
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(0.dp), colors = CardDefaults.cardColors(containerColor = SwiftWhite)) {
                        Column(modifier = Modifier.padding(start = 16.dp)) {
                            ClickableRow("Document Type", passenger.identityType.displayName, false) {
                                passenger = passenger.copy(identityType = if (passenger.identityType == IdentityType.ID_CARD) IdentityType.PASSPORT else IdentityType.ID_CARD)
                            }
                            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("ID Number", modifier = Modifier.weight(0.4f))
                                TextField(
                                    value = passenger.identityNumber,
                                    onValueChange = { passenger = passenger.copy(identityNumber = it) },
                                    modifier = Modifier.weight(0.6f),
                                    colors = TextFieldDefaults.colors(unfocusedContainerColor = SwiftWhite, focusedContainerColor = SwiftWhite, unfocusedIndicatorColor = Color.Transparent, focusedIndicatorColor = Color.Transparent),
                                    textStyle = androidx.compose.ui.text.TextStyle(textAlign = androidx.compose.ui.text.style.TextAlign.End)
                                )
                            }
                            HorizontalDivider(color = SwiftGrayLight)
                            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("Name", modifier = Modifier.weight(0.4f))
                                TextField(
                                    value = passenger.name,
                                    onValueChange = { passenger = passenger.copy(name = it) },
                                    modifier = Modifier.weight(0.6f),
                                    colors = TextFieldDefaults.colors(unfocusedContainerColor = SwiftWhite, focusedContainerColor = SwiftWhite, unfocusedIndicatorColor = Color.Transparent, focusedIndicatorColor = Color.Transparent),
                                    textStyle = androidx.compose.ui.text.TextStyle(textAlign = androidx.compose.ui.text.style.TextAlign.End)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Row(
        modifier = Modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.width(4.dp).height(16.dp).background(SwiftRed, RoundedCornerShape(2.dp)))
        Spacer(modifier = Modifier.width(8.dp))
        Text(title, color = SwiftGrayMedium, fontSize = 14.sp)
    }
}

@Composable
fun ClickableRow(label: String, value: String, isPlaceholder: Boolean, onClick: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(vertical = 16.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = SwiftBlack)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(value, color = if (isPlaceholder) SwiftGrayLight else SwiftGray, fontSize = 14.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = SwiftGrayLight, modifier = Modifier.size(16.dp))
        }
    }
    HorizontalDivider(color = SwiftGrayLight)
}
