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
import com.example.swift.models.IdentityType
import com.example.swift.models.PassengerDetail
import com.example.swift.models.PassengerType
import com.example.swift.models.Gender
import com.example.swift.ui.theme.*
import com.example.swift.viewmodel.BookingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPassengerScreen(
    bookingViewModel: BookingViewModel,
    onSave: () -> Unit,
    onBack: () -> Unit
) {
    var passenger by remember { mutableStateOf(PassengerDetail()) }

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
                    Spacer(modifier = Modifier.width(48.dp)) // To balance the back button and center title
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SwiftWhite,
                    titleContentColor = SwiftBlack,
                    navigationIconContentColor = SwiftBlack
                )
            )
        },
        bottomBar = {
            Surface(color = SwiftWhite, shadowElevation = 16.dp) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Button(
                        onClick = {
                            bookingViewModel.addPassenger(passenger)
                            onSave()
                        },
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SwiftRed)
                    ) {
                        Text("Submit", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SwiftWhite)
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(SwiftPinkBg)
                .padding(padding)
        ) {
            item {
                SectionHeader("Personal Information")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(0.dp),
                    colors = CardDefaults.cardColors(containerColor = SwiftWhite)
                ) {
                    Column(modifier = Modifier.padding(start = 16.dp)) {
                        // Gender
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Gender", color = SwiftBlack, modifier = Modifier.weight(1f))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = passenger.gender == Gender.MALE,
                                    onClick = { passenger = passenger.copy(gender = Gender.MALE) },
                                    colors = RadioButtonDefaults.colors(selectedColor = SwiftRed)
                                )
                                Text("Male", color = SwiftGray, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(16.dp))
                                RadioButton(
                                    selected = passenger.gender == Gender.FEMALE,
                                    onClick = { passenger = passenger.copy(gender = Gender.FEMALE) },
                                    colors = RadioButtonDefaults.colors(selectedColor = SwiftRed)
                                )
                                Text("Female", color = SwiftGray, fontSize = 14.sp)
                            }
                        }
                        HorizontalDivider(color = SwiftGrayLight)

                        // Date of birth
                        var showDatePicker by remember { mutableStateOf(false) }
                        val datePickerState = rememberDatePickerState()

                        if (showDatePicker) {
                            DatePickerDialog(
                                onDismissRequest = { showDatePicker = false },
                                confirmButton = {
                                    TextButton(onClick = {
                                        datePickerState.selectedDateMillis?.let { millis ->
                                            val date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date(millis))
                                            passenger = passenger.copy(dateOfBirth = date)
                                        }
                                        showDatePicker = false
                                    }) { Text("OK", color = SwiftRed) }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                                }
                            ) {
                                DatePicker(state = datePickerState)
                            }
                        }

                        ClickableRow(
                            label = "Date of birth", 
                            value = passenger.dateOfBirth.ifBlank { "Please select a date of birth" }, 
                            isPlaceholder = passenger.dateOfBirth.isBlank(),
                            onClick = { showDatePicker = true }
                        )

                        // Passenger Type
                        ClickableRow("Passenger Type", passenger.passengerType.displayName, false) {
                            passenger = passenger.copy(
                                passengerType = if (passenger.passengerType == PassengerType.ADULT) PassengerType.CHILD else PassengerType.ADULT
                            )
                        }

                        // Discount type
                        ClickableRow("Discount type", passenger.discountType, false)

                        // Country/Region
                        ClickableRow("Country/Region", passenger.countryRegion, false)
                    }
                }
            }

            item {
                SectionHeader("Certificate Information")
                Text(
                    text = "The input information must be consistent with the certificate information.",
                    color = SwiftRed,
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth().background(SwiftRed.copy(alpha = 0.05f)).padding(horizontal = 16.dp, vertical = 8.dp)
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(0.dp),
                    colors = CardDefaults.cardColors(containerColor = SwiftWhite)
                ) {
                    Column(modifier = Modifier.padding(start = 16.dp)) {
                        ClickableRow("Document Type", passenger.identityType.displayName, false) {
                            passenger = passenger.copy(
                                identityType = if (passenger.identityType == IdentityType.ID_CARD) IdentityType.PASSPORT else IdentityType.ID_CARD
                            )
                        }
                        
                        // ID Card Input
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp, horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(passenger.identityType.displayName, color = SwiftBlack, modifier = Modifier.weight(0.4f))
                            TextField(
                                value = passenger.identityNumber,
                                onValueChange = { passenger = passenger.copy(identityNumber = it) },
                                placeholder = { Text("Please enter your ${passenger.identityType.displayName.lowercase()}", color = SwiftGrayLight, fontSize = 14.sp) },
                                modifier = Modifier.weight(0.6f),
                                colors = TextFieldDefaults.colors(
                                    unfocusedContainerColor = SwiftWhite,
                                    focusedContainerColor = SwiftWhite,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent
                                ),
                                textStyle = androidx.compose.ui.text.TextStyle(textAlign = androidx.compose.ui.text.style.TextAlign.End, color = SwiftGray),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = if (passenger.identityType == IdentityType.ID_CARD) KeyboardType.Number else KeyboardType.Ascii)
                            )
                        }
                        HorizontalDivider(color = SwiftGrayLight)

                        // Name Input
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp, horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Name", color = SwiftBlack, modifier = Modifier.weight(0.4f))
                            TextField(
                                value = passenger.name,
                                onValueChange = { passenger = passenger.copy(name = it) },
                                placeholder = { Text("Enter your name on your ID", color = SwiftGrayLight, fontSize = 14.sp) },
                                modifier = Modifier.weight(0.6f),
                                colors = TextFieldDefaults.colors(
                                    unfocusedContainerColor = SwiftWhite,
                                    focusedContainerColor = SwiftWhite,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent
                                ),
                                textStyle = androidx.compose.ui.text.TextStyle(textAlign = androidx.compose.ui.text.style.TextAlign.End, color = SwiftGray),
                                singleLine = true
                            )
                        }
                        HorizontalDivider(color = SwiftGrayLight)

                        ClickableRow("Expiry Date", passenger.expiryDate, false)
                    }
                }
            }

            item {
                SectionHeader("Contact Information")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(0.dp),
                    colors = CardDefaults.cardColors(containerColor = SwiftWhite)
                ) {
                    Column(modifier = Modifier.padding(start = 16.dp)) {
                        // WhatsApp Input
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp, horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("WhatsApp", color = SwiftBlack, modifier = Modifier.weight(0.4f))
                            Row(modifier = Modifier.weight(0.6f), verticalAlignment = Alignment.CenterVertically) {
                                Surface(color = SwiftGrayLight, shape = RoundedCornerShape(4.dp)) {
                                    Text("🇮🇩 62", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 12.sp)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                TextField(
                                    value = passenger.whatsapp,
                                    onValueChange = { passenger = passenger.copy(whatsapp = it) },
                                    placeholder = { Text("Please enter WhatsApp", color = SwiftGrayLight, fontSize = 14.sp) },
                                    colors = TextFieldDefaults.colors(
                                        unfocusedContainerColor = SwiftWhite,
                                        focusedContainerColor = SwiftWhite,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent
                                    ),
                                    textStyle = androidx.compose.ui.text.TextStyle(textAlign = androidx.compose.ui.text.style.TextAlign.End, color = SwiftGray),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                                )
                            }
                        }
                        HorizontalDivider(color = SwiftGrayLight)

                        // E-mail Input
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp, horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("E-mail", color = SwiftBlack, modifier = Modifier.weight(0.4f))
                            TextField(
                                value = passenger.email,
                                onValueChange = { passenger = passenger.copy(email = it) },
                                placeholder = { Text("Please enter your email address", color = SwiftGrayLight, fontSize = 14.sp) },
                                modifier = Modifier.weight(0.6f),
                                colors = TextFieldDefaults.colors(
                                    unfocusedContainerColor = SwiftWhite,
                                    focusedContainerColor = SwiftWhite,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent
                                ),
                                textStyle = androidx.compose.ui.text.TextStyle(textAlign = androidx.compose.ui.text.style.TextAlign.End, color = SwiftGray),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                            )
                        }
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(32.dp))
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
