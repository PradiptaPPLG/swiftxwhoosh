package com.example.swift.ui.screens

import android.app.Activity
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.swift.ui.theme.*
import com.example.swift.utils.BiometricHelper
import com.example.swift.viewmodel.AuthState
import com.example.swift.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onForgotPassword: () -> Unit = {}
) {
    val context = LocalContext.current
    var email    by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var agreedToTerms   by remember { mutableStateOf(false) }
    var biometricError            by remember { mutableStateOf<String?>(null) }
    var showAccountSelector       by remember { mutableStateOf(false) }
    var accountSelectorPreferFace by remember { mutableStateOf(false) }

    val authState      by authViewModel.authState.collectAsState()
    val biometricState by authViewModel.biometricState.collectAsState()

    val enrolledAccounts = remember { authViewModel.getEnrolledBiometricAccounts(context) }
    val canUseBiometric = BiometricHelper.canAuthenticate(context)

    // Navigate on successful standard login
    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            authViewModel.resetState()
            onLoginSuccess()
        }
    }

    // Navigate on successful biometric login
    LaunchedEffect(biometricState) {
        if (biometricState is AuthState.Success) {
            authViewModel.resetBiometricState()
            onLoginSuccess()
        }
        if (biometricState is AuthState.Error) {
            biometricError = (biometricState as AuthState.Error).message
            authViewModel.resetBiometricState()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SwiftWhite)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        // ── Logo ──────────────────────────────────────────────────────────────
        androidx.compose.foundation.Image(
            painter = androidx.compose.ui.res.painterResource(id = com.example.swift.R.drawable.logo),
            contentDescription = "Swift Logo",
            modifier = Modifier.size(120.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // ── Title ─────────────────────────────────────────────────────────────
        Text(
            text = "Welcome Back!",
            style = MaterialTheme.typography.headlineSmall,
            color = SwiftBlack,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Sign in to continue your journey",
            style = MaterialTheme.typography.bodyMedium,
            color = SwiftGray,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(28.dp))

        // ── Email ─────────────────────────────────────────────────────────────
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("E-mail") },
            leadingIcon = { Icon(Icons.Default.Email, null, tint = SwiftRed) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SwiftRed,
                focusedLabelColor = SwiftRed,
                cursorColor = SwiftRed
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ── Password ──────────────────────────────────────────────────────────
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            leadingIcon = { Icon(Icons.Default.Lock, null, tint = SwiftRed) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Toggle",
                        tint = SwiftGray
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SwiftRed,
                focusedLabelColor = SwiftRed,
                cursorColor = SwiftRed
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // ── Forgot Password ───────────────────────────────────────────────────
        Text(
            text = "Forgot Password?",
            color = SwiftRed,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .align(Alignment.End)
                .clickable { onForgotPassword() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ── Terms Checkbox ────────────────────────────────────────────────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = agreedToTerms,
                onCheckedChange = { agreedToTerms = it },
                colors = CheckboxDefaults.colors(checkedColor = SwiftRed)
            )
            Text(
                text = "I agree to the terms of service and privacy policy",
                style = MaterialTheme.typography.bodySmall,
                color = SwiftGray
            )
        }

        // ── Error Messages ────────────────────────────────────────────────────
        val errorMsg = when {
            authState is AuthState.Error     -> (authState as AuthState.Error).message
            biometricError != null           -> biometricError!!
            else                             -> null
        }
        AnimatedVisibility(visible = errorMsg != null) {
            errorMsg?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = SwiftRed.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ErrorOutline, null, tint = SwiftRed, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(it, color = SwiftRed, fontSize = 13.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Login Button ──────────────────────────────────────────────────────
        Button(
            onClick = {
                biometricError = null
                authViewModel.resetState()
                authViewModel.login(email, password)
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SwiftRed),
            enabled = email.isNotBlank() && password.isNotBlank() && agreedToTerms &&
                      authState !is AuthState.Loading && biometricState !is AuthState.Loading
        ) {
            if (authState is AuthState.Loading) {
                CircularProgressIndicator(color = SwiftWhite, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else {
                Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log In", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        // ── Biometric Buttons (only shown when available) ─────────────────────
        if (canUseBiometric) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                "Sign in with:",
                fontSize = 13.sp,
                color = SwiftGray,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(12.dp))

            val onBiometricTrigger: (Boolean) -> Unit = onBiometricTrigger@{ isFace ->
                biometricError = null
                
                if (enrolledAccounts.isEmpty()) {
                    biometricError = "No accounts enrolled. Please login with password first."
                    return@onBiometricTrigger
                }

                // If multiple accounts, show selector first
                if (enrolledAccounts.size > 1) {
                    accountSelectorPreferFace = isFace
                    showAccountSelector = true
                } else {
                    val targetEmail = enrolledAccounts[0].first
                    val activity = context as? FragmentActivity
                    if (activity != null) {
                        BiometricHelper.showPrompt(
                            activity = activity,
                            title = "Swift Express",
                            subtitle = "Sign in as $targetEmail",
                            preferFace = isFace,
                            onSuccess = { authViewModel.onBiometricSuccess(context, targetEmail) },
                            onError   = { biometricError = it },
                            onFailure = { biometricError = "Biometric authentication failed" }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Biometrics (Fingerprint)
                OutlinedButton(
                    onClick = { onBiometricTrigger(false) },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SwiftRed),
                    enabled = biometricState !is AuthState.Loading
                ) {
                    if (biometricState is AuthState.Loading) {
                        CircularProgressIndicator(color = SwiftRed, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Fingerprint, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Biometrics", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Face ID
                OutlinedButton(
                    onClick = { onBiometricTrigger(true) },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SwiftRed),
                    enabled = biometricState !is AuthState.Loading
                ) {
                    if (biometricState is AuthState.Loading) {
                        CircularProgressIndicator(color = SwiftRed, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Face, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Face ID", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ── Register Button ───────────────────────────────────────────────────
        OutlinedButton(
            onClick = {
                biometricError = null
                authViewModel.resetState()
                onNavigateToRegister()
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = SwiftGray),
            border = ButtonDefaults.outlinedButtonBorder(enabled = true)
        ) {
            Text("Create New Account", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = SwiftBlack)
        }

        Spacer(modifier = Modifier.height(40.dp))
    }

    // ── Account Selector (Multi-Account Biometrics) ───────────────────────────
    if (showAccountSelector) {
        AlertDialog(
            onDismissRequest = { showAccountSelector = false },
            icon = { Icon(Icons.Default.AccountCircle, null, tint = SwiftRed, modifier = Modifier.size(32.dp)) },
            title = { Text("Choose Account", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Select an account to sign in with biometrics:", fontSize = 13.sp, color = SwiftGray)
                    Spacer(modifier = Modifier.height(16.dp))
                    enrolledAccounts.forEach { acc ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    showAccountSelector = false
                                    val activity = context as? FragmentActivity
                                    if (activity != null) {
                                        BiometricHelper.showPrompt(
                                            activity = activity,
                                            title = "Swift Express",
                                            subtitle = "Sign in as ${acc.first}",
                                            preferFace = accountSelectorPreferFace,
                                            onSuccess = { authViewModel.onBiometricSuccess(context, acc.first) },
                                            onError   = { biometricError = it },
                                            onFailure = { biometricError = "Biometric authentication failed" }
                                        )
                                    }
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Person, null, tint = SwiftRed)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(acc.first, fontWeight = FontWeight.Medium, color = SwiftBlack)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAccountSelector = false }) { Text("Cancel", color = SwiftGray) }
            }
        )
    }

    // Biometric prompt logic is now handled via BiometricHelper and the system UI
}
