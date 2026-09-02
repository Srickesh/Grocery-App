package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.auth.AuthRepository
import com.example.data.auth.AuthState
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val authRepository = AuthRepository(application.applicationContext)

    val authState: StateFlow<AuthState> = authRepository.authState
    val currentUserProfile: StateFlow<UserProfile?> = authRepository.currentUserProfile

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    private val _isAuthSheetVisible = MutableStateFlow(false)
    val isAuthSheetVisible: StateFlow<Boolean> = _isAuthSheetVisible.asStateFlow()

    fun showAuthSheet() {
        _isAuthSheetVisible.value = true
    }

    fun dismissAuthSheet() {
        _isAuthSheetVisible.value = false
    }

    fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            val result = authRepository.signInWithGoogle(context)
            if (result.isSuccess) {
                _isAuthSheetVisible.value = false
                _userMessage.emit("Welcome back, ${result.getOrNull()?.displayName ?: "Shopper"}! ✨")
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Google Sign-In could not be completed"
                _userMessage.emit(errorMsg)
            }
        }
    }

    fun signInWithEmail(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            viewModelScope.launch { _userMessage.emit("Please enter both email and password") }
            return
        }
        viewModelScope.launch {
            val result = authRepository.signInWithEmail(email, pass)
            if (result.isSuccess) {
                _isAuthSheetVisible.value = false
                _userMessage.emit("Signed in as ${result.getOrNull()?.displayName}!")
            } else {
                _userMessage.emit(result.exceptionOrNull()?.message ?: "Sign-in failed")
            }
        }
    }

    fun signUpWithEmail(email: String, pass: String, name: String) {
        if (email.isBlank() || pass.isBlank()) {
            viewModelScope.launch { _userMessage.emit("Please enter valid email and password") }
            return
        }
        if (pass.length < 6) {
            viewModelScope.launch { _userMessage.emit("Password must be at least 6 characters") }
            return
        }
        viewModelScope.launch {
            val result = authRepository.signUpWithEmail(email, pass, name)
            if (result.isSuccess) {
                _isAuthSheetVisible.value = false
                _userMessage.emit("Welcome to Lekhali Fresh, ${name.ifBlank { "Shopper" }}! 🌿")
            } else {
                _userMessage.emit(result.exceptionOrNull()?.message ?: "Sign-up failed")
            }
        }
    }

    fun signInAsGuest() {
        viewModelScope.launch {
            val result = authRepository.signInAnonymously()
            if (result.isSuccess) {
                _isAuthSheetVisible.value = false
                _userMessage.emit("Continuing as Guest Shopper.")
            } else {
                _userMessage.emit(result.exceptionOrNull()?.message ?: "Guest login failed")
            }
        }
    }

    fun sendPasswordReset(email: String, onDone: (Boolean, String) -> Unit) {
        if (email.isBlank()) {
            onDone(false, "Please enter your email address.")
            return
        }
        viewModelScope.launch {
            val result = authRepository.sendPasswordReset(email)
            if (result.isSuccess) {
                onDone(true, "Password reset email sent to $email.")
                _userMessage.emit("Password reset link sent to $email")
            } else {
                onDone(false, result.exceptionOrNull()?.message ?: "Failed to send reset link")
            }
        }
    }

    fun updateProfile(displayName: String) {
        if (displayName.isBlank()) return
        viewModelScope.launch {
            val result = authRepository.updateProfile(displayName)
            if (result.isSuccess) {
                _userMessage.emit("Profile updated successfully!")
            } else {
                _userMessage.emit(result.exceptionOrNull()?.message ?: "Failed to update profile")
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            _userMessage.emit("Signed out successfully.")
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            val result = authRepository.deleteAccount()
            if (result.isSuccess) {
                _userMessage.emit("Account deleted.")
            } else {
                _userMessage.emit(result.exceptionOrNull()?.message ?: "Could not delete account")
            }
        }
    }

    fun getCurrentUserId(): String = authRepository.getCurrentUserId()
}
