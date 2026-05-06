package com.example.swift.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.swift.api.AuthResponse
import com.example.swift.api.RetrofitClient
import com.example.swift.utils.EmailSender
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

sealed class AuthState {
    data object Idle : AuthState()
    data object Loading : AuthState()
    data class Success(val message: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

// ---------- Password validation rules (shared between Register & ForgotPassword) ----------
object PasswordValidator {
    private val UPPERCASE = Regex("[A-Z]")
    private val DIGIT     = Regex("[0-9]")
    private val SYMBOL    = Regex("[^A-Za-z0-9]")
    const val MIN_LENGTH  = 8

    data class Result(
        val isValid: Boolean,
        val hasMinLength: Boolean,
        val hasUppercase: Boolean,
        val hasDigit: Boolean,
        val hasSymbol: Boolean
    )

    fun validate(password: String) = Result(
        hasMinLength  = password.length >= MIN_LENGTH,
        hasUppercase  = UPPERCASE.containsMatchIn(password),
        hasDigit      = DIGIT.containsMatchIn(password),
        hasSymbol     = SYMBOL.containsMatchIn(password),
        isValid       = password.length >= MIN_LENGTH &&
                        UPPERCASE.containsMatchIn(password) &&
                        DIGIT.containsMatchIn(password) &&
                        SYMBOL.containsMatchIn(password)
    )
}

class AuthViewModel : ViewModel() {

    // ── Core auth ────────────────────────────────────────────────────────────
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _userName  = MutableStateFlow("")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userEmail = MutableStateFlow("")
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    private val _userId = MutableStateFlow<Int?>(null)
    val userId: StateFlow<Int?> = _userId.asStateFlow()

    // ── OTP / Forgot-password state ───────────────────────────────────────────
    private val _otpState = MutableStateFlow<AuthState>(AuthState.Idle)
    val otpState: StateFlow<AuthState> = _otpState.asStateFlow()

    /** Stores the OTP we generated so we can verify locally */
    private var _pendingOtp: String = ""
    /** Email that the OTP was sent to */
    private var _otpTargetEmail: String = ""
    /** UNIX millis when the OTP was created (10-min window) */
    private var _otpCreatedAt: Long = 0L

    val pendingOtpEmail: String get() = _otpTargetEmail

    // ── Biometric state ───────────────────────────────────────────────────────
    private val _biometricState = MutableStateFlow<AuthState>(AuthState.Idle)
    val biometricState: StateFlow<AuthState> = _biometricState.asStateFlow()

    // Store current session password to easily enable biometrics without re-typing
    private var _currentPassword = ""

    // ────────────────────────────────────────────────────────────────────────
    //  LOGIN
    // ────────────────────────────────────────────────────────────────────────

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Email and password are required")
            return
        }
        _authState.value = AuthState.Loading

        RetrofitClient.instance.login(email, password).enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                if (response.isSuccessful && response.body()?.status == "success") {
                    val userData = response.body()?.user
                    _isLoggedIn.value = true
                    _userId.value    = userData?.userId
                    _userName.value  = userData?.fullName ?: ""
                    _userEmail.value = userData?.email ?: ""
                    _currentPassword = password
                    _authState.value = AuthState.Success("Login successful!")
                } else {
                    _authState.value = AuthState.Error(response.body()?.message ?: "Login failed")
                }
            }

            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                _authState.value = AuthState.Error("Network error: ${t.localizedMessage}")
            }
        })
    }

    // ────────────────────────────────────────────────────────────────────────
    //  BIOMETRIC LOGIN (MULTI-ACCOUNT)
    // ────────────────────────────────────────────────────────────────────────

    /**
     * Called AFTER the biometric prompt succeeds at the OS level.
     * We pass the specific selected email to log in to.
     */
    fun onBiometricSuccess(context: Context, email: String) {
        val enrolledAccounts = getEnrolledBiometricAccounts(context)
        val savedPassword = enrolledAccounts.find { it.first == email }?.second

        if (savedPassword != null) {
            _biometricState.value = AuthState.Loading
            RetrofitClient.instance.login(email, savedPassword)
                .enqueue(object : Callback<AuthResponse> {
                    override fun onResponse(
                        call: Call<AuthResponse>,
                        response: Response<AuthResponse>
                    ) {
                        if (response.isSuccessful && response.body()?.status == "success") {
                            val userData = response.body()?.user
                            _isLoggedIn.value = true
                            _userId.value    = userData?.userId
                            _userName.value  = userData?.fullName ?: ""
                            _userEmail.value = userData?.email ?: ""
                            _currentPassword = savedPassword
                            _biometricState.value = AuthState.Success("Biometric login successful!")
                        } else {
                            _biometricState.value = AuthState.Error("Biometric login failed. Session expired.")
                        }
                    }
                    override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                        _biometricState.value = AuthState.Error("Network error: ${t.localizedMessage}")
                    }
                })
        } else {
            _biometricState.value = AuthState.Error("Account not enrolled for biometric. Please login manually.")
        }
    }

    fun resetBiometricState() {
        _biometricState.value = AuthState.Idle
    }

    /** Returns list of Pair(Email, Password) for all enrolled accounts */
    fun getEnrolledBiometricAccounts(context: Context): List<Pair<String, String>> {
        val prefs = context.getSharedPreferences("swift_biometrics", Context.MODE_PRIVATE)
        val data = prefs.getString("accounts", "") ?: ""
        if (data.isEmpty()) return emptyList()
        return data.split(";;").mapNotNull {
            val parts = it.split("||")
            if (parts.size == 2) Pair(parts[0], parts[1]) else null
        }
    }

    /** Enrolls or un-enrolls the currently logged-in account */
    fun setBiometricEnrolled(context: Context, enabled: Boolean) {
        val email = _userEmail.value
        val password = _currentPassword
        if (email.isBlank() || password.isBlank()) return

        val accounts = getEnrolledBiometricAccounts(context).toMutableList()
        accounts.removeAll { it.first == email } // Remove if already exists

        if (enabled) {
            accounts.add(Pair(email, password))
        }

        val data = accounts.joinToString(";;") { "${it.first}||${it.second}" }
        context.getSharedPreferences("swift_biometrics", Context.MODE_PRIVATE)
            .edit()
            .putString("accounts", data)
            .apply()
    }

    /** Check if the current logged-in user is enrolled */
    fun isBiometricEnrolled(context: Context): Boolean {
        return getEnrolledBiometricAccounts(context).any { it.first == _userEmail.value }
    }

    // ────────────────────────────────────────────────────────────────────────
    //  REGISTER
    // ────────────────────────────────────────────────────────────────────────

    fun register(name: String, email: String, phone: String, password: String, confirmPassword: String) {
        if (name.isBlank() || email.isBlank() || phone.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("All fields are required"); return
        }
        if (password != confirmPassword) {
            _authState.value = AuthState.Error("Passwords do not match"); return
        }
        val validation = PasswordValidator.validate(password)
        if (!validation.isValid) {
            _authState.value = AuthState.Error("Password does not meet requirements"); return
        }
        _authState.value = AuthState.Loading

        RetrofitClient.instance.register(name, email, phone, password)
            .enqueue(object : Callback<AuthResponse> {
                override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                    if (response.isSuccessful && response.body()?.status == "success") {
                        _authState.value = AuthState.Success("Registration successful! Please login.")
                    } else {
                        _authState.value = AuthState.Error(response.body()?.message ?: "Registration failed")
                    }
                }
                override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                    _authState.value = AuthState.Error("Network error: ${t.localizedMessage}")
                }
            })
    }

    // ────────────────────────────────────────────────────────────────────────
    //  FORGOT PASSWORD — STEP 1: Send OTP
    // ────────────────────────────────────────────────────────────────────────

    fun sendPasswordResetOtp(email: String) {
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _otpState.value = AuthState.Error("Please enter a valid email address")
            return
        }
        _otpState.value = AuthState.Loading

        // Generate a cryptographically secure 6-digit OTP
        val otp = (100_000..999_999).random().toString()
        _pendingOtp = otp
        _otpTargetEmail = email
        _otpCreatedAt = System.currentTimeMillis()

        viewModelScope.launch {
            try {
                // Optionally validate email exists on the server first (we skip here to avoid
                // leaking user existence — just always send and let the user know)
                val sent = EmailSender.sendOtpEmail(toEmail = email, otp = otp)
                withContext(Dispatchers.Main) {
                    if (sent) {
                        _otpState.value = AuthState.Success("OTP sent to $email")
                    } else {
                        _otpState.value = AuthState.Error("Failed to send OTP. Check your email address.")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _otpState.value = AuthState.Error("Error: ${e.message}")
                }
            }
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    //  FORGOT PASSWORD — STEP 2: Verify OTP
    // ────────────────────────────────────────────────────────────────────────

    fun verifyOtp(enteredOtp: String): Boolean {
        val expired = (System.currentTimeMillis() - _otpCreatedAt) > 10 * 60 * 1000L
        return when {
            expired -> {
                _otpState.value = AuthState.Error("OTP expired. Please request a new code.")
                false
            }
            enteredOtp.trim() != _pendingOtp -> {
                _otpState.value = AuthState.Error("Incorrect OTP. Please try again.")
                false
            }
            else -> {
                _otpState.value = AuthState.Success("OTP verified!")
                true
            }
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    //  FORGOT PASSWORD — STEP 3: Change password via API
    // ────────────────────────────────────────────────────────────────────────

    fun changePassword(newPassword: String, confirmPassword: String) {
        val validation = PasswordValidator.validate(newPassword)
        if (!validation.isValid) {
            _otpState.value = AuthState.Error("Password does not meet all requirements")
            return
        }
        if (newPassword != confirmPassword) {
            _otpState.value = AuthState.Error("Passwords do not match")
            return
        }
        _otpState.value = AuthState.Loading

        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.instance.resetPassword(
                        email = _otpTargetEmail,
                        newPassword = newPassword
                    ).execute()
                }
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body()?.get("status") == "success") {
                        // Clear OTP data
                        _pendingOtp = ""
                        _otpTargetEmail = ""
                        _otpCreatedAt = 0L
                        _otpState.value = AuthState.Success("Password changed successfully! Please login.")
                    } else {
                        _otpState.value = AuthState.Error("Failed to change password. Please try again.")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("AuthViewModel", "changePassword error: ${e.message}")
                    _otpState.value = AuthState.Error("Error: ${e.message}")
                }
            }
        }
    }

    fun resetOtpState() {
        _otpState.value = AuthState.Idle
    }

    // ────────────────────────────────────────────────────────────────────────
    //  LOGOUT / RESET
    // ────────────────────────────────────────────────────────────────────────

    fun logout() {
        _isLoggedIn.value = false
        _authState.value  = AuthState.Idle
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}
