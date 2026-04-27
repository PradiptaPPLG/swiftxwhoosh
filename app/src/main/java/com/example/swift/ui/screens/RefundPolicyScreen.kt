package com.example.swift.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swift.ui.theme.SwiftBlack
import com.example.swift.ui.theme.SwiftWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RefundPolicyScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pengembalian Dana / Perub...", fontWeight = FontWeight.SemiBold, fontSize = 18.sp) },
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
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SwiftWhite)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            RefundRuleItem(
                number = "1.",
                text = "Pembatalan atau perubahan jadwal tiket dilakukan secara online bagi pemesanan di aplikasi Swift atau website ticket.swift.co.id. Pemesanan melalui vending machine, loket, atau channel mitra lainnya dilakukan secara offline di loket stasiun Halim, Karawang, Padalarang, dan Tegalluar Summarecon dengan mengisi form terlebih dahulu."
            )
            
            RefundRuleItem(
                number = "2.",
                text = "- Pembatalan dapat dilakukan hingga 2 jam sebelum keberangkatan.\n" +
                       "- Perubahan jadwal secara online dapat dilakukan hingga 5 menit sebelum keberangkatan.\n" +
                       "- Perubahan jadwal secara offline dapat dilakukan hingga 15 menit setelah keberangkatan. Penumpang tetap harus mengantre dengan tertib saat berada di loket."
            )
            
            RefundRuleItem(
                number = "3.",
                text = "- Pembatalan dikenakan potongan bea 25% dari harga tiket.\n" +
                       "- Perubahan ke tanggal yang sama tidak dikenakan potongan bea.\n" +
                       "- Perubahan ke tanggal yang berbeda dikenakan potongan bea 25% dari harga tiket.\n" +
                       "- Untuk tiket yang sudah pernah diubah jadwalnya akan dikenakan potongan biaya 25% jika dilakukan perubahan jadwal kembali."
            )
            
            RefundRuleItem(
                number = "4.",
                text = "Proses pengembalian dana memakan waktu selambatnya 15 hari. Pengembalian dana untuk perubahan jadwal paling cepat 1x24 jam."
            )
            
            RefundRuleItem(
                number = "5.",
                text = "Pengembalian dana dilakukan secara transfer bank."
            )
            
            RefundRuleItem(
                number = "6.",
                text = "Jika penumpang yang bersangkutan tidak bisa hadir harus membawa surat kuasa bermaterai Rp 10.000 dan fotokopi bukti identitas pemberi kuasa kepada penerima kuasa untuk dapat melakukan pembatalan atau reschedule tiket."
            )
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun RefundRuleItem(number: String, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    ) {
        Text(
            text = number,
            style = MaterialTheme.typography.bodyMedium,
            color = SwiftBlack,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(20.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = SwiftBlack,
            lineHeight = 22.sp
        )
    }
}
