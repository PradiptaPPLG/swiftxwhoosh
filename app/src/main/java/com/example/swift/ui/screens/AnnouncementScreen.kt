package com.example.swift.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.swift.ui.theme.SwiftGray
import com.example.swift.ui.theme.SwiftRed
import com.example.swift.ui.theme.SwiftWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnouncementScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pengumuman Swift", fontWeight = FontWeight.SemiBold, fontSize = 18.sp) },
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
            AnnouncementCard(
                title = "Jadwal Pola Operasi Swift Mulai 1 Februari 2025",
                date = "25 Januari 2025",
                content = "Swift resmi membuka penjualan tiket keberangkatan 1 Februari 2025 " +
                        "dengan 62 jadwal perhari mulai tanggal 25 Januari 2025 di seluruh " +
                        "saluran penjualan tiket Swift. Penambahan jadwal ini memberikan lebih banyak pilihan perjalanan, " +
                        "baik untuk rute Jakarta-Bandung maupun perjalanan ke Karawang.\n\n" +
                        "Terdapat total 62 jadwal perjalanan Swift per hari, dengan pembagian 31 jadwal keberangkatan " +
                        "dari arah Jakarta dan 31 jadwal dari arah Bandung. Dalam jadwal terbaru, rute Jakarta-Bandung " +
                        "tersedia setiap 30 menit sekali, sementara rute Jakarta/Bandung-Karawang tersedia setiap 1 jam sekali.\n\n" +
                        "Tiket perjalanan Swift dapat diperoleh melalui seluruh kanal penjualan Swift, baik secara online " +
                        "melalui aplikasi Swift, situs ticket.swift.co.id, dan platform mitra. Tiket juga dapat dibeli secara offline melalui loket dan Ticket Vending Machine (TVM) di stasiun."
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            AnnouncementCard(
                title = "Ketentuan Boarding",
                date = "Info Layanan",
                content = "1. Boarding adalah proses pemeriksaan tiket penumpang Swift sebelum diizinkan masuk ke peron Stasiun Swift dan melanjutkan perjalanan.\n" +
                        "2. Boarding atau pemeriksaan tiket dibuka paling cepat 30 menit sebelum jadwal keberangkatan dan ditutup 5 menit sebelum jadwal keberangkatan.\n" +
                        "3. Penumpang wajib memasukkan Tiket Fisik atau memindai QR Code Tiket dari Aplikasi ke gate boarding sesuai dengan jadwal dan waktu yang ditentukan.\n" +
                        "4. Setiap penumpang wajib menggunakan Tiket Fisik atau QR Code tiketnya masing-masing.\n" +
                        "5. Penumpang Swift diimbau untuk tiba di stasiun 30 menit sebelum jadwal keberangkatan untuk menghindari tertinggal kereta.\n" +
                        "6. Penumpang yang datang terlambat, maka tiket yang telah dibeli tidak dapat dikembalikan dan tidak dapat digunakan untuk perjalanan kereta selanjutnya."
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            AnnouncementCard(
                title = "Pengembalian Dana / Perubahan Jadwal",
                date = "Syarat & Ketentuan",
                content = "1. Pembatalan atau perubahan jadwal tiket dilakukan secara online bagi pemesanan di aplikasi Swift atau website ticket.swift.co.id. Pemesanan lain dilakukan secara offline di loket stasiun dengan mengisi form terlebih dahulu.\n" +
                        "2. Pembatalan dapat dilakukan hingga 2 jam sebelum keberangkatan. Perubahan jadwal secara online hingga 5 menit sebelum keberangkatan, dan offline hingga 15 menit setelah keberangkatan.\n" +
                        "3. Pembatalan dikenakan potongan bea 25% dari harga tiket. Perubahan jadwal ke tanggal berbeda juga memotong bea 25%.\n" +
                        "4. Proses pengembalian dana memakan waktu selambatnya 15 hari.\n" +
                        "5. Jika tidak hadir, penumpang harus membawa surat kuasa bermaterai Rp 10.000 untuk dapat mewakilkan pembatalan tiket."
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun AnnouncementCard(title: String, date: String, content: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SwiftWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SwiftBlack
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = date,
                style = MaterialTheme.typography.bodySmall,
                color = SwiftRed,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = SwiftGray,
                lineHeight = 22.sp
            )
        }
    }
}
