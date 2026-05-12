package com.example.swift.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swift.ui.theme.*
import com.example.swift.viewmodel.AuthViewModel

@Composable
fun AccountScreen(
    authViewModel: AuthViewModel,
    onLogout: () -> Unit,
    onNavigate: (String) -> Unit
) {
    var showLogoutDialog    by remember { mutableStateOf(false) }
    var showAboutDialog     by remember { mutableStateOf(false) }
    var showLanguageSheet   by remember { mutableStateOf(false) }
    var showCallCenterDialog by remember { mutableStateOf(false) }
    var showRailRegDialog   by remember { mutableStateOf(false) }
    var currentLanguage     by remember { mutableStateOf("English") }

    val userName  by authViewModel.userName.collectAsState()
    val userEmail by authViewModel.userEmail.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SwiftPinkBg)
            .verticalScroll(rememberScrollState())
    ) {
        // ── Red Header ─────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SwiftRed)
                .padding(top = 24.dp, bottom = 32.dp, start = 20.dp, end = 20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(SwiftWhite.copy(alpha = 0.25f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = SwiftWhite,
                        modifier = Modifier.size(36.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        userName.ifBlank { "User" },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = SwiftWhite
                    )
                    if (userEmail.isNotBlank()) {
                        Text(userEmail, fontSize = 13.sp, color = SwiftWhite.copy(alpha = 0.8f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Common Functions ───────────────────────────────────────────────────
        SectionCard(title = "Common Functions") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AccountGridItem(Icons.Default.Badge, "Passenger") {
                    onNavigate("AddPassenger")
                }
                AccountGridItem(Icons.Default.Lock, "Password") {
                    // Navigate to ForgotPassword flow (same screen, OTP-based)
                    onNavigate("ChangePassword")
                }
                AccountGridItem(Icons.Default.GTranslate, "Language") {
                    showLanguageSheet = true
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AccountGridItem(Icons.Default.Notifications, "Notifications") {
                    onNavigate("Announcement")
                }
                AccountGridItem(Icons.Default.Phone, "Call Center") {
                    showCallCenterDialog = true
                }
                AccountGridItem(Icons.Default.Info, "About") {
                    showAboutDialog = true
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ── Service Information ────────────────────────────────────────────────
        SectionCard(title = "Service Information") {
            MenuItemRow(
                icon = Icons.Default.VolumeUp,
                title = "Announcements & Notices"
            ) { onNavigate("Announcement") }

            HorizontalDivider(color = SwiftGrayLight, modifier = Modifier.padding(start = 34.dp))

            MenuItemRow(
                icon = Icons.Default.Policy,
                title = "Refund & Reschedule Policy"
            ) { onNavigate("RefundPolicy") }

            HorizontalDivider(color = SwiftGrayLight, modifier = Modifier.padding(start = 34.dp))

            MenuItemRow(
                icon = Icons.Default.Train,
                title = "Train Schedule Info"
            ) { onNavigate("ScheduleInfo") }

            HorizontalDivider(color = SwiftGrayLight, modifier = Modifier.padding(start = 34.dp))

            MenuItemRow(
                icon = Icons.Default.Gavel,
                title = "Railway Regulations",
                trailing = "KAI Rules"
            ) { showRailRegDialog = true }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ── Account Actions ────────────────────────────────────────────────────
        SectionCard(title = "Account") {
            MenuItemRow(
                icon = Icons.Default.ManageAccounts,
                title = "Edit Profile"
            ) { /* future */ }

            HorizontalDivider(color = SwiftGrayLight, modifier = Modifier.padding(start = 34.dp))

            MenuItemRow(
                icon = Icons.Default.Security,
                title = "Change Password"
            ) { onNavigate("ChangePassword") }

            HorizontalDivider(color = SwiftGrayLight, modifier = Modifier.padding(start = 34.dp))

            // Biometric Toggle
            val context = androidx.compose.ui.platform.LocalContext.current
            var isBiometricEnrolled by remember { mutableStateOf(authViewModel.isBiometricEnrolled(context)) }
            // Force true so it's always visible for the user to see and toggle
            val canUseBiometric = true

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Fingerprint, contentDescription = null, tint = SwiftRed, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "Biometric Authentication",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = SwiftBlack,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = isBiometricEnrolled,
                    onCheckedChange = { enable ->
                        if (enable && canUseBiometric) {
                            val activity = context as? androidx.fragment.app.FragmentActivity
                            if (activity != null) {
                                com.example.swift.utils.BiometricHelper.showPrompt(
                                    activity = activity,
                                    title = "Enable Biometric Login",
                                    subtitle = "Verify your identity to link this account",
                                    preferFace = false,
                                    onSuccess = {
                                        authViewModel.setBiometricEnrolled(context, true)
                                        isBiometricEnrolled = true
                                    },
                                    onError = { /* Handle error / show toast */ },
                                    onFailure = { /* Handle fail */ }
                                )
                            }
                        } else if (!enable) {
                            authViewModel.setBiometricEnrolled(context, false)
                            isBiometricEnrolled = false
                        }
                    },
                    colors = SwitchDefaults.colors(checkedThumbColor = SwiftWhite, checkedTrackColor = SwiftRed)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Logout Button ──────────────────────────────────────────────────────
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clickable { showLogoutDialog = true },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SwiftRed.copy(alpha = 0.08f)),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.Logout, null, tint = SwiftRed, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text("Log Out", color = SwiftRed, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Swift Express v1.2.002 · © 2026",
            fontSize = 11.sp,
            color = SwiftGrayMedium,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(40.dp))
    }

    // ── Logout Confirmation ────────────────────────────────────────────────────
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = { Icon(Icons.Default.Logout, null, tint = SwiftRed) },
            title = { Text("Log Out", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to log out from your account?") },
            confirmButton = {
                Button(
                    onClick = { authViewModel.logout(); onLogout() },
                    colors = ButtonDefaults.buttonColors(containerColor = SwiftRed),
                    shape = RoundedCornerShape(8.dp)
                ) { Text("Log Out") }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel") }
            }
        )
    }

    // ── About Dialog ───────────────────────────────────────────────────────────
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            icon = { Icon(Icons.Default.Train, null, tint = SwiftRed) },
            title = { Text("About Swift Express", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Swift Express Ticketing System", fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Version 1.2.002", color = SwiftGray, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "The fastest, most comfortable high-speed rail experience in Indonesia.",
                        fontSize = 13.sp,
                        color = SwiftGray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("© 2026 Swift Express Indonesia", fontSize = 12.sp, color = SwiftGrayMedium)
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) { Text("Close", color = SwiftRed) }
            }
        )
    }

    // ── Call Center Dialog ─────────────────────────────────────────────────────
    if (showCallCenterDialog) {
        AlertDialog(
            onDismissRequest = { showCallCenterDialog = false },
            icon = { Icon(Icons.Default.Phone, null, tint = SwiftRed) },
            title = { Text("Call Center", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ContactRow(icon = Icons.Default.Phone, label = "Phone", value = "021-121")
                    ContactRow(icon = Icons.Default.Email, label = "Email", value = "cs@swiftexpress.id")
                    ContactRow(icon = Icons.Default.Language, label = "Website", value = "swiftexpress.id")
                    ContactRow(icon = Icons.Default.AccessTime, label = "Hours", value = "24/7 — Everyday")
                }
            },
            confirmButton = {
                TextButton(onClick = { showCallCenterDialog = false }) { Text("Close", color = SwiftRed) }
            }
        )
    }

    // ── Railway Regulations Dialog ─────────────────────────────────────────────
    if (showRailRegDialog) {
        AlertDialog(
            onDismissRequest = { showRailRegDialog = false },
            icon = { Icon(Icons.Default.Gavel, null, tint = SwiftRed) },
            title = { Text("Railway Regulations", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    RegRow("1", "Passengers must carry a valid ID matching their ticket.")
                    RegRow("2", "Check-in at the station at least 30 minutes before departure.")
                    RegRow("3", "No smoking, dangerous goods, or live animals inside trains.")
                    RegRow("4", "Priority seats must be given to the elderly, pregnant women & disabled.")
                    RegRow("5", "Maximum baggage: 20kg per passenger.")
                    RegRow("6", "Tickets are non-transferable. Resale is strictly prohibited.")
                }
            },
            confirmButton = {
                TextButton(onClick = { showRailRegDialog = false }) { Text("Understood", color = SwiftRed) }
            }
        )
    }

    // ── Language Sheet ─────────────────────────────────────────────────────────
    if (showLanguageSheet) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showLanguageSheet = false }) {
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = SwiftWhite)) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.GTranslate, null, tint = SwiftRed, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Select Language", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    listOf(
                        "🇬🇧  English",
                        "🇮🇩  Bahasa Indonesia"
                    ).forEach { lang ->
                        val cleanLang = if (lang.contains("English")) "English" else "Bahasa Indonesia"
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { currentLanguage = cleanLang; showLanguageSheet = false }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentLanguage == cleanLang,
                                onClick = { currentLanguage = cleanLang; showLanguageSheet = false },
                                colors = RadioButtonDefaults.colors(selectedColor = SwiftRed)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(lang, fontSize = 15.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Note: Language change applies to future updates.",
                        fontSize = 11.sp,
                        color = SwiftGrayMedium
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Sub-components
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SwiftWhite),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = SwiftBlack
            )
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
private fun AccountGridItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(90.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(SwiftRed.copy(alpha = 0.08f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = SwiftRed, modifier = Modifier.size(26.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = SwiftBlack,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 2,
            lineHeight = 14.sp
        )
    }
}

@Composable
private fun MenuItemRow(
    icon: ImageVector,
    title: String,
    trailing: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = SwiftRed, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = SwiftBlack,
            modifier = Modifier.weight(1f)
        )
        if (trailing != null) {
            Text(trailing, style = MaterialTheme.typography.bodySmall, color = SwiftGray)
            Spacer(modifier = Modifier.width(4.dp))
        }
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = SwiftGrayMedium,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun ContactRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = SwiftRed, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(label, fontSize = 11.sp, color = SwiftGray)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = SwiftBlack)
        }
    }
}

@Composable
private fun RegRow(number: String, text: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            "$number.",
            fontWeight = FontWeight.Bold,
            color = SwiftRed,
            fontSize = 13.sp,
            modifier = Modifier.width(20.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text, fontSize = 13.sp, color = SwiftBlack, lineHeight = 19.sp)
    }
}
