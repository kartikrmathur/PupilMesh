package com.example.pupilmesh.ui.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pupilmesh.data.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    
    private val userRepository = UserRepository(application)
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState
    
    init {
        // Check if user is already logged in
        if (userRepository.isUserLoggedIn()) {
            _authState.value = AuthState.Success
        }
    }
    
    fun attemptLogin(email: String, password: String) {
        _authState.value = AuthState.Loading
        
        viewModelScope.launch {
            try {
                val userExists = userRepository.checkUserExists(email)
                
                if (userExists) {
                    // User exists, try to login
                    val loginSuccess = userRepository.loginUser(email, password)
                    if (loginSuccess) {
                        userRepository.setLoggedInUser(email)
                        _authState.value = AuthState.Success
                    } else {
                        _authState.value = AuthState.Error("Invalid password")
                    }
                } else {
                    // User doesn't exist, create new account
                    userRepository.registerUser(email, password)
                    userRepository.setLoggedInUser(email)
                    _authState.value = AuthState.Success
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Authentication failed")
            }
        }
    }
    
    fun resetState() {
        _authState.value = AuthState.Idle
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
} 