package com.example.swift.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Train
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swift.ui.theme.*
import com.example.swift.viewmodel.AuthState
import com.example.swift.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var agreedToTerms by remember { mutableStateOf(false) }

    val authState by authViewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            authViewModel.resetState()
            onLoginSuccess()
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

        // Logo
        Icon(
            imageVector = Icons.Default.Train,
            contentDescription = "Swift Logo",
            tint = SwiftRed,
            modifier = Modifier.size(64.dp)
        )
        Text(
            text = "Swift",
            color = SwiftRed,
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            fontStyle = FontStyle.Italic
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Title
        Text(
            text = "Enter your E-mail and password",
            style = MaterialTheme.typography.headlineSmall,
            color = SwiftBlack,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Please log in using your registered e-mail address",
            style = MaterialTheme.typography.bodyMedium,
            color = SwiftGray,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Email Field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("E-mail") },
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

        // Password Field
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Enter password") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Toggle password",
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

        // Forgot Password
        Text(
            text = "Forgot Password",
            color = SwiftDarkTeal,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .align(Alignment.End)
                .clickable { }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Terms Checkbox
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
                text = "I have read and agree to the terms of service and privacy policy agreement",
                style = MaterialTheme.typography.bodySmall,
                color = SwiftGray
            )
        }

        // Error Message
        if (authState is AuthState.Error) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = SwiftRed.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = (authState as AuthState.Error).message,
                    color = SwiftRed,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(12.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Login Button
        Button(
            onClick = { authViewModel.login(email, password) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SwiftRed),
            enabled = email.isNotBlank() && password.isNotBlank() && agreedToTerms && authState !is AuthState.Loading
        ) {
            if (authState is AuthState.Loading) {
                CircularProgressIndicator(color = SwiftWhite, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else {
                Text("Log in", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Register Button
        OutlinedButton(
            onClick = {
                authViewModel.resetState()
                onNavigateToRegister()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = SwiftRed),
            border = ButtonDefaults.outlinedButtonBorder(enabled = true)
        ) {
            Text("Register", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = SwiftRed)
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}
