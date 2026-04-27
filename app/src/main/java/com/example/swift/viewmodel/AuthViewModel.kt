package com.example.swift.viewmodel

import androidx.lifecycle.ViewModel
import com.example.swift.api.AuthResponse
import com.example.swift.api.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

sealed class AuthState {
    data object Idle : AuthState()
    data object Loading : AuthState()
    data class Success(val message: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private var _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()
    
    private var _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName.asStateFlow()
    
    private var _userEmail = MutableStateFlow("")
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    private var _userId = MutableStateFlow<Int?>(null)
    val userId: StateFlow<Int?> = _userId.asStateFlow()

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
                    _userId.value = userData?.userId
                    _userName.value = userData?.fullName ?: ""
                    _userEmail.value = userData?.email ?: ""
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

    fun register(name: String, email: String, phone: String, password: String, confirmPassword: String) {
        if (name.isBlank() || email.isBlank() || phone.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("All fields are required")
            return
        }
        if (password != confirmPassword) {
            _authState.value = AuthState.Error("Passwords do not match")
            return
        }
        if (password.length < 8) {
            _authState.value = AuthState.Error("Password must be at least 8 characters")
            return
        }
        _authState.value = AuthState.Loading
        
        RetrofitClient.instance.register(name, email, phone, password).enqueue(object : Callback<AuthResponse> {
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

    fun logout() {
        _isLoggedIn.value = false
        _authState.value = AuthState.Idle
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}
