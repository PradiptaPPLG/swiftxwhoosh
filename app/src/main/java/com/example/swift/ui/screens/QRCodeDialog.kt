package com.example.swift.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.swift.ui.theme.SwiftBlack
import com.example.swift.ui.theme.SwiftGray
import com.example.swift.ui.theme.SwiftRed
import com.example.swift.ui.theme.SwiftWhite
import com.example.swift.viewmodel.BookingViewModel

@Composable
fun QRCodeDialog(
    bookingViewModel: BookingViewModel,
    onDismiss: () -> Unit
) {
    val currentBooking = bookingViewModel.currentBooking
    val passenger = currentBooking?.passengers?.firstOrNull() // Usually specific to a passenger, picking first for demo
    val hiddenId = if (passenger != null && passenger.identityNumber.length > 4) {
        passenger.identityNumber.take(4) + "****" + passenger.identityNumber.takeLast(2)
    } else passenger?.identityNumber ?: "N/A"

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(12.dp))
                .background(SwiftWhite)
                .padding(bottom = 16.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Whoosh",
                        color = SwiftRed,
                        fontWeight = FontWeight.ExtraBold,
                        fontStyle = FontStyle.Italic,
                        fontSize = 18.sp
                    )
                    Text("QR Code", color = SwiftBlack, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = SwiftGray,
                        modifier = Modifier.clickable { onDismiss() }
                    )
                }
                
                Text("62001Xz086202604199253096", color = SwiftBlack, fontSize = 14.sp)
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Placeholder QR Code
                Icon(
                    imageVector = Icons.Default.QrCode2,
                    contentDescription = "QR Code",
                    modifier = Modifier.size(240.dp),
                    tint = SwiftBlack
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable {  }) {
                    Text("Click to Refresh Status", color = Color(0xFF1CB0B6), fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = Color(0xFF1CB0B6), modifier = Modifier.size(16.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Ticket Info
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(currentBooking?.origin?.displayName ?: "Halim", color = SwiftBlack, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("G1063", color = SwiftRed, fontSize = 12.sp)
                        HorizontalDivider(modifier = Modifier.width(40.dp).padding(vertical = 2.dp), color = SwiftGray)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(currentBooking?.destination?.displayName ?: "Tegalluar", color = SwiftBlack, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
                    Text("Departure Time: ${currentBooking?.departureTime ?: "21:25"} 18/04/2026", color = SwiftBlack, fontSize = 14.sp)
                    Text("Seat: Coach ${currentBooking?.selectedCoachId ?: "03"} | ${currentBooking?.coachClass?.displayName ?: "Premium Economy"} ${currentBooking?.selectedSeats?.firstOrNull() ?: "04A"}", color = SwiftBlack, fontSize = 14.sp)
                    Text("Name: ${passenger?.name ?: "Kiki Supendi MT"}", color = SwiftBlack, fontSize = 14.sp)
                    Text("Identity No.: $hiddenId", color = SwiftBlack, fontSize = 14.sp)
                }
            }
        }
    }
}
