package com.example.swift.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swift.R
import com.example.swift.ui.theme.SwiftBlack
import com.example.swift.ui.theme.SwiftWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleInfoScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Jadwal Swift", fontWeight = FontWeight.SemiBold, fontSize = 18.sp) },
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
        ) {
            Image(
                painter = painterResource(id = R.drawable.jadwal1),
                contentDescription = "Jadwal Keberangkatan Swift",
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                contentScale = ContentScale.FillWidth
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Image(
                painter = painterResource(id = R.drawable.jadwal2),
                contentDescription = "Jadwal Integrasi KA Feeder",
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                contentScale = ContentScale.FillWidth
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Image(
                painter = painterResource(id = R.drawable.jadwal3),
                contentDescription = "Jadwal Integrasi Ekstra",
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                contentScale = ContentScale.FillWidth
            )
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
