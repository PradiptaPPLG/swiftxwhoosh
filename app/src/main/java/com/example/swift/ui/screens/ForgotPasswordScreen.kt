package com.example.swift.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.swift.viewmodel.PasswordValidator

private enum class ForgotStep { EMAIL, OTP, NEW_PASSWORD, DONE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    authViewModel: AuthViewModel,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val otpState by authViewModel.otpState.collectAsState()

    var currentStep by remember { mutableStateOf(ForgotStep.EMAIL) }
    var email      by remember { mutableStateOf("") }
    var otpInput   by remember { mutableStateOf("") }
    var newPassword   by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showNewPass    by remember { mutableStateOf(false) }
    var showConfirmPass by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }

    val pwResult = PasswordValidator.validate(newPassword)

    // React to VM state changes
    LaunchedEffect(otpState) {
        when {
            otpState is AuthState.Success && currentStep == ForgotStep.EMAIL ->
                currentStep = ForgotStep.OTP

            otpState is AuthState.Success && currentStep == ForgotStep.NEW_PASSWORD ->
                currentStep = ForgotStep.DONE

            otpState is AuthState.Error ->
                localError = (otpState as AuthState.Error).message
        }
    }

    LaunchedEffect(currentStep) {
        if (currentStep == ForgotStep.DONE) {
            kotlinx.coroutines.delay(2200)
            authViewModel.resetOtpState()
            onSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Forgot Password", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = {
                        authViewModel.resetOtpState()
                        onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SwiftRed,
                    titleContentColor = SwiftWhite,
                    navigationIconContentColor = SwiftWhite
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SwiftPinkBg)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // ── Step Indicator ───────────────────────────────────────────
            StepIndicator(currentStep = currentStep)

            Spacer(modifier = Modifier.height(32.dp))

            // ── Animated content per step ─────────────────────────────────
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    slideInHorizontally { it } + fadeIn() togetherWith
                    slideOutHorizontally { -it } + fadeOut()
                },
                label = "forgot_step"
            ) { step ->
                when (step) {
                    ForgotStep.EMAIL -> EmailStep(
                        email = email,
                        onEmailChange = { email = it; localError = null },
                        isLoading = otpState is AuthState.Loading,
                        error = localError,
                        onSend = {
                            localError = null
                            authViewModel.sendPasswordResetOtp(email)
                        }
                    )

                    ForgotStep.OTP -> OtpStep(
                        email = authViewModel.pendingOtpEmail,
                        otpInput = otpInput,
                        onOtpChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) { otpInput = it; localError = null } },
                        isLoading = otpState is AuthState.Loading,
                        error = localError,
                        onVerify = {
                            localError = null
                            if (authViewModel.verifyOtp(otpInput)) {
                                authViewModel.resetOtpState()
                                currentStep = ForgotStep.NEW_PASSWORD
                            }
                        },
                        onResend = {
                            otpInput = ""
                            localError = null
                            authViewModel.sendPasswordResetOtp(authViewModel.pendingOtpEmail)
                        }
                    )

                    ForgotStep.NEW_PASSWORD -> NewPasswordStep(
                        newPassword = newPassword,
                        confirmPassword = confirmPassword,
                        showNewPass = showNewPass,
                        showConfirmPass = showConfirmPass,
                        onNewPassChange = { newPassword = it; localError = null },
                        onConfirmPassChange = { confirmPassword = it; localError = null },
                        onToggleNewPass = { showNewPass = !showNewPass },
                        onToggleConfirmPass = { showConfirmPass = !showConfirmPass },
                        pwResult = pwResult,
                        isLoading = otpState is AuthState.Loading,
                        error = localError,
                        onSave = {
                            localError = null
                            authViewModel.changePassword(newPassword, confirmPassword)
                        }
                    )

                    ForgotStep.DONE -> DoneStep()
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Sub-composables
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun StepIndicator(currentStep: ForgotStep) {
    val steps = listOf("Email", "OTP", "New Password")
    val currentIndex = when (currentStep) {
        ForgotStep.EMAIL        -> 0
        ForgotStep.OTP          -> 1
        ForgotStep.NEW_PASSWORD -> 2
        ForgotStep.DONE         -> 2
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, label ->
            val done   = index < currentIndex
            val active = index == currentIndex

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            done   -> SwiftRed
                            active -> SwiftRed
                            else   -> SwiftGrayLight
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (done) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = SwiftWhite, modifier = Modifier.size(18.dp))
                } else {
                    Text(
                        text = "${index + 1}",
                        color = if (active) SwiftWhite else SwiftGray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            if (index < steps.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier
                        .width(48.dp)
                        .padding(horizontal = 4.dp),
                    color = if (done) SwiftRed else SwiftGrayLight,
                    thickness = 2.dp
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        steps.forEachIndexed { index, label ->
            val active = index == when (currentStep) {
                ForgotStep.EMAIL        -> 0
                ForgotStep.OTP          -> 1
                ForgotStep.NEW_PASSWORD, ForgotStep.DONE -> 2
            }
            Box(modifier = Modifier.width(76.dp), contentAlignment = Alignment.Center) {
                Text(
                    label,
                    fontSize = 11.sp,
                    color = if (active) SwiftRed else SwiftGray,
                    fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun StepCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SwiftWhite),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp), content = content)
    }
}

@Composable
private fun ErrorBox(message: String) {
    Spacer(modifier = Modifier.height(12.dp))
    Card(
        colors = CardDefaults.cardColors(containerColor = SwiftRed.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = SwiftRed, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(message, color = SwiftRed, fontSize = 13.sp)
        }
    }
}

// ── Step 1: Enter Email ───────────────────────────────────────────────────────

@Composable
private fun EmailStep(
    email: String,
    onEmailChange: (String) -> Unit,
    isLoading: Boolean,
    error: String?,
    onSend: () -> Unit
) {
    StepCard {
        Icon(
            Icons.Default.Email,
            contentDescription = null,
            tint = SwiftRed,
            modifier = Modifier.size(48.dp).align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Enter Recovery Email",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = SwiftBlack
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            "Enter the email address associated with your Swift account. We'll send a 6-digit OTP code.",
            fontSize = 13.sp,
            color = SwiftGray,
            lineHeight = 20.sp
        )
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Email Address") },
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

        if (error != null) ErrorBox(error)

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onSend,
            enabled = email.isNotBlank() && !isLoading,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SwiftRed)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = SwiftWhite, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
            } else {
                Text("Send OTP", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// ── Step 2: Enter OTP ─────────────────────────────────────────────────────────

@Composable
private fun OtpStep(
    email: String,
    otpInput: String,
    onOtpChange: (String) -> Unit,
    isLoading: Boolean,
    error: String?,
    onVerify: () -> Unit,
    onResend: () -> Unit
) {
    StepCard {
        Icon(
            Icons.Default.Lock,
            contentDescription = null,
            tint = SwiftRed,
            modifier = Modifier.size(48.dp).align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Enter OTP Code", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = SwiftBlack)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            "A 6-digit code has been sent to\n$email",
            fontSize = 13.sp,
            color = SwiftGray,
            lineHeight = 20.sp
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Big OTP input
        OutlinedTextField(
            value = otpInput,
            onValueChange = onOtpChange,
            label = { Text("6-Digit OTP") },
            leadingIcon = { Icon(Icons.Default.Password, null, tint = SwiftRed) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SwiftRed,
                focusedLabelColor = SwiftRed,
                cursorColor = SwiftRed
            )
        )

        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onResend, enabled = !isLoading) {
                Text("Resend OTP", color = SwiftRed, fontSize = 13.sp)
            }
        }

        if (error != null) ErrorBox(error)

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onVerify,
            enabled = otpInput.length == 6 && !isLoading,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SwiftRed)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = SwiftWhite, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
            } else {
                Text("Verify OTP", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// ── Step 3: New Password ──────────────────────────────────────────────────────

@Composable
private fun NewPasswordStep(
    newPassword: String,
    confirmPassword: String,
    showNewPass: Boolean,
    showConfirmPass: Boolean,
    onNewPassChange: (String) -> Unit,
    onConfirmPassChange: (String) -> Unit,
    onToggleNewPass: () -> Unit,
    onToggleConfirmPass: () -> Unit,
    pwResult: PasswordValidator.Result,
    isLoading: Boolean,
    error: String?,
    onSave: () -> Unit
) {
    StepCard {
        Icon(
            Icons.Default.LockReset,
            contentDescription = null,
            tint = SwiftRed,
            modifier = Modifier.size(48.dp).align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Set New Password", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = SwiftBlack)
        Spacer(modifier = Modifier.height(6.dp))
        Text("Choose a strong password for your account.", fontSize = 13.sp, color = SwiftGray)
        Spacer(modifier = Modifier.height(24.dp))

        PasswordField(
            value = newPassword,
            onValueChange = onNewPassChange,
            label = "New Password",
            visible = showNewPass,
            onToggle = onToggleNewPass
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Password requirements checklist
        PasswordRequirements(result = pwResult, password = newPassword)

        Spacer(modifier = Modifier.height(12.dp))

        PasswordField(
            value = confirmPassword,
            onValueChange = onConfirmPassChange,
            label = "Confirm Password",
            visible = showConfirmPass,
            onToggle = onToggleConfirmPass
        )

        // Match indicator
        if (confirmPassword.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (newPassword == confirmPassword) Icons.Default.CheckCircle else Icons.Default.Cancel,
                    contentDescription = null,
                    tint = if (newPassword == confirmPassword) Color(0xFF16A34A) else SwiftRed,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (newPassword == confirmPassword) "Passwords match" else "Passwords do not match",
                    color = if (newPassword == confirmPassword) Color(0xFF16A34A) else SwiftRed,
                    fontSize = 12.sp
                )
            }
        }

        if (error != null) ErrorBox(error)

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onSave,
            enabled = pwResult.isValid && newPassword == confirmPassword && !isLoading,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SwiftRed)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = SwiftWhite, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
            } else {
                Text("Save Password", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// ── Done ──────────────────────────────────────────────────────────────────────

@Composable
private fun DoneStep() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(Color(0xFF16A34A).copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF16A34A), modifier = Modifier.size(60.dp))
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text("Password Changed!", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = SwiftBlack)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Your password has been successfully updated.\nRedirecting to login…",
            fontSize = 14.sp,
            color = SwiftGray,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
        Spacer(modifier = Modifier.height(24.dp))
        CircularProgressIndicator(color = SwiftRed, modifier = Modifier.size(28.dp))
    }
}

// ── Shared helpers ────────────────────────────────────────────────────────────

@Composable
private fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    visible: Boolean,
    onToggle: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(Icons.Default.Lock, null, tint = SwiftRed) },
        trailingIcon = {
            IconButton(onClick = onToggle) {
                Icon(
                    if (visible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = "Toggle",
                    tint = SwiftGray
                )
            }
        },
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = SwiftRed,
            focusedLabelColor = SwiftRed,
            cursorColor = SwiftRed
        )
    )
}

@Composable
private fun PasswordRequirements(result: PasswordValidator.Result, password: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SwiftGrayLight.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Password Requirements", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = SwiftGray)
            Spacer(modifier = Modifier.height(8.dp))
            RequirementRow("At least ${PasswordValidator.MIN_LENGTH} characters", result.hasMinLength)
            RequirementRow("At least 1 uppercase letter (A–Z)", result.hasUppercase)
            RequirementRow("At least 1 number (0–9)", result.hasDigit)
            RequirementRow("At least 1 special character (!@#\$…)", result.hasSymbol)
        }
    }
}

@Composable
private fun RequirementRow(text: String, met: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 3.dp)
    ) {
        Icon(
            imageVector = if (met) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (met) Color(0xFF16A34A) else SwiftGrayMedium,
            modifier = Modifier.size(15.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, fontSize = 12.sp, color = if (met) Color(0xFF16A34A) else SwiftGray)
    }
}
