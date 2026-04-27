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
    onBack: () -> Unit
) {
    val passengers = bookingViewModel.passengers
    val ticketCount = bookingViewModel.ticketCount
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Penumpang", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SwiftWhite,
                    titleContentColor = SwiftBlack,
                    navigationIconContentColor = SwiftBlack
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            val allValid = passengers.all { it.name.isNotBlank() && it.identityNumber.isNotBlank() } &&
                    passengers.firstOrNull()?.email?.isNotBlank() == true

            Surface(
                color = SwiftWhite,
                shadowElevation = 16.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Button(
                        onClick = {
                            if (allValid) onNextClicked() 
                            else coroutineScope.launch { snackbarHostState.showSnackbar("Mohon lengkapi semua data penumpang") }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        enabled = allValid,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SwiftRed,
                            disabledContainerColor = SwiftGrayLight
                        )
                    ) {
                        Text(
                            text = "Lanjut Pilih Kursi",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (allValid) SwiftWhite else SwiftGrayMedium
                        )
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
            // Header: Informasi Perjalanan
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SwiftWhite),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "G1123 | ${bookingViewModel.origin.displayName} - ${bookingViewModel.destination.displayName}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SwiftBlack
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${bookingViewModel.departureDate} | ${bookingViewModel.selectedTime} WIB",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SwiftGray
                        )
                    }
                }
            }

            // List Penumpang
            items(ticketCount) { index ->
                val passenger = passengers.getOrNull(index) ?: PassengerDetail()
                PassengerFormCard(
                    index = index,
                    passenger = passenger,
                    onPassengerChange = { updated ->
                        bookingViewModel.updatePassengerData(index, updated)
                    },
                    onSavedContactsClick = {
                        coroutineScope.launch { snackbarHostState.showSnackbar("Fitur Daftar Kontak segera hadir!") }
                    }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassengerFormCard(
    index: Int,
    passenger: PassengerDetail,
    onPassengerChange: (PassengerDetail) -> Unit,
    onSavedContactsClick: () -> Unit
) {
    var expandedIdentity by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = SwiftWhite),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Penumpang X & Tombol Kontak
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Penumpang ${index + 1}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SwiftBlack
                )
                TextButton(onClick = onSavedContactsClick) {
                    Icon(Icons.Default.Contacts, contentDescription = null, modifier = Modifier.size(16.dp), tint = SwiftRed)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Pilih dari Daftar", color = SwiftRed, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp), color = SwiftGrayLight)

            // Tipe Penumpang (Segmented Button for Dewasa/Anak)
            Text("Tipe Penumpang", style = MaterialTheme.typography.labelMedium, color = SwiftGray)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                PassengerType.entries.forEach { type ->
                    val isSelected = passenger.passengerType == type
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .background(
                                color = if (isSelected) SwiftRed.copy(alpha = 0.1f) else SwiftWhite,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { onPassengerChange(passenger.copy(passengerType = type)) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = type.displayName,
                            color = if (isSelected) SwiftRed else SwiftGrayMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // Jenis Identitas
            ExposedDropdownMenuBox(
                expanded = expandedIdentity,
                onExpandedChange = { expandedIdentity = !expandedIdentity }
            ) {
                OutlinedTextField(
                    value = passenger.identityType.displayName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Jenis Identitas") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedIdentity) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SwiftRed,
                        focusedLabelColor = SwiftRed
                    )
                )
                ExposedDropdownMenu(
                    expanded = expandedIdentity,
                    onDismissRequest = { expandedIdentity = false },
                    modifier = Modifier.background(SwiftWhite)
                ) {
                    IdentityType.entries.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.displayName, color = SwiftBlack) },
                            onClick = {
                                onPassengerChange(passenger.copy(identityType = type))
                                expandedIdentity = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Nomor Identitas
            OutlinedTextField(
                value = passenger.identityNumber,
                onValueChange = { onPassengerChange(passenger.copy(identityNumber = it)) },
                label = { Text("Nomor ${passenger.identityType.displayName}") },
                keyboardOptions = KeyboardOptions(keyboardType = if(passenger.identityType == IdentityType.KTP) KeyboardType.Number else KeyboardType.Ascii),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SwiftRed,
                    focusedLabelColor = SwiftRed
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Nama Lengkap
            OutlinedTextField(
                value = passenger.name,
                onValueChange = { onPassengerChange(passenger.copy(name = it)) },
                label = { Text("Nama Lengkap") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = SwiftGray) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SwiftRed,
                    focusedLabelColor = SwiftRed
                )
            )

            // Hanya Pemesan (Penumpang 1) yang dimintai Email e-ticket
            if (index == 0) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = passenger.email,
                    onValueChange = { onPassengerChange(passenger.copy(email = it)) },
                    label = { Text("Email (Untuk Pengiriman Tiket)") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = SwiftGray) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SwiftRed,
                        focusedLabelColor = SwiftRed
                    )
                )
            }
        }
    }
}
